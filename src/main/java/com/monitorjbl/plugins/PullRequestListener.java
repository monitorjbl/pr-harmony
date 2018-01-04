package com.monitorjbl.plugins;

import com.atlassian.bitbucket.build.BuildStatusSetEvent;
import com.atlassian.bitbucket.event.pull.PullRequestParticipantStatusUpdatedEvent;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestMergeRequest;
import com.atlassian.bitbucket.pull.PullRequestSearchRequest;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.pull.PullRequestState;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.user.SecurityService;
import com.atlassian.bitbucket.util.Page;
import com.atlassian.bitbucket.util.PageRequest;
import com.atlassian.bitbucket.util.PageRequestImpl;
import com.atlassian.event.api.EventListener;
import com.monitorjbl.plugins.config.Config;
import com.monitorjbl.plugins.config.ConfigDao;

import java.util.concurrent.atomic.AtomicBoolean;

public class PullRequestListener {
  private static final String PR_APPROVE_BUCKET = "AUTOMERGE_PR_APPROVAL";
  private static final String BUILD_APPROVE_BUCKET = "AUTOMERGE_BUILD_APPROVAL";
  public static final int MAX_COMMITS = 1048576;
  public static final int SEARCH_PAGE_SIZE = 50;

  private final AsyncProcessor asyncProcessor;
  private final ConfigDao configDao;
  private final PullRequestService prService;
  private final SecurityService securityService;
  private final RegexUtils regexUtils;

  public PullRequestListener(AsyncProcessor asyncProcessor, ConfigDao configDao, PullRequestService prService,
                             SecurityService securityService, RegexUtils regexUtils) {
    this.asyncProcessor = asyncProcessor;
    this.configDao = configDao;
    this.prService = prService;
    this.securityService = securityService;
    this.regexUtils = regexUtils;
  }

  @EventListener
  public void prApprovalListener(PullRequestParticipantStatusUpdatedEvent event) {
    asyncProcessor.dispatch(new ApprovalTaskProcessor(event.getPullRequest()));
  }

  @EventListener
  public void buildStatusListener(BuildStatusSetEvent event) {
    asyncProcessor.dispatch(new BuildTaskProcessor(event.getCommitId()));
  }

  void automergePullRequest(PullRequest pr) {
    Repository repo = pr.getToRef().getRepository();
    Config config = configDao.getConfigForRepo(repo.getProject().getKey(), repo.getSlug());
    String toBranch = regexUtils.formatBranchName(pr.getToRef().getId());
    String fromBranch = regexUtils.formatBranchName(pr.getFromRef().getId());

    if((regexUtils.match(config.getAutomergePRs(), toBranch) || regexUtils.match(config.getAutomergePRsFrom(), fromBranch)) &&
        !regexUtils.match(config.getBlockedPRs(), toBranch) && prService.canMerge(repo.getId(), pr.getId()).canMerge()) {
      securityService.impersonating(pr.getAuthor().getUser(), "Performing automerge on behalf of " + pr.getAuthor().getUser().getSlug()).call(() -> {
        prService.merge(new PullRequestMergeRequest.Builder(pr).build());
        return null;
      });
    }
  }

  PullRequest findPRByCommitId(String commitId) {
    PullRequestSearchRequest request = new PullRequestSearchRequest.Builder()
        .state(PullRequestState.OPEN)
        .withProperties(false)
        .build();
    PageRequest nextPage = new PageRequestImpl(0, SEARCH_PAGE_SIZE);
    do {
      Page<PullRequest> page = prService.search(request, nextPage);
      PullRequest pr = searchForPR(page, commitId);
      if(pr != null) {
        return pr;
      } else {
        nextPage = page.getNextPageRequest();
      }
    } while(nextPage != null);
    return null;
  }

  private PullRequest searchForPR(Page<PullRequest> requests, String commitId) {
    for(PullRequest pr : requests.getValues()) {
      AtomicBoolean found = new AtomicBoolean(false);
      prService.streamCommits(pr.getToRef().getRepository().getId(), pr.getId(), commit -> {
        found.set(commit.getId().equals(commitId));
        return !found.get();
      });
      if(found.get()) {
        return pr;
      }
    }
    return null;
  }

  private class ApprovalTaskProcessor implements Runnable {

    private final PullRequest pr;

    public ApprovalTaskProcessor(PullRequest pr) {
      this.pr = pr;
    }

    @Override
    public void run() {
      securityService.withPermission(Permission.ADMIN, "Automerge check (PR approval)").call(() -> {
        automergePullRequest(prService.getById(pr.getToRef().getRepository().getId(), pr.getId()));
        return null;
      });
    }
  }

  private class BuildTaskProcessor implements Runnable {
    private final String commitId;

    public BuildTaskProcessor(String commitId) {
      this.commitId = commitId;
    }

    @Override
    public void run() {
      securityService.withPermission(Permission.ADMIN, "Automerge check (PR approval)").call(() -> {
        PullRequest pr = findPRByCommitId(commitId);
        if(pr != null) {
          automergePullRequest(pr);
        }
        return null;
      });
    }
  }

}

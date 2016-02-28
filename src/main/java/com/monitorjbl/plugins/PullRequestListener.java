package com.monitorjbl.plugins;

import com.atlassian.bitbucket.commit.Commit;
import com.atlassian.bitbucket.event.pull.PullRequestParticipantStatusUpdatedEvent;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestMergeRequest;
import com.atlassian.bitbucket.pull.PullRequestSearchRequest;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.pull.PullRequestState;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.user.SecurityService;
import com.atlassian.bitbucket.util.Operation;
import com.atlassian.bitbucket.util.Page;
import com.atlassian.bitbucket.util.PageRequestImpl;
import com.atlassian.event.api.EventListener;
import com.monitorjbl.plugins.config.Config;
import com.monitorjbl.plugins.config.ConfigDao;

//import com.atlassian.stash.build.BuildStatusSetEvent;

public class PullRequestListener {
  public static final int MAX_COMMITS = 1048576;

  private final ConfigDao configDao;
  private final UserUtils utils;
  private final PullRequestService prService;
  private final SecurityService securityService;
  private final RegexUtils regexUtils;

  public PullRequestListener(ConfigDao configDao, UserUtils utils, PullRequestService prService, SecurityService securityService, RegexUtils regexUtils) {
    this.configDao = configDao;
    this.utils = utils;
    this.prService = prService;
    this.securityService = securityService;
    this.regexUtils = regexUtils;
  }

  @EventListener
  public void prApprovalListener(PullRequestParticipantStatusUpdatedEvent event) {
    automergePullRequest(event.getPullRequest());
  }

//  @EventListener
//  public void buildStatusListener(BuildStatusSetEvent event) {
//    PullRequest pr = findPRByCommitId(event.getCommitId());
//    if (pr != null) {
//      automergePullRequest(pr);
//    }
//  }

  void automergePullRequest(final PullRequest pr) {
    Repository repo = pr.getToRef().getRepository();
    Config config = configDao.getConfigForRepo(repo.getProject().getKey(), repo.getSlug());
    String toBranch = regexUtils.formatBranchName(pr.getToRef().getId());
    String fromBranch = regexUtils.formatBranchName(pr.getFromRef().getId());

    if((regexUtils.match(config.getAutomergePRs(), toBranch) || regexUtils.match(config.getAutomergePRsFrom(), fromBranch)) &&
        !regexUtils.match(config.getBlockedPRs(), toBranch) && prService.canMerge(repo.getId(), pr.getId()).canMerge()) {
      securityService.withPermission(Permission.ADMIN, "Automerging pull request").call(new Operation<Object, RuntimeException>() {
        public Object perform() throws RuntimeException {
          return prService.merge(new PullRequestMergeRequest.Builder(pr).build());
        }
      });
    }
  }

  PullRequest findPRByCommitId(String commitId) {
    int start = 0;
    Page<PullRequest> requests = null;
    while(requests == null || requests.getSize() > 0) {
      requests = prService.search(new PullRequestSearchRequest.Builder()
          .state(PullRequestState.OPEN)
          .build(), new PageRequestImpl(start, 10));
      for(PullRequest pr : requests.getValues()) {
        Page<Commit> commits = prService.getCommits(pr.getToRef().getRepository().getId(), pr.getId(), new PageRequestImpl(0, MAX_COMMITS));
        for(Commit c : commits.getValues()) {
          if(c.getId().equals(commitId)) {
            return pr;
          }
        }
      }
      start += 10;
    }
    return null;
  }
}

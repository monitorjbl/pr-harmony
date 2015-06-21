package com.monitorjbl.plugins;

import com.atlassian.event.api.EventListener;
//import com.atlassian.stash.build.BuildStatusSetEvent;
import com.atlassian.stash.commit.Commit;
import com.atlassian.stash.event.pull.PullRequestApprovedEvent;
import com.atlassian.stash.event.pull.PullRequestOpenedEvent;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.pull.PullRequestMergeRequest;
import com.atlassian.stash.pull.PullRequestParticipant;
import com.atlassian.stash.pull.PullRequestRole;
import com.atlassian.stash.pull.PullRequestSearchRequest;
import com.atlassian.stash.pull.PullRequestService;
import com.atlassian.stash.pull.PullRequestState;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.SecurityService;
import com.atlassian.stash.util.Operation;
import com.atlassian.stash.util.Page;
import com.atlassian.stash.util.PageRequestImpl;
import com.google.common.base.Function;
import com.monitorjbl.plugins.config.Config;
import com.monitorjbl.plugins.config.ConfigDao;

import java.util.Set;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;

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
  public void populateDefaultReviewers(PullRequestOpenedEvent event) {
    final PullRequest pr = event.getPullRequest();
    final Repository repo = pr.getToRef().getRepository();
    Config config = configDao.getConfigForRepo(repo.getProject().getKey(), repo.getSlug());

    //combine existing reviewers with required reviewers
    final Set<String> reviewers = newHashSet(concat(transform(pr.getReviewers(), new Function<PullRequestParticipant, String>() {
      @Override
      public String apply(PullRequestParticipant input) {
        return input.getUser().getSlug();
      }
    }), config.getDefaultReviewers(), utils.dereferenceGroups(config.getDefaultReviewerGroups())));

    securityService.withPermission(Permission.ADMIN, "Adding default reviewers").call(new Operation<Object, RuntimeException>() {
      public Object perform() throws RuntimeException {
        for (String u : reviewers) {
          if (!pr.getAuthor().getUser().getSlug().equals(u)) {
            prService.assignRole(repo.getId(), pr.getId(), u, PullRequestRole.REVIEWER);
          }
        }
        return null;
      }
    });
  }

  @EventListener
  public void prApprovalListener(PullRequestApprovedEvent event) {
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

    if ((regexUtils.match(config.getAutomergePRs(), toBranch) || regexUtils.match(config.getAutomergePRsFrom(), fromBranch)) &&
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
    while (requests == null || requests.getSize() > 0) {
      requests = prService.search(new PullRequestSearchRequest.Builder()
          .state(PullRequestState.OPEN)
          .build(), new PageRequestImpl(start, 10));
      for (PullRequest pr : requests.getValues()) {
        Page<Commit> commits = prService.getCommits(pr.getToRef().getRepository().getId(), pr.getId(), new PageRequestImpl(0, MAX_COMMITS));
        for (Commit c : commits.getValues()) {
          if (c.getId().equals(commitId)) {
            return pr;
          }
        }
      }
      start += 10;
    }
    return null;
  }
}

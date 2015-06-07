package com.monitorjbl.plugins;

import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.scm.pull.MergeRequest;
import com.atlassian.stash.scm.pull.MergeRequestCheck;
import com.google.common.base.Joiner;
import com.monitorjbl.plugins.config.Config;
import com.monitorjbl.plugins.config.ConfigDao;

import javax.annotation.Nonnull;
import java.util.Set;

public class MergeBlocker implements MergeRequestCheck {
  public static final String REFS_PREFIX = "refs/heads/";
  private final ConfigDao configDao;
  private final UserUtils utils;

  public MergeBlocker(ConfigDao configDao, UserUtils utils) {
    this.configDao = configDao;
    this.utils = utils;
  }

  @Override
  public void check(@Nonnull MergeRequest mergeRequest) {
    PullRequest pr = mergeRequest.getPullRequest();
    Repository repo = pr.getToRef().getRepository();
    final Config config = configDao.getConfigForRepo(repo.getProject().getKey(), repo.getSlug());

    String branch = pr.getToRef().getId().replace(REFS_PREFIX, "");
    if (config.getBlockedPRs().contains(branch)) {
      mergeRequest.veto("Pull Request Blocked", "Pull requests have been disabled for branch [" + branch + "]");
    } else {
      PullRequestApproval approval = new PullRequestApproval(config, utils);
      if (!approval.isPullRequestApproved(pr)) {
        Set<String> missing = approval.missingRevieiwers(pr);
        mergeRequest.veto("Required reviewers must approve", (config.getRequiredReviews() - approval.seenReviewers(pr).size()) +
            " more approvals required from the following users: " + Joiner.on(',').join(missing));
      }
    }
  }

}

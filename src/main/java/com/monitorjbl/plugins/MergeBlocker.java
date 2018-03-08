package com.monitorjbl.plugins;

import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestParticipant;
import com.atlassian.bitbucket.pull.PullRequestParticipantStatus;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.scm.pull.MergeRequest;
import com.atlassian.bitbucket.scm.pull.MergeRequestCheck;
import com.google.common.base.Joiner;
import com.monitorjbl.plugins.config.Config;
import com.monitorjbl.plugins.config.ConfigDao;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.Predicate;

public class MergeBlocker implements MergeRequestCheck {
  private final ConfigDao configDao;
  private final RegexUtils regexUtils;
  private final UserUtils userUtils;

  public MergeBlocker(ConfigDao configDao, RegexUtils regexUtils, UserUtils userUtils) {
    this.configDao = configDao;
    this.regexUtils = regexUtils;
    this.userUtils = userUtils;
  }

  @Override
  public void check(@Nonnull MergeRequest mergeRequest) {
    PullRequest pr = mergeRequest.getPullRequest();
    Repository repo = pr.getToRef().getRepository();
    Config config = configDao.getConfigForRepo(repo.getProject().getKey(), repo.getSlug());

    String branch = pr.getToRef().getDisplayId();
    if (regexUtils.match(config.getBlockedPRs(), branch)) {
      mergeRequest.veto("Pull Request Blocked", "Pull requests have been disabled for branch [" + branch + "]");
    } else {
      PullRequestApproval approval = new PullRequestApproval(config, userUtils);
      if (!approval.isPullRequestApproved(pr)) {
        Set<String> missing = approval.missingReviewersNames(pr);
        mergeRequest.veto("Required reviewers must approve", (config.getRequiredReviews() - approval.seenReviewers(pr).size()) +
            " more approvals required from the following users: " + Joiner.on(", ").join(missing));
      } else {
        Boolean needsWork = config.getBlockMergeIfPrNeedsWork();
        final Boolean blockAutoMergeBecausePrNeedsWork = needsWork != null && needsWork && needsWork(pr);

        if (blockAutoMergeBecausePrNeedsWork) {
          mergeRequest.veto("Needs work", "PR marked as Needs Work from reviewer(s)");
        }
      }
    }
  }

  private boolean needsWork(PullRequest pr) {
    return pr.getReviewers().stream()
            .map(PullRequestParticipant::getStatus)
            .anyMatch(Predicate.isEqual(PullRequestParticipantStatus.NEEDS_WORK));
  }
}

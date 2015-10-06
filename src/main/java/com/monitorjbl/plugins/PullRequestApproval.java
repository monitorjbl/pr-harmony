package com.monitorjbl.plugins;

import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestParticipant;
import com.google.common.base.Function;
import com.monitorjbl.plugins.config.Config;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Maps.uniqueIndex;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.newHashSet;

public class PullRequestApproval {

  private final Config config;
  private final UserUtils utils;

  public PullRequestApproval(Config config, UserUtils utils) {
    this.config = config;
    this.utils = utils;
  }

  public boolean isPullRequestApproved(PullRequest pr) {
    Integer requiredReviews = config.getRequiredReviews();
    return requiredReviews == null || seenReviewers(pr).size() >= requiredReviews;
  }

  public Set<String> missingRevieiwers(PullRequest pr) {
    Map<String, PullRequestParticipant> map = transformReviewers(pr);
    Set<String> missingReviewers = newHashSet();

    for(String req : concat(config.getRequiredReviewers(), utils.dereferenceGroups(config.getRequiredReviewerGroups()))) {
      if(reviewerIsMissing(map.get(req)) && !(submitterIsRequiredReviewer(pr, req) && exactlyEnoughRequiredReviewers())) {
        missingReviewers.add(req);
      }
    }
    return missingReviewers;
  }

  public Set<String> seenReviewers(PullRequest pr) {
    Set<String> required = newHashSet(concat(config.getRequiredReviewers(), utils.dereferenceGroups(config.getRequiredReviewerGroups())));
    return difference(required, missingRevieiwers(pr));
  }

  Map<String, PullRequestParticipant> transformReviewers(PullRequest pr) {
    return uniqueIndex(pr.getReviewers(), new Function<PullRequestParticipant, String>() {
      public String apply(PullRequestParticipant input) {
        return input.getUser().getSlug();
      }
    });
  }

  Boolean reviewerIsMissing(PullRequestParticipant reviewer) {
    return reviewer == null || !reviewer.isApproved();
  }

  Boolean submitterIsRequiredReviewer(PullRequest pr, String username) {
    return pr.getAuthor().getUser().getSlug().equals(username);
  }

  Boolean exactlyEnoughRequiredReviewers() {
    return Objects.equals(config.getRequiredReviewers().size(), config.getRequiredReviews());
  }
}

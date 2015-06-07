package com.monitorjbl.plugins;

import com.atlassian.event.api.EventListener;
import com.atlassian.stash.event.pull.PullRequestApprovedEvent;
import com.atlassian.stash.event.pull.PullRequestOpenedEvent;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.pull.PullRequestParticipant;
import com.atlassian.stash.pull.PullRequestRole;
import com.atlassian.stash.pull.PullRequestService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.SecurityService;
import com.atlassian.stash.util.Operation;
import com.google.common.base.Function;
import com.monitorjbl.plugins.config.Config;
import com.monitorjbl.plugins.config.ConfigDao;

import java.util.Set;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;

public class PullRequestListener {
  private final ConfigDao configDao;
  private final UserUtils utils;
  private final PullRequestService prService;
  private final SecurityService securityService;

  public PullRequestListener(ConfigDao configDao, UserUtils utils, PullRequestService prService, SecurityService securityService) {
    this.configDao = configDao;
    this.utils = utils;
    this.prService = prService;
    this.securityService = securityService;
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
  public void automergePullRequest(PullRequestApprovedEvent event) {
    PullRequest pr = event.getPullRequest();
    Repository repo = pr.getToRef().getRepository();
    Config config = configDao.getConfigForRepo(repo.getProject().getKey(), repo.getSlug());
    String branch = pr.getToRef().getId().replaceAll(MergeBlocker.REFS_PREFIX, "");

    if (config.getAutomergePRs().contains(branch) && !config.getBlockedPRs().contains(branch) &&
        prService.canMerge(repo.getId(), pr.getId()).canMerge()) {
      prService.merge(repo.getId(), pr.getId(), pr.getVersion());
    }
  }

}

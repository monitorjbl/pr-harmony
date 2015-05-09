package com.monitorjbl.plugins;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.scm.pull.MergeRequest;
import com.atlassian.stash.scm.pull.MergeRequestCheck;
import com.monitorjbl.plugins.config.Config;
import com.monitorjbl.plugins.config.ConfigDao;

public class MergeBlocker implements MergeRequestCheck {
  private final ConfigDao configDao;
  private final UserManager userManager;

  public MergeBlocker(ConfigDao configDao, UserManager userManager) {
    this.configDao = configDao;
    this.userManager = userManager;
  }

  @Override
  public void check(MergeRequest mergeRequest) {
    PullRequest pr = mergeRequest.getPullRequest();
    Repository repo = pr.getToRef().getRepository();
    Config config = configDao.getConfigForRepo(repo.getProject().getKey(), repo.getSlug());

    String branch = pr.getToRef().getId().replace("refs/heads/", "");
    if (config.getBlockedPRs().contains(branch)) {
      mergeRequest.veto("Pull Request Blocked", "Pull requests have been disabled for branch [" + branch + "]");
    }
  }
}

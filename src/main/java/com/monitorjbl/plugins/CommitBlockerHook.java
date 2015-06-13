package com.monitorjbl.plugins;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.stash.hook.HookResponse;
import com.atlassian.stash.hook.PreReceiveHook;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.Repository;
import com.monitorjbl.plugins.config.Config;
import com.monitorjbl.plugins.config.ConfigDao;

import java.util.Collection;

public class CommitBlockerHook implements PreReceiveHook {
  private final ConfigDao configDao;
  private final UserManager userManager;
  private final RegexUtils regexUtils;

  public CommitBlockerHook(ConfigDao configDao, UserManager userManager, RegexUtils regexUtils) {
    this.configDao = configDao;
    this.userManager = userManager;
    this.regexUtils = regexUtils;
  }

  @Override
  public boolean onReceive(Repository repository, Collection<RefChange> collection, HookResponse hookResponse) {
    Config config = configDao.getConfigForRepo(repository.getProject().getKey(), repository.getSlug());

    UserProfile user = userManager.getRemoteUser();
    for (RefChange ch : collection) {
      String branch = regexUtils.formatBranchName(ch.getRefId());
      if (regexUtils.match(config.getBlockedCommits(), branch) && !config.getExcludedUsers().contains(user.getUsername())) {
        hookResponse.err().write("\n" +
                "******************************\n" +
                "*    !! Commit Rejected !!   *\n" +
                "******************************\n\n" +
                "Direct commits are not allowed\n" +
                "to branch [" + branch + "].\n\n"
        );
        return false;
      }
    }
    return true;
  }
}

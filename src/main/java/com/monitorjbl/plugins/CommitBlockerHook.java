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

  public CommitBlockerHook(ConfigDao configDao, UserManager userManager) {
    this.configDao = configDao;
    this.userManager = userManager;
  }

  @Override
  public boolean onReceive(Repository repository, Collection<RefChange> collection, HookResponse hookResponse) {
    Config config = configDao.getConfigForRepo(repository.getProject().getKey(), repository.getSlug());

    UserProfile user = userManager.getRemoteUser();
    for (RefChange ch : collection) {
      String branch = ch.getRefId().replace("refs/heads/", "");
      if (config.getBlockedCommits().contains(branch) && !config.getExcludedUsers().contains(user.getUsername())) {
        hookResponse.err().write("\n" +
                "******************************\n" +
                "*    !! Commit Rejected !!   *\n" +
                "******************************\n\n"+
                "Direct commits are not allowed\n"+
                "to branch ["+branch+"].\n\n"
        );
        return false;
      }
    }
    return true;
  }
}

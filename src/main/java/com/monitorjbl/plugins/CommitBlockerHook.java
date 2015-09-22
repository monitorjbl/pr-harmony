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
import java.util.Set;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Sets.newHashSet;

public class CommitBlockerHook implements PreReceiveHook {
  private final ConfigDao configDao;
  private final UserManager userManager;
  private final RegexUtils regexUtils;
  private final UserUtils userUtils;

  public CommitBlockerHook(ConfigDao configDao, UserManager userManager, RegexUtils regexUtils, UserUtils userUtils) {
    this.configDao = configDao;
    this.userManager = userManager;
    this.regexUtils = regexUtils;
    this.userUtils = userUtils;
  }

  @Override
  public boolean onReceive(Repository repository, Collection<RefChange> collection, HookResponse hookResponse) {
    Config config = configDao.getConfigForRepo(repository.getProject().getKey(), repository.getSlug());

    UserProfile user = userManager.getRemoteUser();
    for(RefChange ch : collection) {
      String branch = regexUtils.formatBranchName(ch.getRefId());
      Set<String> excluded = newHashSet(concat(config.getExcludedUsers(), userUtils.dereferenceGroups(config.getExcludedGroups())));
      if(regexUtils.match(config.getBlockedCommits(), branch) && !excluded.contains(user.getUsername())) {
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

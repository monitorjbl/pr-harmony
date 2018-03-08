package com.monitorjbl.plugins;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.hook.HookResponse;
import com.atlassian.bitbucket.hook.PreReceiveHook;
import com.atlassian.bitbucket.repository.MinimalRef;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.StandardRefType;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.monitorjbl.plugins.config.Config;
import com.monitorjbl.plugins.config.ConfigDao;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class CommitBlockerHook implements PreReceiveHook {
  private final AuthenticationContext authenticationContext;
  private final ConfigDao configDao;
  private final RegexUtils regexUtils;
  private final UserUtils userUtils;

  public CommitBlockerHook(AuthenticationContext authenticationContext, ConfigDao configDao,
                           RegexUtils regexUtils, UserUtils userUtils) {
    this.authenticationContext = authenticationContext;
    this.configDao = configDao;
    this.regexUtils = regexUtils;
    this.userUtils = userUtils;
  }

  @Override
  public boolean onReceive(@Nonnull Repository repository, @Nonnull Collection<RefChange> collection,
                           @Nonnull HookResponse hookResponse) {
    Config config = configDao.getConfigForRepo(repository.getProject().getKey(), repository.getSlug());
    List<String> blockedCommits = config.getBlockedCommits();
    if (blockedCommits.isEmpty()) {
      //If no branches have been configured to block direct commits, allow the push
      return true;
    }

    List<String> restrictedBranches = collection.stream()
        .map(RefChange::getRef)
        .filter(ref -> StandardRefType.TAG != ref.getType()) //Only consider branches
        .map(MinimalRef::getDisplayId)                       //Use branch name, not ID (no refs/heads/ prefix)
        .filter(branchName -> regexUtils.match(blockedCommits, branchName))
        .collect(toList());
    if (restrictedBranches.isEmpty()) {
      //If none of the branches being pushed to is configured to block direct commits, allow the push
      return true;
    }

    //Otherwise, if the user is pushing to at least one branch which does not allow direct commits, check
    //whether the they've been excluded from the restriction
    ApplicationUser user = authenticationContext.getCurrentUser();
    if (user != null && isExcluded(config, user)) {
      //If they are, allow the push
      return true;
    }

    //If they're not, list the branch(es) the user isn't allowed to push to directly and block the push
    hookResponse.err().write("\n" +
        "******************************\n" +
        "*     !! Push Rejected !!    *\n" +
        "******************************\n\n");
    if (restrictedBranches.size() == 1) {
      hookResponse.err().write("Direct pushes are not allowed for [" + restrictedBranches.get(0) + "]");
    } else {
      hookResponse.err().write("The following branches do not allow direct pushes:\n\t" +
          restrictedBranches.stream().collect(joining("\n\t")));
    }
    hookResponse.err().write("\n\n");

    return false;
  }

  private boolean isExcluded(Config config, ApplicationUser user) {
    return config.getExcludedUsers().contains(user.getName()) ||
        userUtils.dereferenceGroups(config.getExcludedGroups()).contains(user.getSlug());
  }
}

package com.monitorjbl.plugins;

import com.atlassian.stash.user.StashUser;
import com.atlassian.stash.user.UserService;
import com.atlassian.stash.util.PageRequestImpl;
import com.monitorjbl.plugins.config.Config;
import com.monitorjbl.plugins.config.ConfigDao;
import com.monitorjbl.plugins.config.User;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

public class UserUtils {

  private final UserService userService;
  private final ConfigDao configDao;

  public UserUtils(UserService userService, ConfigDao configDao) {
    this.userService = userService;
    this.configDao = configDao;
  }

  public List<User> getDefaultAndRequiredUsers(String projectKey, String repoSlug) {
    Config cfg = configDao.getConfigForRepo(projectKey, repoSlug);
    Set<User> users = newHashSet();
    Iterable<String> defaultAndRequired = concat(
        cfg.getDefaultReviewers(),
        cfg.getRequiredReviewers(),
        dereferenceGroups(cfg.getDefaultReviewerGroups()),
        dereferenceGroups(cfg.getRequiredReviewerGroups()));

    for (String u : defaultAndRequired) {
      StashUser user = userService.getUserBySlug(u);
      users.add(new User(user.getSlug(), user.getDisplayName()));
    }
    return newArrayList(users);
  }

  public List<String> dereferenceGroups(List<String> groups) {
    List<String> users = newArrayList();
    for (String group : groups) {
      for (StashUser u : userService.findUsersByGroup(group, new PageRequestImpl(0, 25)).getValues()) {
        users.add(u.getSlug());
      }
    }
    return users;
  }
}

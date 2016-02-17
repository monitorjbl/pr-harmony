package com.monitorjbl.plugins;

import com.atlassian.stash.user.StashUser;
import com.atlassian.stash.user.UserService;
import com.atlassian.stash.util.PageRequestImpl;
import com.monitorjbl.plugins.config.User;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class UserUtils {

  private final UserService userService;

  public UserUtils(UserService userService) {
    this.userService = userService;
  }

  public List<User> dereferenceUsers(Iterable<String> users) {
    List<User> list = newArrayList();
    for (String u : users) {
      list.add(getUserByName(u));
    }
    return list;
  }

  public User getUserByName(String username) {
    StashUser user = userService.getUserByName(username);
    return new User(user.getSlug(), user.getDisplayName());
  }

  public StashUser getApplicationUserByName(String username) {
    return userService.getUserByName(username);
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

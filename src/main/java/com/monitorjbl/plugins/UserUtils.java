package com.monitorjbl.plugins;

import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.bitbucket.user.UserService;
import com.atlassian.bitbucket.util.PageRequestImpl;
import com.monitorjbl.plugins.config.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

public class UserUtils {
  private static final int RESULTS_PER_REQUEST = 25;
  private final UserService userService;

  public UserUtils(UserService userService) {
    this.userService = userService;
  }

  public List<User> dereferenceUsers(Iterable<String> users) {
    List<User> list = newArrayList();
    for(String u : users) {
      Optional<User> user = getUserByName(u);
      if(user.isPresent()) {
        list.add(user.get());
      }
    }
    return list;
  }

  public Optional<User> getUserByName(String username) {
    ApplicationUser user = userService.getUserByName(username);
    return user == null ? Optional.empty() : Optional.of(new User(user.getName(), user.getDisplayName()));
  }

  public ApplicationUser getApplicationUserByName(String username) {
    return userService.getUserByName(username);
  }

  public List<String> dereferenceGroups(List<String> groups) {
    List<String> users = newArrayList();
    for(String group : groups) {
      List<ApplicationUser> results = newArrayList(userService.findUsersByGroup(group, new PageRequestImpl(0, RESULTS_PER_REQUEST)).getValues());
      for(int i = 1; results.size() > 0; i++) {
        users.addAll(results.stream()
            .map(ApplicationUser::getSlug)
            .collect(Collectors.toList()));
        results = newArrayList(userService.findUsersByGroup(group, new PageRequestImpl(i * RESULTS_PER_REQUEST, RESULTS_PER_REQUEST)).getValues());
      }
    }
    return users;
  }


}

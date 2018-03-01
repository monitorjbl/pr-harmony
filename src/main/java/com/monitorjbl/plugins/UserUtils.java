package com.monitorjbl.plugins;

import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.bitbucket.user.UserService;
import com.atlassian.bitbucket.util.PagedIterable;
import com.monitorjbl.plugins.config.User;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.bitbucket.util.MoreStreams.streamIterable;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class UserUtils {
  private static final int RESULTS_PER_REQUEST = 25;
  private final UserService userService;

  public UserUtils(UserService userService) {
    this.userService = userService;
  }

  public List<User> dereferenceUsers(Collection<String> usernames) {
    Set<String> distinctNames = new HashSet<>(usernames);
    if (distinctNames.isEmpty()) {
      return Collections.emptyList();
    }

    Set<ApplicationUser> usersByName = userService.getUsersByName(distinctNames);
    if (usersByName.isEmpty()) {
      return Collections.emptyList();
    }

    return usersByName.stream()
        .map(user -> new User(user.getName(), user.getDisplayName()))
        .collect(toList());
  }

  public String getUserDisplayNameByName(String username) {
    return ofNullable(userService.getUserByName(username))
        .map(ApplicationUser::getDisplayName)
        .orElse(username);
  }
  
  public ApplicationUser getApplicationUserByName(String username) {
    return userService.getUserByName(username);
  }

  public List<String> dereferenceGroups(Collection<String> groups) {
    return groups.stream()
        //Replace each group with a stream of the users in that group
        .flatMap(group ->
            streamIterable(new PagedIterable<>(
                pageRequest -> userService.findUsersByGroup(group, pageRequest),
                RESULTS_PER_REQUEST)))
        //Transform each user to its slug
        .map(ApplicationUser::getSlug)
        //Drop any duplicates for users who exist in multiple groups
        .distinct()
        //Return a list of the distinct slugs
        .collect(toList());
  }
}

package com.monitorjbl.plugins;

import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.bitbucket.user.UserService;
import com.atlassian.bitbucket.util.PagedIterable;
import com.google.common.collect.Iterables;
import com.monitorjbl.plugins.config.User;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.atlassian.bitbucket.util.MoreStreams.streamIterable;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class UserUtils {
  private static final int RESULTS_PER_REQUEST = 50;

  private final UserService userService;

  public UserUtils(UserService userService) {
    this.userService = userService;
  }

  /**
   * Returns a list of the distinct users across the provided set of usernames and members from the specified groups.
   * If a given user is present in multiple groups, or is included by both a group and name, they will only appear
   * once in the returned list. The ordering of the returned list is not guaranteed.
   *
   * @param usernames  a collection containing zero or more usernames
   * @param groupNames a collection containing zero or more group names
   * @return a list of the <i>unique</i> users between all of the provided usernames and groups
   */
  public List<User> dereferenceUsers(Collection<String> usernames, Collection<String> groupNames) {
    if (usernames.isEmpty() && groupNames.isEmpty()) {
      return Collections.emptyList();
    }

    Set<String> slugs = new HashSet<>();
    return Stream.concat(streamUsers(usernames), streamGroupMembers(groupNames))
            .filter(user -> slugs.add(user.getSlug())) //Only add each user once
            .map(user -> new User(user.getName(), user.getDisplayName()))
            .collect(toList());
  }

  public String getUserDisplayNameByName(String username) {
    return ofNullable(userService.getUserByName(username))
        .map(ApplicationUser::getDisplayName)
        .orElse(username);
  }

  /**
   * Returns a set of {@link ApplicationUser#getSlug slugs} for all of the users in the specified groups.
   *
   * @param groups a collection containing one or more group names
   * @return a set of {@link ApplicationUser#getSlug user slugs} for group members
   */
  public Set<String> dereferenceGroups(Collection<String> groups) {
    return streamGroupMembers(groups)
        //Transform each user to its slug
        .map(ApplicationUser::getSlug)
        //Return a set of the distinct slugs
        .collect(toSet());
  }

  private Stream<ApplicationUser> streamGroupMembers(Collection<String> groups) {
    if (groups.isEmpty()) {
      return Stream.empty();
    }
    //Replace each group with a stream of the users in that group
    return groups.stream()
        .flatMap(group ->
            streamIterable(new PagedIterable<>(
                pageRequest -> userService.findUsersByGroup(group, pageRequest),
                RESULTS_PER_REQUEST)));
  }

  private Stream<ApplicationUser> streamUsers(Collection<String> usernames) {
    if (usernames.isEmpty()) {
      return Stream.empty();
    }
    //Look up users in batches of up to 50. For most configurations this will retrieve all of the users in a
    //single lookup, and for configurations with a large number of users it will avoid loading a large number
    //of ApplicationUser instances into memory
    return streamIterable(Iterables.partition(usernames, RESULTS_PER_REQUEST))
            .map(HashSet::new)
            .map(userService::getUsersByName)
            .flatMap(Collection::stream);
  }
}

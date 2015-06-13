package com.monitorjbl.plugins.config;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.stash.user.UserService;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;

import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

public class ConfigDao {
  public static final String REQUIRED_REVIEWS = "requiredReviews";
  public static final String REQUIRED_REVIWERS = "requiredReviewers";
  public static final String REQUIRED_REVIWER_GROUPS = "requiredReviewerGroups";
  public static final String DEFAULT_REVIEWERS = "defaultReviewers";
  public static final String DEFAULT_REVIEWER_GROUPS = "defaultReviewerGroups";
  public static final String EXCLUDED_USERS = "excludedUsers";
  public static final String EXCLUDED_GROUPS = "excludedGroups";
  public static final String BLOCKED_COMMITS = "blockedCommits";
  public static final String BLOCKED_PRS = "blockedPRs";
  public static final String AUTOMERGE_PRS = "automergePRs";
  public static final String AUTOMERGE_PRS_FROM = "automergePRsFrom";

  private final PluginSettingsFactory pluginSettingsFactory;
  private final UserService userService;
  private final Predicate noOpFilter = new NoOpFilter();

  public ConfigDao(PluginSettingsFactory pluginSettingsFactory, UserService userService) {
    this.pluginSettingsFactory = pluginSettingsFactory;
    this.userService = userService;
  }

  public Config getConfigForRepo(String projectKey, String repoSlug) {
    PluginSettings settings = settings(projectKey, repoSlug);
    return Config.builder()
        .requiredReviews(Integer.parseInt(get(settings, REQUIRED_REVIEWS, "0")))
        .requiredReviewers(split(get(settings, REQUIRED_REVIWERS, "")))
        .requiredReviewerGroups(split(get(settings, REQUIRED_REVIWER_GROUPS, "")))
        .defaultReviewers(split(get(settings, DEFAULT_REVIEWERS, "")))
        .defaultReviewerGroups(split(get(settings, DEFAULT_REVIEWER_GROUPS, "")))
        .excludedUsers(split(get(settings, EXCLUDED_USERS, "")))
        .excludedGroups(split(get(settings, EXCLUDED_GROUPS, "")))
        .blockedCommits(split(get(settings, BLOCKED_COMMITS, "")))
        .blockedPRs(split(get(settings, BLOCKED_PRS, "")))
        .automergePRs(split(get(settings, AUTOMERGE_PRS, "")))
        .automergePRsFrom(split(get(settings, AUTOMERGE_PRS_FROM, "")))
        .build();
  }

  @SuppressWarnings("unchecked")
  public void setConfigForRepo(String projectKey, String repoSlug, Config config) {
    PluginSettings settings = settings(projectKey, repoSlug);
    settings.put(REQUIRED_REVIEWS, Integer.toString(config.getRequiredReviews()));
    settings.put(REQUIRED_REVIWERS, join(config.getRequiredReviewers(), new FilterInvalidUsers()));
    settings.put(REQUIRED_REVIWER_GROUPS, join(config.getRequiredReviewerGroups(), new FilterInvalidGroups()));
    settings.put(DEFAULT_REVIEWERS, join(config.getDefaultReviewers(), new FilterInvalidUsers()));
    settings.put(DEFAULT_REVIEWER_GROUPS, join(config.getDefaultReviewerGroups(), new FilterInvalidGroups()));
    settings.put(EXCLUDED_USERS, join(config.getExcludedUsers(), new FilterInvalidUsers()));
    settings.put(EXCLUDED_GROUPS, join(config.getExcludedGroups(), new FilterInvalidGroups()));
    settings.put(BLOCKED_COMMITS, join(config.getBlockedCommits(), noOpFilter));
    settings.put(BLOCKED_PRS, join(config.getBlockedPRs(), noOpFilter));
    settings.put(AUTOMERGE_PRS, join(config.getAutomergePRs(), noOpFilter));
    settings.put(AUTOMERGE_PRS_FROM, join(config.getAutomergePRsFrom(), noOpFilter));
  }

  PluginSettings settings(String projectKey, String repoSlug) {
    return pluginSettingsFactory.createSettingsForKey(projectKey + "-" + repoSlug);
  }

  String get(PluginSettings settings, String key, String defaultValue) {
    String val = (String) settings.get(key);
    return val == null ? defaultValue : val;
  }

  String join(Iterable<String> values, Predicate<String> predicate) {
    return Joiner.on(", ").join(filter(values, predicate));
  }

  List<String> split(String value) {
    if ("".equals(value)) {
      return newArrayList();
    } else {
      return newArrayList(Splitter.on(", ").trimResults().split(value));
    }
  }

  class NoOpFilter implements Predicate {
    @Override
    public boolean apply(Object input) {
      return true;
    }
  }

  class FilterInvalidUsers implements Predicate<String> {
    @Override
    public boolean apply(String username) {
      return userService.getUserBySlug(username.trim().toLowerCase()) != null;
    }
  }

  class FilterInvalidGroups implements Predicate<String> {
    @Override
    public boolean apply(String group) {
      return userService.existsGroup(group.trim().toLowerCase());
    }
  }

}

package com.monitorjbl.plugins.config;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.stash.user.UserService;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;

import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

public class ConfigDao {
  public static final String CHOOSE_REQUIRED_REVIEWERS = "chooseRequiredReviewers";
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
    return overlayConfig(getConfigForProject(projectKey), readConfig(repoSettings(projectKey, repoSlug)));
  }

  public void setConfigForRepo(String projectKey, String repoSlug, Config config) {
    writeConfig(repoSettings(projectKey, repoSlug), reverseOverlayConfig(getConfigForProject(projectKey), config));
  }

  public Config getConfigForProject(String projectKey) {
    return readConfig(projectSettings(projectKey));
  }

  public void setConfigForProject(String projectKey, Config config) {
    writeConfig(projectSettings(projectKey), config);
  }

  Config readConfig(PluginSettings settings) {
    return Config.builder()
        .chooseRequiredReviewers(parseBoolean(get(settings, CHOOSE_REQUIRED_REVIEWERS, null)))
        .requiredReviews(parseInt(get(settings, REQUIRED_REVIEWS, null)))
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
  void writeConfig(PluginSettings settings, Config config) {
    settings.put(CHOOSE_REQUIRED_REVIEWERS, toString(config.getChooseRequiredReviewers()));
    settings.put(REQUIRED_REVIEWS, toString(config.getRequiredReviews()));
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

  /**
   * Overlays the top argument on the bottom argument, replacing any fields
   * that have values on the top argument with fields that have values in the
   * bottom argument
   */
  Config overlayConfig(Config bottom, Config top) {
    return Config.builder()
        .chooseRequiredReviewers(top.getChooseRequiredReviewers() != null ? top.getChooseRequiredReviewers() : bottom.getChooseRequiredReviewers())
        .requiredReviews(top.getRequiredReviews() != null ? top.getRequiredReviews() : bottom.getRequiredReviews())
        .requiredReviewers(overlay(bottom.getRequiredReviewers(), top.getRequiredReviewers()))
        .requiredReviewerGroups(overlay(bottom.getRequiredReviewerGroups(), top.getRequiredReviewerGroups()))
        .defaultReviewers(overlay(bottom.getDefaultReviewers(), top.getDefaultReviewers()))
        .defaultReviewerGroups(overlay(bottom.getDefaultReviewerGroups(), top.getDefaultReviewerGroups()))
        .excludedUsers(overlay(bottom.getExcludedUsers(), top.getExcludedUsers()))
        .excludedGroups(overlay(bottom.getExcludedGroups(), top.getExcludedGroups()))
        .blockedCommits(overlay(bottom.getBlockedCommits(), top.getBlockedCommits()))
        .blockedPRs(overlay(bottom.getBlockedPRs(), top.getBlockedPRs()))
        .automergePRs(overlay(bottom.getAutomergePRs(), top.getAutomergePRs()))
        .automergePRsFrom(overlay(bottom.getAutomergePRsFrom(), top.getAutomergePRsFrom()))
        .build();
  }

  /**
   * Reverses the overlay to retrieve on;y the top argument. Assumes that if any
   * field is equal between both arguments, it was not set on the top argument and
   * the field will be set to null
   */
  Config reverseOverlayConfig(Config bottom, Config top) {
    return Config.builder()
        .chooseRequiredReviewers(top.getChooseRequiredReviewers() == null ? bottom.getChooseRequiredReviewers() : top.getChooseRequiredReviewers())
        .requiredReviews(Objects.equals(top.getRequiredReviews(), bottom.getRequiredReviews()) ? null : top.getRequiredReviews())
        .requiredReviewers(reverseOverlay(bottom.getRequiredReviewers(), top.getRequiredReviewers()))
        .requiredReviewerGroups(reverseOverlay(bottom.getRequiredReviewerGroups(), top.getRequiredReviewerGroups()))
        .defaultReviewers(reverseOverlay(bottom.getDefaultReviewers(), top.getDefaultReviewers()))
        .defaultReviewerGroups(reverseOverlay(bottom.getDefaultReviewerGroups(), top.getDefaultReviewerGroups()))
        .excludedUsers(reverseOverlay(bottom.getExcludedUsers(), top.getExcludedUsers()))
        .excludedGroups(reverseOverlay(bottom.getExcludedGroups(), top.getExcludedGroups()))
        .blockedCommits(reverseOverlay(bottom.getBlockedCommits(), top.getBlockedCommits()))
        .blockedPRs(reverseOverlay(bottom.getBlockedPRs(), top.getBlockedPRs()))
        .automergePRs(reverseOverlay(bottom.getAutomergePRs(), top.getAutomergePRs()))
        .automergePRsFrom(reverseOverlay(bottom.getAutomergePRsFrom(), top.getAutomergePRsFrom()))
        .build();
  }

  /**
   * Overlays the top list on the bottom list. If the top list has items, it is used.
   * Otherwise, the bottom list is used.
   */
  <E> List<E> overlay(List<E> bottom, List<E> top) {
    return top.size() > 0 ? top : bottom;
  }

  /**
   * Reverse an existing overlay of the top list from the bottom list. If the two are
   * identical, it is assumed that the top item had no value. Otherwise the top value
   * is used.
   */
  <E> List<E> reverseOverlay(List<E> bottom, List<E> top) {
    return top.equals(bottom) ? null : top;
  }

  Integer parseInt(String str) {
    return str == null ? null : Integer.parseInt(str);
  }

  Boolean parseBoolean(String str) {
    return str == null ? null : Boolean.parseBoolean(str);
  }

  String toString(Integer integer) {
    return integer == null ? null : Integer.toString(integer);
  }

  String toString(Boolean bool) {
    return bool == null ? null : Boolean.toString(bool);
  }

  PluginSettings repoSettings(String projectKey, String repoSlug) {
    return pluginSettingsFactory.createSettingsForKey(projectKey + "-" + repoSlug);
  }

  PluginSettings projectSettings(String projectKey) {
    return pluginSettingsFactory.createSettingsForKey(projectKey);
  }

  String get(PluginSettings settings, String key, String defaultValue) {
    String val = (String) settings.get(key);
    return val == null ? defaultValue : val;
  }

  String join(Iterable<String> values, Predicate<String> predicate) {
    return values == null ? null : Joiner.on(", ").join(filter(values, predicate));
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

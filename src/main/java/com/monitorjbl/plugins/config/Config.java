package com.monitorjbl.plugins.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Config {
  private List<String> defaultReviewers = newArrayList();
  private List<String> defaultReviewerGroups = newArrayList();
  private List<String> requiredReviewers = newArrayList();
  private List<String> requiredReviewerGroups = newArrayList();
  private Integer requiredReviews = 0;
  private List<String> blockedCommits = newArrayList();
  private List<String> blockedPRs = newArrayList();
  private List<String> automergePRs = newArrayList();
  private List<String> automergePRsFrom = newArrayList();
  private List<String> excludedUsers = newArrayList();
  private List<String> excludedGroups = newArrayList();

  public Config() {
  }

  private Config(Builder builder) {
    setDefaultReviewers(builder.defaultReviewers);
    setDefaultReviewerGroups(builder.defaultReviewerGroups);
    setRequiredReviewers(builder.requiredReviewers);
    setRequiredReviewerGroups(builder.requiredReviewerGroups);
    setRequiredReviews(builder.requiredReviews);
    setBlockedCommits(builder.blockedCommits);
    setBlockedPRs(builder.blockedPRs);
    setAutomergePRs(builder.automergePRs);
    setAutomergePRsFrom(builder.automergePRsFrom);
    setExcludedUsers(builder.excludedUsers);
    setExcludedGroups(builder.excludedGroups);
  }

  public List<String> getDefaultReviewers() {
    return defaultReviewers;
  }

  public void setDefaultReviewers(List<String> defaultReviewers) {
    this.defaultReviewers = defaultReviewers;
  }

  public List<String> getDefaultReviewerGroups() {
    return defaultReviewerGroups;
  }

  public void setDefaultReviewerGroups(List<String> defaultReviewerGroups) {
    this.defaultReviewerGroups = defaultReviewerGroups;
  }

  public List<String> getRequiredReviewers() {
    return requiredReviewers;
  }

  public void setRequiredReviewers(List<String> requiredReviewers) {
    this.requiredReviewers = requiredReviewers;
  }

  public List<String> getRequiredReviewerGroups() {
    return requiredReviewerGroups;
  }

  public void setRequiredReviewerGroups(List<String> requiredReviewerGroups) {
    this.requiredReviewerGroups = requiredReviewerGroups;
  }

  public Integer getRequiredReviews() {
    return requiredReviews;
  }

  public void setRequiredReviews(Integer requiredReviews) {
    this.requiredReviews = requiredReviews;
  }

  public List<String> getBlockedCommits() {
    return blockedCommits;
  }

  public void setBlockedCommits(List<String> blockedCommits) {
    this.blockedCommits = blockedCommits;
  }

  public List<String> getBlockedPRs() {
    return blockedPRs;
  }

  public void setBlockedPRs(List<String> blockedPRs) {
    this.blockedPRs = blockedPRs;
  }

  public List<String> getAutomergePRs() {
    return automergePRs;
  }

  public void setAutomergePRs(List<String> automergePRs) {
    this.automergePRs = automergePRs;
  }

  public List<String> getAutomergePRsFrom() {
    return automergePRsFrom;
  }

  public void setAutomergePRsFrom(List<String> automergePRsFrom) {
    this.automergePRsFrom = automergePRsFrom;
  }

  public List<String> getExcludedUsers() {
    return excludedUsers;
  }

  public void setExcludedUsers(List<String> excludedUsers) {
    this.excludedUsers = excludedUsers;
  }

  public List<String> getExcludedGroups() {
    return excludedGroups;
  }

  public void setExcludedGroups(List<String> excludedGroups) {
    this.excludedGroups = excludedGroups;
  }

  public Builder copyBuilder() {
    return new Builder(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private List<String> defaultReviewers = newArrayList();
    private List<String> defaultReviewerGroups = newArrayList();
    private List<String> requiredReviewers = newArrayList();
    private List<String> requiredReviewerGroups = newArrayList();
    private Integer requiredReviews = 0;
    private List<String> blockedCommits = newArrayList();
    private List<String> blockedPRs = newArrayList();
    private List<String> automergePRs = newArrayList();
    private List<String> automergePRsFrom = newArrayList();
    private List<String> excludedUsers = newArrayList();
    private List<String> excludedGroups = newArrayList();

    private Builder() {
    }

    private Builder(Config copy) {
      defaultReviewers = newArrayList(copy.defaultReviewers);
      defaultReviewerGroups = newArrayList(copy.defaultReviewerGroups);
      requiredReviewers = newArrayList(copy.requiredReviewers);
      requiredReviewerGroups = newArrayList(copy.requiredReviewerGroups);
      requiredReviews = copy.requiredReviews;
      blockedCommits = newArrayList(copy.blockedCommits);
      blockedPRs = newArrayList(copy.blockedPRs);
      automergePRs = newArrayList(copy.automergePRs);
      automergePRsFrom = newArrayList(copy.automergePRsFrom);
      excludedUsers = newArrayList(copy.excludedUsers);
      excludedGroups = newArrayList(copy.excludedGroups);
    }

    public Builder defaultReviewers(List<String> defaultReviewers) {
      this.defaultReviewers = defaultReviewers;
      return this;
    }

    public Builder defaultReviewerGroups(List<String> defaultReviewerGroups) {
      this.defaultReviewerGroups = defaultReviewerGroups;
      return this;
    }

    public Builder requiredReviewers(List<String> requiredReviewers) {
      this.requiredReviewers = requiredReviewers;
      return this;
    }

    public Builder requiredReviewerGroups(List<String> requiredReviewerGroups) {
      this.requiredReviewerGroups = requiredReviewerGroups;
      return this;
    }

    public Builder requiredReviews(Integer requiredReviews) {
      this.requiredReviews = requiredReviews;
      return this;
    }

    public Builder blockedCommits(List<String> blockedCommits) {
      this.blockedCommits = blockedCommits;
      return this;
    }

    public Builder blockedPRs(List<String> blockedPRs) {
      this.blockedPRs = blockedPRs;
      return this;
    }

    public Builder automergePRs(List<String> automergePRs) {
      this.automergePRs = automergePRs;
      return this;
    }

    public Builder automergePRsFrom(List<String> automergePRsFrom) {
      this.automergePRsFrom = automergePRsFrom;
      return this;
    }

    public Builder excludedUsers(List<String> excludedUsers) {
      this.excludedUsers = excludedUsers;
      return this;
    }

    public Builder excludedGroups(List<String> excludedGroups) {
      this.excludedGroups = excludedGroups;
      return this;
    }

    public Config build() {
      return new Config(this);
    }
  }
}

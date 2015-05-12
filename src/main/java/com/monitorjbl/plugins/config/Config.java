package com.monitorjbl.plugins.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Config {
  private List<String> defaultReviewers;
  private List<String> requiredReviewers;
  private Integer requiredReviews;
  private List<String> blockedCommits;
  private List<String> blockedPRs;
  private List<String> automergePRs;
  private List<String> excludedUsers;

  public Config() {
  }

  private Config(Builder builder) {
    setDefaultReviewers(builder.defaultReviewers);
    setRequiredReviewers(builder.requiredReviewers);
    setRequiredReviews(builder.requiredReviews);
    setBlockedCommits(builder.blockedCommits);
    setBlockedPRs(builder.blockedPRs);
    setExcludedUsers(builder.excludedUsers);
    setAutomergePRs(builder.automergePRs);
  }

  public List<String> getDefaultReviewers() {
    return defaultReviewers;
  }

  public void setDefaultReviewers(List<String> defaultReviewers) {
    this.defaultReviewers = defaultReviewers;
  }

  public List<String> getRequiredReviewers() {
    return requiredReviewers;
  }

  public void setRequiredReviewers(List<String> requiredReviewers) {
    this.requiredReviewers = requiredReviewers;
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

  public List<String> getExcludedUsers() {
    return excludedUsers;
  }

  public void setExcludedUsers(List<String> excludedUsers) {
    this.excludedUsers = excludedUsers;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private List<String> defaultReviewers = newArrayList();
    private List<String> requiredReviewers = newArrayList();
    private Integer requiredReviews = 0;
    private List<String> blockedCommits = newArrayList();
    private List<String> blockedPRs = newArrayList();
    private List<String> automergePRs = newArrayList();
    private List<String> excludedUsers = newArrayList();

    private Builder() {
    }

    public Builder defaultReviewers(List<String> defaultReviewers) {
      this.defaultReviewers = defaultReviewers;
      return this;
    }

    public Builder requiredReviewers(List<String> requiredReviewers) {
      this.requiredReviewers = requiredReviewers;
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

    public Builder excludedUsers(List<String> excludedUsers) {
      this.excludedUsers = excludedUsers;
      return this;
    }

    public Config build() {
      return new Config(this);
    }
  }
}

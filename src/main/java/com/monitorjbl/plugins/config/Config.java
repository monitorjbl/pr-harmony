package com.monitorjbl.plugins.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Config {
  private List<String> defaultReviewers;
  private List<String> requiredReviewers;
  private Double requiredReviews;
  private List<String> blockedCommits;
  private List<String> blockedPRs;
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

  public Double getRequiredReviews() {
    return requiredReviews;
  }

  public void setRequiredReviews(Double requiredReviews) {
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
    private List<String> defaultReviewers;
    private List<String> requiredReviewers;
    private Double requiredReviews;
    private List<String> blockedCommits;
    private List<String> blockedPRs;
    private List<String> excludedUsers;

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

    public Builder requiredReviews(Double requiredReviews) {
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

    public Builder excludedUsers(List<String> excludedUsers) {
      this.excludedUsers = excludedUsers;
      return this;
    }

    public Config build() {
      return new Config(this);
    }
  }
}

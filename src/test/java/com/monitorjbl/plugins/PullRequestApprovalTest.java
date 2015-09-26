package com.monitorjbl.plugins;

import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.pull.PullRequestParticipant;
import com.google.common.collect.Sets;
import com.monitorjbl.plugins.config.Config;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.monitorjbl.plugins.TestUtils.mockParticipant;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class PullRequestApprovalTest {
  @Mock
  PullRequest pr;
  @Mock
  UserUtils utils;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    PullRequestParticipant author = mockParticipant("author", false);
    when(pr.getAuthor()).thenReturn(author);
  }

  @Test
  public void testDefaultConfiguration_withRevieiwers() {
    Set<PullRequestParticipant> p = newHashSet(
        mockParticipant("user1", false)
    );
    when(pr.getReviewers()).thenReturn(p);
    assertThat(new PullRequestApproval(Config.builder().build(), utils).isPullRequestApproved(pr), is(true));
  }

  @Test
  public void testDefaultConfiguration_noRevieiwers() {
    when(pr.getReviewers()).thenReturn(Sets.<PullRequestParticipant>newHashSet());
    assertThat(new PullRequestApproval(Config.builder().build(), utils).isPullRequestApproved(pr), is(true));
  }

  @Test
  public void testSingleApprover_notApproved() {
    Set<PullRequestParticipant> p = newHashSet(
        mockParticipant("user1", false)
    );
    when(pr.getReviewers()).thenReturn(p);
    assertThat(new PullRequestApproval(Config.builder()
        .requiredReviewers(newArrayList("user1"))
        .requiredReviews(1)
        .build(),
        utils).isPullRequestApproved(pr), is(false));
  }

  @Test
  public void testSingleApprover_approved() {
    Set<PullRequestParticipant> p = newHashSet(
        mockParticipant("user1", true)
    );
    when(pr.getReviewers()).thenReturn(p);
    assertThat(new PullRequestApproval(Config.builder()
        .requiredReviewers(newArrayList("user1"))
        .requiredReviews(1)
        .build(),
        utils).isPullRequestApproved(pr), is(true));
  }

  @Test
  public void testSingleApprover_submitterIsApprover() {
    PullRequestParticipant author = mockParticipant("user1", false);
    when(pr.getReviewers()).thenReturn(Sets.<PullRequestParticipant>newHashSet());
    when(pr.getAuthor()).thenReturn(author);
    assertThat(new PullRequestApproval(Config.builder()
        .requiredReviewers(newArrayList("user1"))
        .requiredReviews(1)
        .build(),
        utils).isPullRequestApproved(pr), is(true));
  }

  @Test
  public void testMultipleApprovers_approved() {
    Set<PullRequestParticipant> p = newHashSet(
        mockParticipant("user1", true),
        mockParticipant("user2", true)
    );
    when(pr.getReviewers()).thenReturn(p);
    assertThat(new PullRequestApproval(Config.builder()
        .requiredReviewers(newArrayList("user1", "user2"))
        .requiredReviews(2)
        .build(),
        utils).isPullRequestApproved(pr), is(true));
  }

  @Test
  public void testMultipleApprovers_oneNotApproved() {
    Set<PullRequestParticipant> p = newHashSet(
        mockParticipant("user1", true),
        mockParticipant("user2", false)
    );
    when(pr.getReviewers()).thenReturn(p);
    assertThat(new PullRequestApproval(Config.builder()
        .requiredReviewers(newArrayList("user1", "user2"))
        .requiredReviews(2)
        .build(),
        utils).isPullRequestApproved(pr), is(false));
  }

  @Test
  public void testMultipleApprovers_submitterIsApprover_matchingNumberOfApprovers() {
    Set<PullRequestParticipant> p = newHashSet(
        mockParticipant("user2", true)
    );
    PullRequestParticipant author = mockParticipant("user1", false);
    when(pr.getReviewers()).thenReturn(p);
    when(pr.getAuthor()).thenReturn(author);
    assertThat(new PullRequestApproval(Config.builder()
        .requiredReviewers(newArrayList("user1", "user2"))
        .requiredReviews(2)
        .build(),
        utils).isPullRequestApproved(pr), is(true));
  }

  @Test
  public void testMultipleApprovers_submitterIsApprover_notEnoughApprovals() {
    Set<PullRequestParticipant> p = newHashSet(
        mockParticipant("user2", true),
        mockParticipant("user3", false)
    );
    PullRequestParticipant author = mockParticipant("user1", false);
    when(pr.getReviewers()).thenReturn(p);
    when(pr.getAuthor()).thenReturn(author);
    assertThat(new PullRequestApproval(Config.builder()
        .requiredReviewers(newArrayList("user1", "user2", "user3"))
        .requiredReviews(2)
        .build(),
        utils).isPullRequestApproved(pr), is(false));
  }

  @Test
  public void testMultipleApprovers_submitterIsApprover_approved() {
    Set<PullRequestParticipant> p = newHashSet(
        mockParticipant("user2", true),
        mockParticipant("user3", true)
    );
    PullRequestParticipant author = mockParticipant("user1", false);
    when(pr.getReviewers()).thenReturn(p);
    when(pr.getAuthor()).thenReturn(author);
    assertThat(new PullRequestApproval(Config.builder()
        .requiredReviewers(newArrayList("user1", "user2", "user3"))
        .requiredReviews(2)
        .build(),
        utils).isPullRequestApproved(pr), is(true));
  }

  @Test
  public void testMissingReviewers_oneMissing() {
    Set<PullRequestParticipant> p = newHashSet(
        mockParticipant("user1", false)
    );
    when(pr.getReviewers()).thenReturn(p);
    assertThat(new PullRequestApproval(Config.builder()
        .requiredReviewers(newArrayList("user1"))
        .requiredReviews(1)
        .build(),
        utils).missingRevieiwers(pr).size(), is(1));
  }

  @Test
  public void testMissingReviewers_twoOutOfThreeMissing() {
    Set<PullRequestParticipant> p = newHashSet(
        mockParticipant("user1", false),
        mockParticipant("user2", false),
        mockParticipant("user3", true)
    );
    when(pr.getReviewers()).thenReturn(p);
    assertThat(new PullRequestApproval(Config.builder()
        .requiredReviewers(newArrayList("user1", "user2", "user3"))
        .requiredReviews(1)
        .build(),
        utils).missingRevieiwers(pr).size(), is(2));
  }

  @Test
  public void testMissingReviewers_zeroMissing() {
    Set<PullRequestParticipant> p = newHashSet(
        mockParticipant("user1", true),
        mockParticipant("user2", true),
        mockParticipant("user3", true)
    );
    when(pr.getReviewers()).thenReturn(p);
    assertThat(new PullRequestApproval(Config.builder()
        .requiredReviewers(newArrayList("user1", "user2", "user3"))
        .requiredReviews(1)
        .build(),
        utils).missingRevieiwers(pr).size(), is(0));
  }

  @Test
  public void testSeenRevieiwers_twoOutOfThree() {
    Set<PullRequestParticipant> p = newHashSet(
        mockParticipant("user1", true),
        mockParticipant("user2", false),
        mockParticipant("user3", true)
    );
    when(pr.getReviewers()).thenReturn(p);

    Set<String> seen = new PullRequestApproval(Config.builder()
        .requiredReviewers(newArrayList("user1", "user2", "user3"))
        .requiredReviews(1)
        .build(),
        utils).seenReviewers(pr);
    assertThat(seen.size(), is(2));
    assertThat(seen, contains("user1", "user3"));
  }

  @Test
  public void testGroupRequiredReviewers_mixed() throws Exception {
    Set<PullRequestParticipant> p = newHashSet(
        mockParticipant("user1", true),
        mockParticipant("user2", true),
        mockParticipant("user3", true)
    );
    when(pr.getReviewers()).thenReturn(p);
    when(utils.dereferenceGroups(newArrayList("group1"))).thenReturn(newArrayList("user2", "user3"));

    Set<String> seen = new PullRequestApproval(Config.builder()
        .requiredReviewers(newArrayList("user1"))
        .requiredReviewerGroups(newArrayList("group1"))
        .requiredReviews(1)
        .build(),
        utils).seenReviewers(pr);

    assertThat(seen.size(), is(3));
    assertThat(seen, hasItem("user1"));
    assertThat(seen, hasItem("user2"));
    assertThat(seen, hasItem("user3"));
  }

  @Test
  public void testGroupRequiredReviewers_groupOnly() throws Exception {
    Set<PullRequestParticipant> p = newHashSet(
        mockParticipant("user1", true),
        mockParticipant("user2", true),
        mockParticipant("user3", true)
    );
    when(pr.getReviewers()).thenReturn(p);
    when(utils.dereferenceGroups(newArrayList("group1"))).thenReturn(newArrayList("user2", "user3"));

    Set<String> seen = new PullRequestApproval(Config.builder()
        .requiredReviewerGroups(newArrayList("group1"))
        .requiredReviews(1)
        .build(),
        utils).seenReviewers(pr);

    assertThat(seen.size(), is(2));
    assertThat(seen, hasItem("user2"));
    assertThat(seen, hasItem("user3"));
  }

  @Test
  public void testGroupRequiredReviewers_multipleGroups() throws Exception {
    Set<PullRequestParticipant> p = newHashSet(
        mockParticipant("user1", true),
        mockParticipant("user2", true),
        mockParticipant("user3", true)
    );
    when(pr.getReviewers()).thenReturn(p);
    when(utils.dereferenceGroups(newArrayList("group1", "group2"))).thenReturn(newArrayList("user2", "user3"));

    Set<String> seen = new PullRequestApproval(Config.builder()
        .requiredReviewerGroups(newArrayList("group1", "group2"))
        .requiredReviews(1)
        .build(),
        utils).seenReviewers(pr);

    assertThat(seen.size(), is(2));
    assertThat(seen, hasItem("user2"));
    assertThat(seen, hasItem("user3"));
  }
}

package com.monitorjbl.plugins;

import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestParticipant;
import com.atlassian.bitbucket.pull.PullRequestParticipantStatus;
import com.atlassian.bitbucket.pull.PullRequestRef;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.scm.pull.MergeRequest;
import com.monitorjbl.plugins.config.Config;
import com.monitorjbl.plugins.config.ConfigDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.monitorjbl.plugins.TestUtils.mockParticipant;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MergeBlockerTest {
  @Mock
  private ConfigDao configDao;
  @InjectMocks
  private MergeBlocker mergeBlocker;
  @Spy
  @SuppressWarnings("unused") //Used by @InjectMocks
  private RegexUtils regexUtils;
  @Mock
  private UserUtils userUtils;

  @Mock
  private MergeRequest merge;
  @Mock
  private PullRequest pr;
  @Mock
  private Project project;
  @Mock
  private PullRequestRef ref;
  @Mock
  private Repository repository;

  @Before
  public void setUp() {
    when(merge.getPullRequest()).thenReturn(pr);
    when(pr.getToRef()).thenReturn(ref);
    when(ref.getRepository()).thenReturn(repository);
    when(ref.getDisplayId()).thenReturn("master");
    when(repository.getProject()).thenReturn(project);
    when(repository.getSlug()).thenReturn("repo_1");
    when(project.getKey()).thenReturn("PRJ");
    when(userUtils.dereferenceGroups(anyListOf(String.class))).thenReturn(Collections.emptySet());
    when(userUtils.getUserDisplayNameByName(Mockito.eq("user1"))).thenReturn("First user");
    when(userUtils.getUserDisplayNameByName(Mockito.eq("user2"))).thenReturn("Second user");
  }

  @Test
  public void testBlocking_blocked() {
    when(configDao.getConfigForRepo(project.getKey(), repository.getSlug())).thenReturn(Config.builder()
        .blockedPRs(newArrayList("master"))
        .build());
    mergeBlocker.check(merge);
    verify(merge, times(1)).veto(anyString(), anyString());
  }

  @Test
  public void testBlocking_notBlocked() {
    when(configDao.getConfigForRepo(project.getKey(), repository.getSlug())).thenReturn(Config.builder()
        .blockedPRs(newArrayList("bugfix"))
        .build());
    mergeBlocker.check(merge);
    verify(merge, never()).veto(anyString(), anyString());
  }

  @Test
  public void testBlocking_missingRequiredReviewer() {
    Set<PullRequestParticipant> p = newHashSet(
        mockParticipant("user1", false)
    );
    PullRequestParticipant author = mockParticipant("author", false);
    when(pr.getReviewers()).thenReturn(p);
    when(pr.getAuthor()).thenReturn(author);
    when(configDao.getConfigForRepo(project.getKey(), repository.getSlug())).thenReturn(Config.builder()
        .blockedPRs(newArrayList("bugfix"))
        .requiredReviewers(newArrayList("user1"))
        .requiredReviews(1)
        .build());
    mergeBlocker.check(merge);
    verify(merge, times(1)).veto(anyString(), anyString());
  }

  @Test
  public void testBlocking_reviewerIsAuthor_notEnoughApprovals() {
    Set<PullRequestParticipant> p = newHashSet(
        mockParticipant("user2", false)
    );
    PullRequestParticipant author = mockParticipant("user1", false);
    when(pr.getReviewers()).thenReturn(p);
    when(pr.getAuthor()).thenReturn(author);
    when(configDao.getConfigForRepo(project.getKey(), repository.getSlug())).thenReturn(Config.builder()
        .blockedPRs(newArrayList("bugfix"))
        .requiredReviewers(newArrayList("user1", "user2"))
        .requiredReviews(1)
        .build());
    mergeBlocker.check(merge);
    verify(merge, times(1)).veto(anyString(), anyString());
  }

  @Test
  public void testBlocking_reviewerIsAuthor_matchingNumberOfApprovals() {
    Set<PullRequestParticipant> p = newHashSet(
        mockParticipant("user2", true)
    );
    PullRequestParticipant author = mockParticipant("user1", false);
    when(pr.getReviewers()).thenReturn(p);
    when(pr.getAuthor()).thenReturn(author);
    when(configDao.getConfigForRepo(project.getKey(), repository.getSlug())).thenReturn(Config.builder()
        .blockedPRs(newArrayList("bugfix"))
        .requiredReviewers(newArrayList("user1", "user2"))
        .requiredReviews(2)
        .build());
    mergeBlocker.check(merge);
    verify(merge, never()).veto(anyString(), anyString());
  }

  @Test
  public void testBlocking_reviewerIsAuthor_approved() {
    Set<PullRequestParticipant> p = newHashSet(
        mockParticipant("user2", true)
    );
    PullRequestParticipant author = mockParticipant("user1", false);
    when(pr.getReviewers()).thenReturn(p);
    when(pr.getAuthor()).thenReturn(author);
    when(configDao.getConfigForRepo(project.getKey(), repository.getSlug())).thenReturn(Config.builder()
        .blockedPRs(newArrayList("bugfix"))
        .requiredReviewers(newArrayList("user2"))
        .requiredReviews(1)
        .build());
    mergeBlocker.check(merge);
    verify(merge, never()).veto(anyString(), anyString());
  }

  @Test
  public void testBlocking_prNeedsWorkEnabledAndOneNeedsWorkSet() {
    final PullRequestParticipant reviewer = mockParticipant("user2", true);
    when(reviewer.getStatus()).thenReturn(PullRequestParticipantStatus.NEEDS_WORK);
    Set<PullRequestParticipant> p = newHashSet(
            reviewer
    );
    PullRequestParticipant author = mockParticipant("user1", false);
    when(pr.getReviewers()).thenReturn(p);
    when(pr.getParticipants()).thenReturn(p);
    when(pr.getAuthor()).thenReturn(author);
    when(configDao.getConfigForRepo(project.getKey(), repository.getSlug())).thenReturn(Config.builder()
            .blockMergeIfPrNeedsWork(true)
            .build());
    mergeBlocker.check(merge);
    verify(merge).veto(anyString(), anyString());
  }

  @Test
  public void testBlocking_prNeedsWorkEnabledAndNoNeedsWorkSet() {
    final PullRequestParticipant reviewer = mockParticipant("user2", true);
    when(reviewer.getStatus()).thenReturn(PullRequestParticipantStatus.APPROVED);
    Set<PullRequestParticipant> p = newHashSet(
            reviewer
    );
    PullRequestParticipant author = mockParticipant("user1", false);
    when(pr.getReviewers()).thenReturn(p);
    when(pr.getParticipants()).thenReturn(p);
    when(pr.getAuthor()).thenReturn(author);
    when(configDao.getConfigForRepo(project.getKey(), repository.getSlug())).thenReturn(Config.builder()
            .blockMergeIfPrNeedsWork(true)
            .build());
    mergeBlocker.check(merge);
    verify(merge, never()).veto(anyString(), anyString());
  }

  @Test
  public void testBlocking_prNeedsWorkDisabledAndOneNeedsWorkSet() {
    final PullRequestParticipant reviewer = mockParticipant("user2", true);
    when(reviewer.getStatus()).thenReturn(PullRequestParticipantStatus.NEEDS_WORK);
    Set<PullRequestParticipant> p = newHashSet(
            reviewer
    );
    PullRequestParticipant author = mockParticipant("user1", false);
    when(pr.getReviewers()).thenReturn(p);
    when(pr.getParticipants()).thenReturn(p);
    when(pr.getAuthor()).thenReturn(author);
    when(configDao.getConfigForRepo(project.getKey(), repository.getSlug())).thenReturn(Config.builder()
            .blockMergeIfPrNeedsWork(false)
            .build());
    mergeBlocker.check(merge);
    verify(merge, never()).veto(anyString(), anyString());
  }
}

package com.monitorjbl.plugins;

import com.atlassian.stash.project.Project;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.pull.PullRequestParticipant;
import com.atlassian.stash.pull.PullRequestRef;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.scm.pull.MergeRequest;
import com.google.common.collect.Lists;
import com.monitorjbl.plugins.config.Config;
import com.monitorjbl.plugins.config.ConfigDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.monitorjbl.plugins.TestUtils.mockParticipant;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MergeBlockerTest {
  @Mock
  private ConfigDao configDao;
  @Mock
  private UserUtils utils;
  @InjectMocks
  private MergeBlocker sut;

  @Mock
  MergeRequest merge;
  @Mock
  PullRequest pr;
  @Mock
  Repository repository;
  @Mock
  Project project;
  @Mock
  PullRequestRef ref;


  @Before
  @SuppressWarnings("unchecked")
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(merge.getPullRequest()).thenReturn(pr);
    when(pr.getToRef()).thenReturn(ref);
    when(ref.getRepository()).thenReturn(repository);
    when(ref.getId()).thenReturn(MergeBlocker.REFS_PREFIX + "master");
    when(repository.getProject()).thenReturn(project);
    when(repository.getSlug()).thenReturn("repo_1");
    when(project.getKey()).thenReturn("PRJ");
    when(utils.dereferenceGroups(anyList())).thenReturn(Lists.<String>newArrayList());
  }

  @Test
  public void testBlocking_blocked() throws Exception {
    when(configDao.getConfigForRepo(project.getKey(), repository.getSlug())).thenReturn(Config.builder()
        .blockedPRs(newArrayList("master"))
        .build());
    sut.check(merge);
    verify(merge, times(1)).veto(anyString(), anyString());
  }

  @Test
  public void testBlocking_notBlocked() throws Exception {
    when(configDao.getConfigForRepo(project.getKey(), repository.getSlug())).thenReturn(Config.builder()
        .blockedPRs(newArrayList("bugfix"))
        .build());
    sut.check(merge);
    verify(merge, never()).veto(anyString(), anyString());
  }

  @Test
  public void testBlocking_missingRequiredReviewer() throws Exception {
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
    sut.check(merge);
    verify(merge, times(1)).veto(anyString(), anyString());
  }

  @Test
  public void testBlocking_reviewerIsAuthor() throws Exception {
    Set<PullRequestParticipant> p = newHashSet(
        mockParticipant("user1", false)
    );
    PullRequestParticipant author = mockParticipant("user1", false);
    when(pr.getReviewers()).thenReturn(p);
    when(pr.getAuthor()).thenReturn(author);
    when(configDao.getConfigForRepo(project.getKey(), repository.getSlug())).thenReturn(Config.builder()
        .blockedPRs(newArrayList("bugfix"))
        .requiredReviewers(newArrayList("user1"))
        .requiredReviews(1)
        .build());
    sut.check(merge);
    verify(merge, never()).veto(anyString(), anyString());
  }
}

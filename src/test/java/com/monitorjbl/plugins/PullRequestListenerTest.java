package com.monitorjbl.plugins;

import com.atlassian.bitbucket.event.pull.PullRequestOpenedEvent;
import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestMergeRequest;
import com.atlassian.bitbucket.pull.PullRequestMergeability;
import com.atlassian.bitbucket.pull.PullRequestParticipant;
import com.atlassian.bitbucket.pull.PullRequestRef;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.bitbucket.user.DummySecurityService;
import com.atlassian.bitbucket.user.SecurityService;
import com.monitorjbl.plugins.config.Config;
import com.monitorjbl.plugins.config.ConfigDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PullRequestListenerTest {
  @Mock
  private ConfigDao configDao;
  @InjectMocks
  private PullRequestListener listener;
  @Mock
  private PullRequestService prService;
  @Spy
  @SuppressWarnings("unused") //Used by @InjectMocks
  private RegexUtils regexUtils;
  @Spy
  @SuppressWarnings("unused") //Used by @InjectMocks
  private SecurityService securityService = new DummySecurityService();
  @Mock
  private UserUtils userUtils;

  @Mock
  private PullRequestParticipant author;
  @Mock
  private ApplicationUser authorUser;
  @Mock
  private PullRequestRef fromRef;
  @Mock
  private Repository fromRepo;
  @Mock
  private PullRequestMergeability mergeability;
  @Mock
  private PullRequestOpenedEvent openedEvent;
  @Mock
  private PullRequest pr;
  @Mock
  private Project project;
  @Mock
  private PullRequestRef toRef;
  @Mock
  private Repository toRepo;

  @Before
  public void setUp() {
    when(openedEvent.getPullRequest()).thenReturn(pr);
    when(authorUser.getSlug()).thenReturn("someguy");
    when(author.getUser()).thenReturn(authorUser);
    when(pr.getToRef()).thenReturn(toRef);
    when(pr.getFromRef()).thenReturn(fromRef);
    when(pr.getId()).thenReturn(10L);
    when(pr.getVersion()).thenReturn(10384);
    when(pr.getAuthor()).thenReturn(author);
    when(toRef.getRepository()).thenReturn(toRepo);
    when(toRef.getDisplayId()).thenReturn("master");
    when(fromRef.getRepository()).thenReturn(fromRepo);
    when(fromRef.getDisplayId()).thenReturn("otherMaster");
    when(toRepo.getProject()).thenReturn(project);
    when(toRepo.getId()).thenReturn(20);
    when(toRepo.getSlug()).thenReturn("repo_1");
    when(fromRepo.getProject()).thenReturn(project);
    when(fromRepo.getId()).thenReturn(30);
    when(fromRepo.getSlug()).thenReturn("repo_2");
    when(project.getKey()).thenReturn("PRJ");
    when(userUtils.dereferenceGroups(anyListOf(String.class))).thenReturn(Collections.emptySet());
  }

  @Test
  public void testAutomerge_defaultConfig() {
    when(configDao.getConfigForRepo(project.getKey(), toRepo.getSlug())).thenReturn(Config.builder().build());
    listener.automergePullRequest(pr);
    verify(prService, never()).merge(any(PullRequestMergeRequest.class));
  }

  @Test
  public void testAutomerge_blockedBranches() {
    when(configDao.getConfigForRepo(project.getKey(), toRepo.getSlug())).thenReturn(Config.builder()
        .automergePRs(newArrayList("master"))
        .blockedPRs(newArrayList("master"))
        .build());
    listener.automergePullRequest(pr);
    verify(prService, never()).merge(any(PullRequestMergeRequest.class));
  }

  @Test
  public void testAutomerge_canMerge() {
    when(mergeability.canMerge()).thenReturn(true);
    when(prService.canMerge(toRepo.getId(), pr.getId())).thenReturn(mergeability);
    when(configDao.getConfigForRepo(project.getKey(), toRepo.getSlug())).thenReturn(Config.builder()
        .automergePRs(newArrayList("master"))
        .requiredReviewers(newArrayList("user1"))
        .requiredReviews(1)
        .build());
    listener.automergePullRequest(pr);
    verify(prService, times(1)).merge(any(PullRequestMergeRequest.class));
  }

  @Test
  public void testAutomerge_cannotMerge() {
    when(mergeability.canMerge()).thenReturn(false);
    when(prService.canMerge(toRepo.getId(), pr.getId())).thenReturn(mergeability);
    when(configDao.getConfigForRepo(project.getKey(), toRepo.getSlug())).thenReturn(Config.builder()
        .automergePRs(newArrayList("master"))
        .requiredReviewers(newArrayList("user1"))
        .requiredReviews(1)
        .build());
    listener.automergePullRequest(pr);
    verify(prService, never()).merge(any(PullRequestMergeRequest.class));
  }
}

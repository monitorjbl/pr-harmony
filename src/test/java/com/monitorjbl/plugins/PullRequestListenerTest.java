package com.monitorjbl.plugins;

import com.atlassian.stash.event.pull.PullRequestApprovedEvent;
import com.atlassian.stash.event.pull.PullRequestOpenedEvent;
import com.atlassian.stash.project.Project;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.pull.PullRequestMergeability;
import com.atlassian.stash.pull.PullRequestParticipant;
import com.atlassian.stash.pull.PullRequestRef;
import com.atlassian.stash.pull.PullRequestRole;
import com.atlassian.stash.pull.PullRequestService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.user.EscalatedSecurityContext;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.SecurityService;
import com.atlassian.stash.util.Operation;
import com.google.common.collect.Sets;
import com.monitorjbl.plugins.config.Config;
import com.monitorjbl.plugins.config.ConfigDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.Nonnull;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.monitorjbl.plugins.Utils.mockParticipant;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PullRequestListenerTest {
  @Mock
  private ConfigDao configDao;
  @Mock
  private PullRequestService prService;
  @Mock
  private SecurityService securityService;
  @InjectMocks
  private PullRequestListener sut;

  @Mock
  PullRequestApprovedEvent approvedEvent;
  @Mock
  PullRequestOpenedEvent openedEvent;
  @Mock
  PullRequest pr;
  @Mock
  Repository repository;
  @Mock
  Project project;
  @Mock
  PullRequestRef ref;
  @Mock
  PullRequestMergeability mergeability;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(securityService.withPermission(any(Permission.class), anyString())).thenReturn(new MockSecurityContext());
    when(approvedEvent.getPullRequest()).thenReturn(pr);
    when(openedEvent.getPullRequest()).thenReturn(pr);
    when(pr.getToRef()).thenReturn(ref);
    when(pr.getId()).thenReturn(10L);
    when(pr.getVersion()).thenReturn(10384);
    when(ref.getRepository()).thenReturn(repository);
    when(ref.getId()).thenReturn(MergeBlocker.REFS_PREFIX + "master");
    when(repository.getProject()).thenReturn(project);
    when(repository.getId()).thenReturn(20);
    when(repository.getSlug()).thenReturn("repo_1");
    when(project.getKey()).thenReturn("PRJ");
  }

  @Test
  public void testAutomerge_defaultConfig() throws Exception {
    when(configDao.getConfigForRepo(project.getKey(), repository.getSlug())).thenReturn(Config.builder().build());
    sut.automergePullRequest(approvedEvent);
    verify(prService, never()).merge(repository.getId(), pr.getId(), pr.getVersion());
  }

  @Test
  public void testAutomerge_blockedBranches() throws Exception {
    when(configDao.getConfigForRepo(project.getKey(), repository.getSlug())).thenReturn(Config.builder()
        .automergePRs(newArrayList("master"))
        .blockedPRs(newArrayList("master"))
        .build());
    sut.automergePullRequest(approvedEvent);
    verify(prService, never()).merge(repository.getId(), pr.getId(), pr.getVersion());
  }

  @Test
  public void testAutomerge_canMerge() throws Exception {
    when(mergeability.canMerge()).thenReturn(true);
    when(prService.canMerge(repository.getId(), pr.getId())).thenReturn(mergeability);
    when(configDao.getConfigForRepo(project.getKey(), repository.getSlug())).thenReturn(Config.builder()
        .automergePRs(newArrayList("master"))
        .requiredReviewers(newArrayList("user1"))
        .requiredReviews(1)
        .build());
    sut.automergePullRequest(approvedEvent);
    verify(prService, times(1)).merge(repository.getId(), pr.getId(), pr.getVersion());
  }

  @Test
  public void testAutomerge_cannotMerge() throws Exception {
    when(mergeability.canMerge()).thenReturn(false);
    when(prService.canMerge(repository.getId(), pr.getId())).thenReturn(mergeability);
    when(configDao.getConfigForRepo(project.getKey(), repository.getSlug())).thenReturn(Config.builder()
        .automergePRs(newArrayList("master"))
        .requiredReviewers(newArrayList("user1"))
        .requiredReviews(1)
        .build());
    sut.automergePullRequest(approvedEvent);
    verify(prService, never()).merge(repository.getId(), pr.getId(), pr.getVersion());
  }

  @Test
  public void testDefaultReviewers_defaultConfig() throws Exception {
    Set<PullRequestParticipant> p = newHashSet(
        mockParticipant("user1", false)
    );
    PullRequestParticipant author = mockParticipant("author", false);
    when(pr.getReviewers()).thenReturn(p);
    when(pr.getAuthor()).thenReturn(author);
    when(configDao.getConfigForRepo(project.getKey(), repository.getSlug())).thenReturn(Config.builder().build());
    sut.populateDefaultReviewers(openedEvent);

    verify(prService, times(1)).assignRole(anyInt(), anyLong(), anyString(), any(PullRequestRole.class));
    verify(prService, times(1)).assignRole(repository.getId(), pr.getId(), "user1", PullRequestRole.REVIEWER);
  }

  @Test
  public void testDefaultReviewers_noReviewers() throws Exception {
    PullRequestParticipant author = mockParticipant("author", false);
    when(pr.getReviewers()).thenReturn(Sets.<PullRequestParticipant>newHashSet());
    when(pr.getAuthor()).thenReturn(author);
    when(configDao.getConfigForRepo(project.getKey(), repository.getSlug())).thenReturn(Config.builder()
        .defaultReviewers(newArrayList("user1", "user2"))
        .build());
    sut.populateDefaultReviewers(openedEvent);

    verify(prService, times(2)).assignRole(anyInt(), anyLong(), anyString(), any(PullRequestRole.class));
    verify(prService, times(1)).assignRole(repository.getId(), pr.getId(), "user1", PullRequestRole.REVIEWER);
    verify(prService, times(1)).assignRole(repository.getId(), pr.getId(), "user2", PullRequestRole.REVIEWER);
  }

  @Test
  public void testDefaultReviewers_addedReviewers() throws Exception {
    Set<PullRequestParticipant> p = newHashSet(
        mockParticipant("user1", false)
    );
    PullRequestParticipant author = mockParticipant("author", false);
    when(pr.getReviewers()).thenReturn(p);
    when(pr.getAuthor()).thenReturn(author);
    when(configDao.getConfigForRepo(project.getKey(), repository.getSlug())).thenReturn(Config.builder()
        .defaultReviewers(newArrayList("user2", "user3"))
        .build());
    sut.populateDefaultReviewers(openedEvent);

    verify(prService, times(3)).assignRole(anyInt(), anyLong(), anyString(), any(PullRequestRole.class));
    verify(prService, times(1)).assignRole(repository.getId(), pr.getId(), "user1", PullRequestRole.REVIEWER);
    verify(prService, times(1)).assignRole(repository.getId(), pr.getId(), "user2", PullRequestRole.REVIEWER);
    verify(prService, times(1)).assignRole(repository.getId(), pr.getId(), "user3", PullRequestRole.REVIEWER);
  }

  static class MockSecurityContext implements EscalatedSecurityContext {

    @Override
    public <T, E extends Throwable> T call(Operation<T, E> operation) throws E {
      operation.perform();
      return null;
    }

    @Nonnull
    @Override
    public EscalatedSecurityContext withPermission(Permission permission) {
      return null;
    }

    @Nonnull
    @Override
    public EscalatedSecurityContext withPermission(@Nonnull Object o, @Nonnull Permission permission) {
      return null;
    }

    @Nonnull
    @Override
    public EscalatedSecurityContext withPermissions(@Nonnull Set<Permission> set) {
      return null;
    }
  }
}

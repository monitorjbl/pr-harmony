package com.monitorjbl.plugins;

import com.atlassian.bitbucket.event.pull.PullRequestOpenedEvent;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestMergeRequest;
import com.atlassian.bitbucket.pull.PullRequestMergeability;
import com.atlassian.bitbucket.pull.PullRequestParticipant;
import com.atlassian.bitbucket.pull.PullRequestRef;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.bitbucket.user.EscalatedSecurityContext;
import com.atlassian.bitbucket.user.SecurityService;
import com.atlassian.bitbucket.util.Operation;
import com.google.common.collect.Lists;
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class PullRequestListenerTest {
  @Mock
  private ConfigDao configDao;
  @Mock
  private PullRequestService prService;
  @Mock
  private SecurityService securityService;
  @Mock
  private UserUtils userUtils;
  @Mock
  private RegexUtils regexUtils;
  @InjectMocks
  private PullRequestListener sut;

  @Mock
  PullRequestOpenedEvent openedEvent;
  @Mock
  PullRequest pr;
  @Mock
  Repository toRepo;
  @Mock
  Repository fromRepo;
  @Mock
  Project project;
  @Mock
  PullRequestRef toRef;
  @Mock
  PullRequestRef fromRef;
  @Mock
  PullRequestMergeability mergeability;
  @Mock
  PullRequestParticipant author;
  @Mock
  ApplicationUser authorUser;
  @Mock
  EscalatedSecurityContext securityContext;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(securityService.withPermission(any(Permission.class), anyString())).thenReturn(new MockSecurityContext());
    when(openedEvent.getPullRequest()).thenReturn(pr);
    when(authorUser.getSlug()).thenReturn("someguy");
    when(author.getUser()).thenReturn(authorUser);
    when(pr.getToRef()).thenReturn(toRef);
    when(pr.getFromRef()).thenReturn(fromRef);
    when(pr.getId()).thenReturn(10L);
    when(pr.getVersion()).thenReturn(10384);
    when(pr.getAuthor()).thenReturn(author);
    when(toRef.getRepository()).thenReturn(toRepo);
    when(toRef.getId()).thenReturn(RegexUtils.REFS_PREFIX + "master");
    when(fromRef.getRepository()).thenReturn(fromRepo);
    when(fromRef.getId()).thenReturn(RegexUtils.REFS_PREFIX + "otherMaster");
    when(toRepo.getProject()).thenReturn(project);
    when(toRepo.getId()).thenReturn(20);
    when(toRepo.getSlug()).thenReturn("repo_1");
    when(fromRepo.getProject()).thenReturn(project);
    when(fromRepo.getId()).thenReturn(30);
    when(fromRepo.getSlug()).thenReturn("repo_2");
    when(project.getKey()).thenReturn("PRJ");
    when(userUtils.dereferenceGroups(anyList())).thenReturn(Lists.<String>newArrayList());
    when(regexUtils.match(anyList(), anyString())).thenCallRealMethod();
    when(regexUtils.formatBranchName(anyString())).thenCallRealMethod();
  }

  @Test
  public void testAutomerge_defaultConfig() throws Exception {
    when(configDao.getConfigForRepo(project.getKey(), toRepo.getSlug())).thenReturn(Config.builder().build());
    sut.automergePullRequest(pr);
    verify(prService, never()).merge(any(PullRequestMergeRequest.class));
  }

  @Test
  public void testAutomerge_blockedBranches() throws Exception {
    when(configDao.getConfigForRepo(project.getKey(), toRepo.getSlug())).thenReturn(Config.builder()
        .automergePRs(newArrayList("master"))
        .blockedPRs(newArrayList("master"))
        .build());
    sut.automergePullRequest(pr);
    verify(prService, never()).merge(any(PullRequestMergeRequest.class));
  }

  @Test
  public void testAutomerge_canMerge() throws Exception {
    when(mergeability.canMerge()).thenReturn(true);
    when(prService.canMerge(toRepo.getId(), pr.getId())).thenReturn(mergeability);
    when(configDao.getConfigForRepo(project.getKey(), toRepo.getSlug())).thenReturn(Config.builder()
        .automergePRs(newArrayList("master"))
        .requiredReviewers(newArrayList("user1"))
        .requiredReviews(1)
        .build());
    when(securityService.impersonating(any(), any())).thenReturn(securityContext);
    when(securityContext.call(any())).then(inv -> {
      ((Operation) inv.getArguments()[0]).perform();
      return null;
    });
    sut.automergePullRequest(pr);
    verify(prService, times(1)).merge(any(PullRequestMergeRequest.class));
  }

  @Test
  public void testAutomerge_cannotMerge() throws Exception {
    when(mergeability.canMerge()).thenReturn(false);
    when(prService.canMerge(toRepo.getId(), pr.getId())).thenReturn(mergeability);
    when(configDao.getConfigForRepo(project.getKey(), toRepo.getSlug())).thenReturn(Config.builder()
        .automergePRs(newArrayList("master"))
        .requiredReviewers(newArrayList("user1"))
        .requiredReviews(1)
        .build());
    sut.automergePullRequest(pr);
    verify(prService, never()).merge(any(PullRequestMergeRequest.class));
  }

  static class MockSecurityContext implements EscalatedSecurityContext {

    @Override
    public <T, E extends Throwable> T call(Operation<T, E> operation) throws E {
      operation.perform();
      return null;
    }

    @Override
    public void applyToRequest() {

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

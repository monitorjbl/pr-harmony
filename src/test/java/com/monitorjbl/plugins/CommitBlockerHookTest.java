package com.monitorjbl.plugins;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.stash.hook.HookResponse;
import com.atlassian.stash.project.Project;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.Repository;
import com.google.common.collect.Lists;
import com.monitorjbl.plugins.config.Config;
import com.monitorjbl.plugins.config.ConfigDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.PrintWriter;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class CommitBlockerHookTest {
  @Mock
  private ConfigDao configDao;
  @Mock
  private UserManager userManager;
  @Mock
  private RegexUtils regexUtils;
  @Mock
  private UserUtils userUtils;
  @InjectMocks
  private CommitBlockerHook sut;

  @Mock
  Repository repository;
  @Mock
  Project project;
  @Mock
  RefChange change;
  @Mock
  HookResponse hookResponse;
  @Mock
  PrintWriter stderr;
  @Mock
  UserProfile user;

  @Before
  public void before() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(hookResponse.err()).thenReturn(stderr);
    when(repository.getProject()).thenReturn(project);
    when(repository.getSlug()).thenReturn("repo_1");
    when(project.getKey()).thenReturn("PRJ");
    when(userManager.getRemoteUser()).thenReturn(user);
    when(user.getUsername()).thenReturn("user1");
    when(userUtils.dereferenceGroups(anyList())).thenReturn(Lists.<String>newArrayList());
    when(regexUtils.match(anyList(), anyString())).thenCallRealMethod();
    when(regexUtils.formatBranchName(anyString())).thenCallRealMethod();
  }

  @Test
  public void testCommit_blocked() throws Exception {
    when(change.getRefId()).thenReturn("master");
    when(configDao.getConfigForRepo(project.getKey(), repository.getSlug())).thenReturn(Config.builder()
        .blockedCommits(newArrayList("master"))
        .build());
    assertThat(sut.onReceive(repository, newArrayList(change), hookResponse), is(false));
    verify(stderr, atLeastOnce()).write(anyString());
  }

  @Test
  public void testCommit_blockedWithExcludedUser() throws Exception {
    when(change.getRefId()).thenReturn(RegexUtils.REFS_PREFIX + "master");
    when(configDao.getConfigForRepo(project.getKey(), repository.getSlug())).thenReturn(Config.builder()
        .blockedCommits(newArrayList("master"))
        .excludedUsers(newArrayList("user1"))
        .build());
    assertThat(sut.onReceive(repository, newArrayList(change), hookResponse), is(true));
    verify(stderr, never()).write(anyString());
  }

  @Test
  public void testCommit_blockedWithExcludedGroup() throws Exception {
    when(change.getRefId()).thenReturn(RegexUtils.REFS_PREFIX + "master");
    when(configDao.getConfigForRepo(project.getKey(), repository.getSlug())).thenReturn(Config.builder()
        .blockedCommits(newArrayList("master"))
        .excludedGroups(newArrayList("group1"))
        .build());
    when(userUtils.dereferenceGroups(newArrayList("group1"))).thenReturn(newArrayList("user1"));

    assertThat(sut.onReceive(repository, newArrayList(change), hookResponse), is(true));
    verify(stderr, never()).write(anyString());
  }

  @Test
  public void testCommit_notBlocked() throws Exception {
    when(change.getRefId()).thenReturn(RegexUtils.REFS_PREFIX + "master");
    when(configDao.getConfigForRepo(project.getKey(), repository.getSlug())).thenReturn(Config.builder()
        .blockedCommits(newArrayList("bugfix"))
        .build());
    assertThat(sut.onReceive(repository, newArrayList(change), hookResponse), is(true));
    verify(stderr, never()).write(anyString());
  }

}

package com.monitorjbl.plugins;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.hook.HookResponse;
import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.repository.MinimalRef;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.StandardRefType;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.monitorjbl.plugins.config.Config;
import com.monitorjbl.plugins.config.ConfigDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommitBlockerHookTest {
  private final Config config = Config.builder().blockedCommits(Collections.singletonList("master")).build();
  private final StringWriter stderr = new StringWriter();

  @Mock
  private AuthenticationContext authenticationContext;
  @Mock
  private ConfigDao configDao;
  @InjectMocks
  private CommitBlockerHook hook;
  @Spy
  @SuppressWarnings("unused") //Used via @InjectMocks
  private RegexUtils regexUtils;
  @Mock
  private UserUtils userUtils;

  @Mock
  private RefChange change;
  @Mock
  private HookResponse hookResponse;
  @Mock
  private Project project;
  @Mock
  private MinimalRef ref;
  @Mock
  private Repository repository;
  @Mock
  private ApplicationUser user;

  @Before
  public void before() {
    when(authenticationContext.getCurrentUser()).thenReturn(user);
    when(change.getRef()).thenReturn(ref);
    when(configDao.getConfigForRepo(eq("PRJ"), eq("repo_1"))).thenReturn(config);
    when(hookResponse.err()).thenReturn(new PrintWriter(stderr));
    when(project.getKey()).thenReturn("PRJ");
    when(ref.getDisplayId()).thenReturn("master");
    when(ref.getType()).thenReturn(StandardRefType.BRANCH);
    when(repository.getProject()).thenReturn(project);
    when(repository.getSlug()).thenReturn("repo_1");
    when(userUtils.dereferenceGroups(anyListOf(String.class))).thenReturn(Collections.emptySet());
    when(user.getName()).thenReturn("name");
    when(user.getSlug()).thenReturn("slug");
  }

  @Test
  public void testCommit_blocked() {
    assertThat(hook.onReceive(repository, newArrayList(change), hookResponse), is(false));

    assertThat(stderr.toString(), allOf(
        containsString("Push Rejected"),
        containsString("Direct pushes are not allowed for [master]")));
  }

  @Test
  public void testCommit_blockedWithExcludedUser() {
    config.setExcludedUsers(Collections.singletonList("name"));

    assertThat(hook.onReceive(repository, newArrayList(change), hookResponse), is(true));

    verifyZeroInteractions(hookResponse);
  }

  @Test
  public void testCommit_blockedWithExcludedGroup() {
    config.setExcludedGroups(Collections.singletonList("group"));

    when(userUtils.dereferenceGroups(newArrayList("group"))).thenReturn(Collections.singleton("slug"));

    assertThat(hook.onReceive(repository, newArrayList(change), hookResponse), is(true));

    verifyZeroInteractions(hookResponse);
  }

  @Test
  public void testCommit_notBlockedForTags() {
    reset(ref);
    when(ref.getType()).thenReturn(StandardRefType.TAG);

    assertThat(hook.onReceive(repository, newArrayList(change), hookResponse), is(true));

    //Tags should not be checked, so as soon as the hook determines the push was to a tag it should return
    verify(ref, never()).getDisplayId();
    verify(ref, never()).getId();
    verifyZeroInteractions(authenticationContext, hookResponse, regexUtils, userUtils);
  }

  @Test
  public void testCommit_notBlockedNoMatch() {
    config.setBlockedCommits(Collections.singletonList("bugfix"));

    assertThat(hook.onReceive(repository, newArrayList(change), hookResponse), is(true));

    verifyZeroInteractions(hookResponse, userUtils);
  }

  @Test
  public void testCommit_notBlockedNotConfigured() {
    config.setBlockedCommits(Collections.emptyList());

    assertThat(hook.onReceive(repository, newArrayList(change), hookResponse), is(true));

    //When no restricted branches are configured, the hook should essentially be a no-op
    verifyZeroInteractions(authenticationContext, change, hookResponse, ref, regexUtils, userUtils);
  }
}

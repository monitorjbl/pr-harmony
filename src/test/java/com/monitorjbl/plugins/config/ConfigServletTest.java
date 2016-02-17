package com.monitorjbl.plugins.config;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.repository.RepositoryService;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.PermissionService;
import com.atlassian.stash.user.StashUser;
import com.atlassian.stash.user.UserService;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.monitorjbl.plugins.UserUtils;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletResponse;
import java.io.Writer;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class ConfigServletTest {
  @Mock
  private UserManager userManager;
  @Mock
  private UserUtils userUtils;
  @Mock
  private UserService userService;
  @Mock
  private RepositoryService repoService;
  @Mock
  private TemplateRenderer renderer;
  @Mock
  private PermissionService permissionService;
  @Mock
  private LoginUriProvider loginUriProvider;
  @InjectMocks
  private ConfigServlet sut;

  @Mock
  HttpServletResponse response;
  @Mock
  StashUser user;
  @Mock
  Repository repo;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(userUtils.getApplicationUserByName("user1")).thenReturn(user);
    when(repoService.getBySlug("PRJ", "repo1")).thenReturn(repo);
    when(permissionService.hasRepositoryPermission(user, repo, Permission.REPO_ADMIN)).thenReturn(true);
  }

  @Test
  public void testFoundRepo() throws Exception {
    sut.handleRequest("/plugins/servlet/pr-harmony/PRJ/repo1", "user1", response);
    verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
    verify(renderer, times(1)).render(anyString(), any(Map.class), any(Writer.class));
  }

  @Test
  public void testFoundRepo_differentContext() throws Exception {
    sut.handleRequest("/stash/plugins/servlet/pr-harmony/PRJ/repo1", "user1", response);
    verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
    verify(renderer, times(1)).render(anyString(), any(Map.class), any(Writer.class));
  }

  @Test
  public void testHandleMalformedRequest() throws Exception {
    sut.handleRequest("/plugins/servlet/saywhat/pr-harmony/PRJ/repo1", "user1", response);
    verify(response, atLeastOnce()).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }

  @Test
  public void testHandleMissingRepo() throws Exception {
    sut.handleRequest("/plugins/servlet/pr-harmony/FAKE/repo1", "user1", response);
    verify(response, atLeastOnce()).setStatus(HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  public void testPermissionDenied() throws Exception {
    sut.handleRequest("/plugins/servlet/pr-harmony/PRJ/repo1", "nonuser1", response);
    verify(response, atLeastOnce()).setStatus(HttpServletResponse.SC_FORBIDDEN);
  }
}

package com.monitorjbl.plugins.config;

import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletResponse;
import java.io.Writer;

import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigServletTest {
  @Mock
  private RepositoryService repoService;
  @Mock
  private PermissionService permissionService;
  @InjectMocks
  private ConfigServlet servlet;
  @Mock
  private TemplateRenderer templateRenderer;

  @Mock
  private Repository repo;
  @Mock
  private HttpServletResponse response;
  @Mock
  private ApplicationUser user;

  @Before
  public void setUp() {
    when(repoService.getBySlug("PRJ", "repo1")).thenReturn(repo);
    when(permissionService.hasRepositoryPermission(user, repo, Permission.REPO_ADMIN)).thenReturn(true);
    when(user.getName()).thenReturn("user1");
  }

  @Test
  public void testFoundRepo() throws Exception {
    servlet.handleRequest("/plugins/servlet/pr-harmony/PRJ/repo1", user, response);
    verify(response).setStatus(HttpServletResponse.SC_OK);
    verify(templateRenderer).render(anyString(), anyMapOf(String.class, Object.class), any(Writer.class));
  }

  @Test
  public void testFoundRepo_differentContext() throws Exception {
    servlet.handleRequest("/bitbucket/plugins/servlet/pr-harmony/PRJ/repo1", user, response);
    verify(response).setStatus(HttpServletResponse.SC_OK);
    verify(templateRenderer).render(anyString(), anyMapOf(String.class, Object.class), any(Writer.class));
  }

  @Test
  public void testHandleMalformedRequest() throws Exception {
    servlet.handleRequest("/plugins/servlet/saywhat/pr-harmony/PRJ/repo1", user, response);
    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }

  @Test
  public void testHandleMissingRepo() throws Exception {
    servlet.handleRequest("/plugins/servlet/pr-harmony/FAKE/repo1", user, response);
    verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  public void testPermissionDenied() throws Exception {
    reset(permissionService);

    servlet.handleRequest("/plugins/servlet/pr-harmony/PRJ/repo1", user, response);
    verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
  }
}

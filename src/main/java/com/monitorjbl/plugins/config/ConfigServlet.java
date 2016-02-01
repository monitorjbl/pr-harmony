package com.monitorjbl.plugins.config;

import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.project.ProjectService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;
import com.monitorjbl.plugins.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

public class ConfigServlet extends HttpServlet {
  public static final String SERVLET_PATH = "/plugins/servlet/pr-harmony/";
  private static final Logger logger = LoggerFactory.getLogger(ConfigServlet.class);

  private final UserManager userManager;
  private final UserUtils userUtils;
  private final RepositoryService repoService;
  private final ProjectService projectService;
  private final TemplateRenderer renderer;
  private final PermissionService permissionService;
  private final LoginUriProvider loginUriProvider;

  public ConfigServlet(UserManager userManager, UserUtils userUtils, RepositoryService repoService,
                       ProjectService projectService, TemplateRenderer renderer, PermissionService permissionService,
                       LoginUriProvider loginUriProvider) {
    this.userManager = userManager;
    this.userUtils = userUtils;
    this.repoService = repoService;
    this.projectService = projectService;
    this.renderer = renderer;
    this.permissionService = permissionService;
    this.loginUriProvider = loginUriProvider;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    UserProfile user = userManager.getRemoteUser();
    if(user == null) {
      response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
    } else {
      handleRequest(request.getRequestURI(), user.getUsername(), response);
    }
  }

  void handleRequest(String requestPath, String username, HttpServletResponse response) throws IOException {
    String[] coords = requestPath.replaceAll(".*" + SERVLET_PATH, "").split("/");
    if(coords.length == 1) {
      renderProjectSettings(coords[0], username, response);
    } else if(coords.length == 2) {
      renderRepoSettings(coords[0], coords[1], username, response);
    } else {
      logger.warn("Malformed request path, expecting {}{projectKey}/{repoSlug}", SERVLET_PATH);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  void renderRepoSettings(String projectKey, String repoSlug, String username, HttpServletResponse response) throws IOException {
    Repository repo = repoService.getBySlug(projectKey, repoSlug);
    if(repo == null) {
      logger.warn("Project/Repo [{}/{}] not found for user {}", projectKey, repoSlug, username);
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    ApplicationUser appUser = userUtils.getApplicationUserByName(username);
    if(permissionService.hasRepositoryPermission(appUser, repo, Permission.REPO_ADMIN)) {
      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentType("text/html;charset=utf-8");
      renderer.render("repo-config.html", ImmutableMap.<String, Object>of(
          "projectKey", projectKey,
          "repositorySlug", repoSlug
                                                                         ), response.getWriter());
    } else {
      logger.debug("Permission denied for user [{}]", username);
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
  }

  void renderProjectSettings(String projectKey, String username, HttpServletResponse response) throws IOException {
    Project project = projectService.getByKey(projectKey);
    if(project == null) {
      logger.warn("Project [{}] not found for user {}", projectKey, username);
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    ApplicationUser appUser = userUtils.getApplicationUserByName(username);
    if(permissionService.hasProjectPermission(appUser, project, Permission.PROJECT_ADMIN)) {
      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentType("text/html;charset=utf-8");
      renderer.render("project-config.html", ImmutableMap.<String, Object>of(
          "projectKey", projectKey
                                                                            ), response.getWriter());
    } else {
      logger.debug("Permission denied for user [{}]", username);
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
  }

  public URI getUri(HttpServletRequest request) {
    StringBuffer builder = request.getRequestURL();
    if(request.getQueryString() != null) {
      builder.append("?");
      builder.append(request.getQueryString());
    }
    return URI.create(builder.toString());
  }
}

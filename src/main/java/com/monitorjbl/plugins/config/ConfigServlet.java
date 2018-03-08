package com.monitorjbl.plugins.config;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.project.ProjectService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

public class ConfigServlet extends HttpServlet {
  public static final String SERVLET_PATH = "/plugins/servlet/pr-harmony/";

  private static final Logger logger = LoggerFactory.getLogger(ConfigServlet.class);

  private final AuthenticationContext authenticationContext;
  private final RepositoryService repoService;
  private final ProjectService projectService;
  private final TemplateRenderer renderer;
  private final PermissionService permissionService;
  private final LoginUriProvider loginUriProvider;

  public ConfigServlet(AuthenticationContext authenticationContext, RepositoryService repoService,
                       ProjectService projectService, TemplateRenderer renderer,
                       PermissionService permissionService, LoginUriProvider loginUriProvider) {
    this.authenticationContext = authenticationContext;
    this.repoService = repoService;
    this.projectService = projectService;
    this.renderer = renderer;
    this.permissionService = permissionService;
    this.loginUriProvider = loginUriProvider;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    ApplicationUser user = authenticationContext.getCurrentUser();
    if(user == null) {
      response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
    } else {
      handleRequest(request.getRequestURI(), user, response);
    }
  }

  void handleRequest(String requestPath, ApplicationUser user, HttpServletResponse response) throws IOException {
    String[] coords = requestPath.replaceAll(".*" + SERVLET_PATH, "").split("/");
    if(coords.length == 1) {
      renderProjectSettings(coords[0], user, response);
    } else if(coords.length == 2) {
      renderRepoSettings(coords[0], coords[1], user, response);
    } else {
      logger.warn("Malformed request path, expecting {}{projectKey}/{repoSlug}", SERVLET_PATH);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  void renderRepoSettings(String projectKey, String repoSlug, ApplicationUser user, HttpServletResponse response) throws IOException {
    Repository repo = repoService.getBySlug(projectKey, repoSlug);
    if(repo == null) {
      logger.warn("Project/Repo [{}/{}] not found for user {}", projectKey, repoSlug, user.getName());
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    if(permissionService.hasRepositoryPermission(user, repo, Permission.REPO_ADMIN)) {
      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentType("text/html;charset=utf-8");
      renderer.render("repo-config.html", ImmutableMap.of(
          "projectKey", projectKey,
          "repositorySlug", repoSlug
      ), response.getWriter());
    } else {
      logger.debug("Permission denied for user [{}]", user.getName());
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
  }

  void renderProjectSettings(String projectKey, ApplicationUser user, HttpServletResponse response) throws IOException {
    Project project = projectService.getByKey(projectKey);
    if(project == null) {
      logger.warn("Project [{}] not found for user {}", projectKey, user.getName());
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    if(permissionService.hasProjectPermission(user, project, Permission.PROJECT_ADMIN)) {
      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentType("text/html;charset=utf-8");
      renderer.render("project-config.html", ImmutableMap.of(
          "projectKey", projectKey
      ), response.getWriter());
    } else {
      logger.debug("Permission denied for user [{}]", user.getName());
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

package com.monitorjbl.plugins.config;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.repository.RepositoryService;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.PermissionService;
import com.atlassian.stash.user.StashUser;
import com.atlassian.stash.user.UserService;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

public class ConfigServlet extends HttpServlet {
  private static final Logger logger = LoggerFactory.getLogger(ConfigServlet.class);
  private final UserManager userManager;
  private final UserService userService;
  private final RepositoryService repoService;
  private final TemplateRenderer renderer;
  private final PermissionService permissionService;
  private final LoginUriProvider loginUriProvider;

  public ConfigServlet(UserManager userManager, UserService userService, RepositoryService repoService,
                       TemplateRenderer renderer, PermissionService permissionService, LoginUriProvider loginUriProvider) {
    this.userManager = userManager;
    this.userService = userService;
    this.repoService = repoService;
    this.renderer = renderer;
    this.permissionService = permissionService;
    this.loginUriProvider = loginUriProvider;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    UserProfile user = userManager.getRemoteUser();
    if (user == null) {
      response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
      return;
    }

    String[] coords = request.getRequestURI().replace("/stash/plugins/servlet/pr-harmony/", "").split("/");
    StashUser stashUser = userService.findUserByNameOrEmail(user.getUsername());
    Repository repo = repoService.getBySlug(coords[0], coords[1]);
    if (permissionService.hasRepositoryPermission(stashUser, repo, Permission.REPO_ADMIN)) {
      response.setContentType("text/html;charset=utf-8");
      renderer.render("config.html", ImmutableMap.<String, Object>of(
          "projectKey", coords[0],
          "repositorySlug", coords[1]
      ), response.getWriter());
    } else {
      logger.debug("Permission denied for user [{}]", user.getUsername());
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
  }

  public URI getUri(HttpServletRequest request) {
    StringBuffer builder = request.getRequestURL();
    if (request.getQueryString() != null) {
      builder.append("?");
      builder.append(request.getQueryString());
    }
    return URI.create(builder.toString());
  }
}

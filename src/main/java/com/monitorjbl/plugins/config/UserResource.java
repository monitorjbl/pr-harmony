package com.monitorjbl.plugins.config;

import com.monitorjbl.plugins.UserUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/users/{projectKey}/{repoSlug}")
public class UserResource {
  private final ConfigDao configDao;
  private final UserUtils utils;

  public UserResource(ConfigDao configDao, UserUtils utils) {
    this.configDao = configDao;
    this.utils = utils;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response get(@PathParam("projectKey") String projectKey, @PathParam("repoSlug") String repoSlug) {
    return Response.ok(utils.getDefaultAndRequiredUsers(projectKey, repoSlug)).build();
  }
}

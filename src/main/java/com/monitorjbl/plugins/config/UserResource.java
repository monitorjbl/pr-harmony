package com.monitorjbl.plugins.config;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/users/{projectKey}/{repoSlug}")
public class UserResource {
  private final ConfigDao configDao;

  public UserResource(ConfigDao configDao) {
    this.configDao = configDao;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response get(@PathParam("projectKey") String projectKey, @PathParam("repoSlug") String repoSlug) {
    return Response.ok(configDao.getDefaultAndRequiredUsers(projectKey, repoSlug)).build();
  }
}

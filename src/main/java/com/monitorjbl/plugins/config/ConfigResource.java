package com.monitorjbl.plugins.config;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/config/{projectKey}/{repoSlug}")
public class ConfigResource {
  private final ConfigDao configDao;

  public ConfigResource(ConfigDao configDao) {
    this.configDao = configDao;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response get(@PathParam("projectKey") String projectKey, @PathParam("repoSlug") String repoSlug) {
    return Response.ok(configDao.getConfigForRepo(projectKey, repoSlug)).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public Response post(@PathParam("projectKey") String projectKey, @PathParam("repoSlug") String repoSlug, Config config) {
    configDao.setConfigForRepo(projectKey, repoSlug, config);
    return Response.ok().build();
  }
}

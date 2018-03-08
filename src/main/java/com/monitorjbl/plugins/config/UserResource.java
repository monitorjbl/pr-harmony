package com.monitorjbl.plugins.config;

import com.google.common.collect.ImmutableMap;
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
    Config config = configDao.getConfigForRepo(projectKey, repoSlug);
    return Response.ok(ImmutableMap.of(
        "requiredReviews", config.getRequiredReviews() == null ? "" : config.getRequiredReviews(),
        "blockMergeIfPrNeedsWork", config.getBlockMergeIfPrNeedsWork() == null ? "" : config.getBlockMergeIfPrNeedsWork(),
        "requiredReviewers", utils.dereferenceUsers(config.getRequiredReviewers(), config.getRequiredReviewerGroups()),
        "defaultReviewers", utils.dereferenceUsers(config.getDefaultReviewers(), config.getDefaultReviewerGroups())
    )).build();
  }
}

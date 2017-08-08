package com.monitorjbl.plugins;

import com.atlassian.bitbucket.concurrent.BucketProcessor;
import com.atlassian.bitbucket.concurrent.BucketedExecutor;
import com.atlassian.bitbucket.concurrent.BucketedExecutorSettings;
import com.atlassian.bitbucket.concurrent.ConcurrencyPolicy;
import com.atlassian.bitbucket.concurrent.ConcurrencyService;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.bitbucket.user.SecurityService;
import com.atlassian.sal.api.user.UserManager;
import com.monitorjbl.plugins.config.ConfigDao;

import java.io.Serializable;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class AsyncProcessor {
  private static final String PREFIX = "com.monitorjbl.plugins:pr-harmony:";
  private static final Function<PRHarmonyTaskRequest, String> bucketFunction = task -> task.repositoryId + "-" + UUID.randomUUID();

  private final ConcurrencyService concurrencyService;
  private final ConfigDao configDao;
  private final UserUtils userUtils;
  private final PullRequestService prService;
  private final RepositoryService repoService;
  private final UserManager userManager;
  private final SecurityService securityService;

  public AsyncProcessor(ConcurrencyService concurrencyService, ConfigDao configDao, UserUtils userUtils,
                        PullRequestService prService, RepositoryService repoService, UserManager userManager,
                        SecurityService securityService) {
    this.concurrencyService = concurrencyService;
    this.configDao = configDao;
    this.userUtils = userUtils;
    this.prService = prService;
    this.repoService = repoService;
    this.userManager = userManager;
    this.securityService = securityService;
  }

  public void dispatch(String bucketName, PRHarmonyTaskRequest taskRequest, Consumer<TaskContext> func) {
    BucketedExecutor<PRHarmonyTaskRequest> exec = concurrencyService.getBucketedExecutor(
        PREFIX + bucketName,
        new BucketedExecutorSettings.Builder<>(bucketFunction, toBucketProcessor(taskRequest, func))
            .batchSize(Integer.MAX_VALUE)
            .maxAttempts(1)
            .maxConcurrency(4, ConcurrencyPolicy.PER_NODE)
            .build());
    exec.submit(taskRequest);
  }

  private BucketProcessor<PRHarmonyTaskRequest> toBucketProcessor(PRHarmonyTaskRequest taskRequest, Consumer<TaskContext> func) {
    return (s, list) -> func.accept(new TaskContext(this, taskRequest));
  }

  public static class TaskContext {
    public final ConfigDao configDao;
    public final UserUtils userUtils;
    public final UserManager userManager;
    public final PullRequestService prService;
    public final RepositoryService repoService;
    public final SecurityService securityService;
    public final PRHarmonyTaskRequest taskRequest;

    public TaskContext(AsyncProcessor asyncProcessor, PRHarmonyTaskRequest taskRequest) {
      this(asyncProcessor.configDao,
          asyncProcessor.userUtils,
          asyncProcessor.userManager,
          asyncProcessor.prService,
          asyncProcessor.repoService,
          asyncProcessor.securityService,
          taskRequest);
    }

    public TaskContext(ConfigDao configDao, UserUtils userUtils, UserManager userManager, PullRequestService prService,
                       RepositoryService repoService, SecurityService securityService, PRHarmonyTaskRequest taskRequest) {
      this.configDao = configDao;
      this.userUtils = userUtils;
      this.userManager = userManager;
      this.prService = prService;
      this.repoService = repoService;
      this.securityService = securityService;
      this.taskRequest = taskRequest;
    }
  }

  public static class PRHarmonyTaskRequest implements Serializable {
    public final Long pullRequestId;
    public final int repositoryId;

    public PRHarmonyTaskRequest() {
      this(null, -1);
    }

    public PRHarmonyTaskRequest(Repository repo) {
      this(null, repo.getId());
    }

    public PRHarmonyTaskRequest(PullRequest pr) {
      this(pr.getId(), pr.getToRef().getRepository().getId());
    }

    public PRHarmonyTaskRequest(Long pullRequestId, Integer repositoryId) {
      this.pullRequestId = pullRequestId;
      this.repositoryId = repositoryId;
    }
  }
}

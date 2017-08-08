package com.monitorjbl.plugins;

import com.atlassian.bitbucket.concurrent.BucketProcessor;
import com.atlassian.bitbucket.concurrent.BucketedExecutor;
import com.atlassian.bitbucket.concurrent.BucketedExecutorSettings;
import com.atlassian.bitbucket.concurrent.ConcurrencyService;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;

public class AsyncProcessor {
  private static final String PREFIX = "com.monitorjbl.plugins:pr-harmony:";
  private final ConcurrencyService concurrencyService;

  public AsyncProcessor(ConcurrencyService concurrencyService) {
    this.concurrencyService = concurrencyService;
  }

  @SuppressWarnings("unchecked")
  public <T extends Serializable> void dispatch(String bucketName, T taskRequest, TaskProcessor taskProcessor) {
    BucketedExecutor<T> exec = concurrencyService.getBucketedExecutor(
        PREFIX + bucketName,
        new BucketedExecutorSettings.Builder<T>(task -> format("pr-harmony_%s", UUID.randomUUID().toString()), taskProcessor)
            .batchSize(Integer.MAX_VALUE)
            .maxAttempts(1)
            .build());
    exec.submit(taskRequest);
  }

  public static abstract class TaskProcessor<T extends Serializable> implements BucketProcessor<T> {
    @Override
    public void process(@Nonnull String s, @Nonnull List<T> list) {
      list.forEach(this::handleTask);
    }

    public abstract void handleTask(T task);
  }

}

package com.monitorjbl.plugins;

import com.atlassian.bitbucket.concurrent.BucketProcessor;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class AsyncProcessor {
  private static final String PREFIX = "com.monitorjbl.plugins:pr-harmony:";
  private final ExecutorService concurrencyService;

  public AsyncProcessor(ExecutorService concurrencyService) {
    this.concurrencyService = concurrencyService;
  }

  @SuppressWarnings("unchecked")
  public <T extends Serializable> void dispatch(Runnable taskRequest) {
    concurrencyService.submit(taskRequest);
  }

  public static abstract class TaskProcessor<T extends Serializable> implements BucketProcessor<T> {
    @Override
    public void process(@Nonnull String s, @Nonnull List<T> list) {
      list.forEach(this::handleTask);
    }

    public abstract void handleTask(T task);
  }

}

package com.monitorjbl.plugins;

import com.atlassian.bitbucket.util.Version;
import com.atlassian.bitbucket.util.concurrent.ExecutorUtils;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.util.concurrent.ThreadFactories;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncProcessor implements LifecycleAware {

  private static final Version VERSION_5_9 = new Version(5, 9);

  private final ExecutorService executorService;
  private final boolean ownsExecutor;

  public AsyncProcessor(ApplicationProperties applicationProperties, ExecutorService executorService) {
    ownsExecutor = new Version(applicationProperties.getVersion()).compareTo(VERSION_5_9) < 0;
    if (ownsExecutor) {
      //For Bitbucket Server versions before 5.9, use our own ExecutorService to avoid starving out hook callbacks
      this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
          ThreadFactories.namedThreadFactory("pr-harmony", ThreadFactories.Type.DAEMON));
    } else {
      //Bitbucket Server 5.9 includes a fix for BSERV-10652, which moves hook callback to their own threadpool, so
      //for 5.9+ use the shared executor
      this.executorService = executorService;
    }
  }

  public void dispatch(Runnable taskRequest) {
    executorService.execute(taskRequest);
  }

  @Override
  public void onStart() {
    //No-op
  }

  @Override
  public void onStop() {
    if (ownsExecutor) {
      ExecutorUtils.shutdown(executorService, LoggerFactory.getLogger(getClass()));
    }
  }
}

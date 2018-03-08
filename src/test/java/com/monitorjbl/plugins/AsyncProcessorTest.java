package com.monitorjbl.plugins;

import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AsyncProcessorTest {

  @Mock
  private ApplicationProperties applicationProperties;
  @Spy
  private ExecutorService executorService = MoreExecutors.newDirectExecutorService();
  @Mock
  private Runnable task;

  @Test
  public void testDispatchFor5_8() {
    AsyncProcessor processor = create("5.8.1");
    processor.dispatch(task);
    processor.onStop();

    verify(task).run();
    verifyZeroInteractions(executorService);
  }

  @Test
  public void testDispatchFor5_9() {
    AsyncProcessor processor = create("5.9.0");
    processor.dispatch(task);
    processor.onStop();

    verify(executorService).execute(same(task));
    verify(task).run();
  }

  private AsyncProcessor create(String version) {
    when(applicationProperties.getVersion()).thenReturn(version);

    return new AsyncProcessor(applicationProperties, executorService);
  }
}

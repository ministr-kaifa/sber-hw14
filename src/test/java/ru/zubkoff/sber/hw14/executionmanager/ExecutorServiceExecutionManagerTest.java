package ru.zubkoff.sber.hw14.executionmanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.jupiter.api.RepeatedTest;

class ExecutorServiceExecutionManagerTest {

  @RepeatedTest(1_000)
  void givenTasksWhichThrowsExceptions_whenExecuteTasks_thenFailedTaskCountEqualToTaskCountAndCallbackExecutedExactlyOnce() throws InterruptedException {
    //given
    var executor = Executors.newFixedThreadPool(2);
    var manager = new ExecutorServiceExecutorManager(executor);
    AtomicInteger callbackExecutions = new AtomicInteger(0);
    Runnable callback = () -> {callbackExecutions.incrementAndGet();};
    Runnable[] tasks = {
      () -> {throw new RuntimeException();},
      () -> {throw new RuntimeException();},
      () -> {throw new RuntimeException();},
      () -> {throw new RuntimeException();},
      () -> {throw new RuntimeException();},
      () -> {throw new RuntimeException();}
    };

    //when
    var context = manager.execute(callback, tasks);
    executor.shutdown();
    executor.awaitTermination(10, TimeUnit.SECONDS);

    //then
    assertEquals(1, callbackExecutions.get());
    assertEquals(tasks.length, context.getFailedTaskCount());
  }

  @RepeatedTest(1_000)
  void givenTasks_whenExecuteTasks_thenAllTasksCompletedCallbackExecutedExactlyOnce() throws InterruptedException {
    //given
    var executor = Executors.newFixedThreadPool(2);
    var manager = new ExecutorServiceExecutorManager(executor);
    AtomicInteger callbackExecutions = new AtomicInteger(0);
    AtomicInteger taskExecutions = new AtomicInteger(0);
    Runnable callback = () -> {callbackExecutions.incrementAndGet();};
    Runnable task = () -> {taskExecutions.incrementAndGet();};
    int taskCount = 10;
    Runnable[] tasks = IntStream.range(0, taskCount).mapToObj(i -> task).toList().toArray(new Runnable[0]);

    //when
    var context = manager.execute(callback, tasks);
    executor.shutdown();
    executor.awaitTermination(10, TimeUnit.SECONDS);

    //then
    assertEquals(1, callbackExecutions.get());
    assertEquals(taskCount, context.getCompletedTaskCount());
    assertEquals(taskCount, taskExecutions.get());
  }
  
  @RepeatedTest(1_000)
  void givenTasks_whenExecuteTasksAndThenCancelThem_thenAllTasksInterruptedSuccesfullyCompletedTasksCountLessOrEqualsToThreadCountCallbackExecutedOnce() throws InterruptedException {
    //given
    int threadCount = 2;
    var executor = Executors.newFixedThreadPool(threadCount);
    var manager = new ExecutorServiceExecutorManager(executor);
    var taskLatch = new CountDownLatch(1);
    var notInterruptedTasks = new AtomicInteger(0);
    AtomicInteger callbackExecutions = new AtomicInteger(0);
    Runnable callback = () -> {callbackExecutions.incrementAndGet();};
    Runnable task = () -> {
      try {
        taskLatch.await();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      notInterruptedTasks.incrementAndGet(); 
    };
    int taskCount = 10;
    Runnable[] tasks = IntStream.range(0, taskCount)
      .mapToObj(i -> task)
      .toList().toArray(new Runnable[0]);

    //when
    var context = manager.execute(callback, tasks);
    context.interrupt();
    taskLatch.countDown();
    executor.shutdown();
    executor.awaitTermination(10, TimeUnit.SECONDS);


    //then
    assertEquals(taskCount, context.getInterruptedTaskCount());
    assertEquals(taskCount, context.getCompletedTaskCount());
    assertTrue(notInterruptedTasks.get() <= threadCount);
    assertEquals(1, callbackExecutions.get());

  }
}

package ru.zubkoff.sber.hw14.task;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.jupiter.api.RepeatedTest;

class TaskTest {

  @RepeatedTest(1_000)
  void givenTask_whenMultipleExecutionsSimultaneous_thenTaskRunsExactlyOnce() throws InterruptedException {
    //given
    var executor = Executors.newFixedThreadPool(15);
    var taskExecutions = new AtomicInteger(0);
    var threadsSimultaneousExecutionLatch = new CountDownLatch(1);
    Callable<Void> taskCallable = () -> {
      taskExecutions.incrementAndGet();
      return null;
    };

    //when
    var task = new Task<>(taskCallable);
    IntStream.range(0, 15)
      .mapToObj(i -> (Runnable) () -> {
        try {
          threadsSimultaneousExecutionLatch.await();
          task.get();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      })
      .forEach(executor::execute);

    threadsSimultaneousExecutionLatch.countDown();
    executor.shutdown();
    executor.awaitTermination(10, TimeUnit.SECONDS);
    

    //then
    assertEquals(1, taskExecutions.get());
  }

}

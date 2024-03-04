package ru.zubkoff.sber.hw14.task;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public class Task<T> {
  private boolean isComplete = false;
  private Callable<? extends T> callable;
  private final CountDownLatch initializationLatch = new CountDownLatch(1);

  public Task(Callable<? extends T> callable) {
    Semaphore executionSemaphore = new Semaphore(1);
    this.callable = () -> {
      executionSemaphore.acquire();
      if(!isComplete) {
        var result = callable.call();
        this.callable = () -> result;
        isComplete = true;
        executionSemaphore.release(Integer.MAX_VALUE);
        return result;
      } else {
        return get();
      }
    };
    initializationLatch.countDown();
  }

  public T get() throws Exception {
    initializationLatch.await();
    return callable.call();
  }

  public boolean isComplete() {
    return isComplete;
  }

}

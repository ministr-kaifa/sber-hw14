package ru.zubkoff.sber.hw14.executionmanager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ExecutorServiceExecutorManager implements ExecutionManger {

  private final ExecutorService executorService;

  public ExecutorServiceExecutorManager(ExecutorService executorService) {
    this.executorService = executorService;
  }

  @Override
  public Context execute(Runnable callback, Runnable... tasks) {
    AtomicInteger taskCounter = new AtomicInteger(tasks.length);
    return new Context(Stream.of(tasks)
        .map(task -> {
          CompletableFuture<Void> taskFuture = CompletableFuture.runAsync(task, executorService);
          taskFuture.whenComplete((result, e) -> {
              if(taskCounter.decrementAndGet() == 0) {
                callback.run();
              }
          });
          return taskFuture;
        })
        .toList()
    );
  }
}

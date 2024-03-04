package ru.zubkoff.sber.hw14.executionmanager;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;


public class Context {

  private final List<CompletableFuture<Void>> futures;

  public Context(List<CompletableFuture<Void>> futures) {
    this.futures = futures;
  }

  /**
   * @return количество тасков, которые на текущий момент успешно выполнились.
   */
  public int getCompletedTaskCount() {
    return (int) futures.stream().filter(Future::isDone).count();
  }

  /**
   * @return озвращает количество тасков, при выполнении которых произошел Exception.
   */
  public int getFailedTaskCount() {
    return (int) futures.stream()
      .filter(exceptionFuture -> exceptionFuture.isCompletedExceptionally() && !exceptionFuture.isCancelled())
      .count();
  }

  /**
   * @return возвращает количество тасков, которые не были выполены из-за отмены 
   */
  public int getInterruptedTaskCount() {
    return (int) futures.stream().filter(Future::isCancelled).count();
  }

  /**
   * отменяет выполнения тасков, которые еще не начали выполняться
   */
  public void interrupt() {
    futures.forEach(future -> future.cancel(true));
  }

  /**
   * @return true, если все таски были выполнены или отменены, false в противном случае.
   */
  public boolean isFinished() {
    return futures.stream().allMatch(Future::isDone);
  }

}
package ru.zubkoff.sber.hw14.executionmanager;

public interface ExecutionManger {
  Context execute(Runnable callback, Runnable... tasks);
}

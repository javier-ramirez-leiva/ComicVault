package org.comicVaultBackend.services;

import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface SchedulerService {
    public <T> Future<?> schedule(Consumer<T> task, T arg);

    public void shutdown();
}

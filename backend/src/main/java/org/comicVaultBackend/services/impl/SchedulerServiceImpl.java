package org.comicVaultBackend.services.impl;

import jakarta.annotation.PreDestroy;
import org.comicVaultBackend.services.SchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


@Service
public class SchedulerServiceImpl implements SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerServiceImpl.class);

    // Executor with max 5 concurrent jobs
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    /**
     * Schedule a job with arguments asynchronously.
     *
     * @param task A function that accepts an argument.
     * @param arg  The argument passed to the task.
     * @param <T>  Type of the argument.
     * @return A Future representing the running job.
     */
    public <T> Future<?> schedule(Consumer<T> task, T arg) {
        return executor.submit(() -> {
            try {
                task.accept(arg);
            } catch (Exception e) {
                logger.error("Error creating task: ", e);
            }
        });
    }

    /**
     * Gracefully shutdown the scheduler.
     */
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down scheduler...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

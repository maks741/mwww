package org.maks.mwww_daemon.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class AsyncRunnerService {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));
    }

    public <T> CompletableFuture<T> run(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, executorService);
    }

    public CompletableFuture<Void> run(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, executorService);
    }
}

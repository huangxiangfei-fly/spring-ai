package com.bigfly.langchain4j.demo.jdk21;

import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * JDK 21 虚拟线程池 (Virtual Thread Pool) 演示
 * 
 * JDK 21 提供了多种使用虚拟线程的方式：
 * 1. Executors.newVirtualThreadPerTaskExecutor() - 每个任务一个虚拟线程
 * 2. Thread.ofVirtual().factory() - 虚拟线程工厂
 * 3. 自定义虚拟线程池
 */
public class VirtualThreadPoolDemo {

    private static final AtomicInteger completedTasks = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        System.out.println("=== JDK 21 虚拟线程池演示 ===\n");
        
        // 1. 基础虚拟线程池
        basicVirtualThreadPool();
        
        // 2. 批量任务执行
        batchTaskExecution();
        
        // 3. 虚拟线程与传统线程池对比
        compareVirtualAndTraditionalPool();
        
        // 4. 虚拟线程池 + CompletableFuture
        virtualThreadWithCompletableFuture();
        
        // 5. 虚拟线程池处理阻塞操作
        virtualThreadWithBlockingOperations();
        
        // 6. 虚拟线程池的错误处理
        virtualThreadWithErrorHandling();
        
        // 7. 自定义虚拟线程工厂
        customVirtualThreadFactory();
    }
    
    /**
     * 基础虚拟线程池
     */
    private static void basicVirtualThreadPool() throws Exception {
        System.out.println("1. 基础虚拟线程池:");
        
        // 创建虚拟线程池（每个任务一个虚拟线程）
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // 提交多个任务
            List<Future<String>> futures = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                final int taskId = i;
                Future<String> future = executor.submit(() -> {
                    Thread.sleep(100);
                    return "任务 " + taskId + " 完成，线程: " + Thread.currentThread().getName();
                });
                futures.add(future);
            }
            
            // 获取结果
            for (Future<String> future : futures) {
                System.out.println("   " + future.get());
            }
        }
        
        System.out.println();
    }
    
    /**
     * 批量任务执行
     */
    private static void batchTaskExecution() throws Exception {
        System.out.println("2. 批量任务执行:");
        
        completedTasks.set(0);
        
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            int taskCount = 100;
            long startTime = System.currentTimeMillis();
            
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            for (int i = 0; i < taskCount; i++) {
                final int taskId = i;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        // 模拟一些工作
                        Thread.sleep(10);
                        completedTasks.incrementAndGet();
                        
                        // 每20个任务打印一次进度
                        if (taskId % 20 == 0) {
                            System.out.println("   任务 " + taskId + " 完成");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }, executor);
                futures.add(future);
            }
            
            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            long endTime = System.currentTimeMillis();
            System.out.println("   总耗时: " + (endTime - startTime) + "ms");
            System.out.println("   完成任务数: " + completedTasks.get());
        }
        
        System.out.println();
    }
    
    /**
     * 虚拟线程与传统线程池对比
     */
    private static void compareVirtualAndTraditionalPool() throws Exception {
        System.out.println("3. 虚拟线程与传统线程池对比:");
        
        int taskCount = 50;
        
        // 虚拟线程池
        long virtualStart = System.currentTimeMillis();
        try (ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < taskCount; i++) {
                final int taskId = i;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }, virtualExecutor);
                futures.add(future);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
        long virtualEnd = System.currentTimeMillis();
        System.out.println("   虚拟线程池耗时: " + (virtualEnd - virtualStart) + "ms");
        
        // 传统线程池（固定大小）
        long traditionalStart = System.currentTimeMillis();
        try (ExecutorService traditionalExecutor = Executors.newFixedThreadPool(10)) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < taskCount; i++) {
                final int taskId = i;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }, traditionalExecutor);
                futures.add(future);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
        long traditionalEnd = System.currentTimeMillis();
        System.out.println("   传统线程池(10线程)耗时: " + (traditionalEnd - traditionalStart) + "ms");
        
        System.out.println();
    }
    
    /**
     * 虚拟线程池 + CompletableFuture
     */
    private static void virtualThreadWithCompletableFuture() throws Exception {
        System.out.println("4. 虚拟线程池 + CompletableFuture:");
        
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        
        try {
            // 链式任务
            CompletableFuture<String> result = CompletableFuture.supplyAsync(() -> {
                return "第一步完成";
            }, executor)
            .thenApplyAsync(s -> s + " -> 第二步完成", executor)
            .thenApplyAsync(s -> s + " -> 第三步完成", executor);
            
            System.out.println("   " + result.get());
            
            // 并行任务
            CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> {
                try { Thread.sleep(100); } catch (Exception e) {}
                return "任务1";
            }, executor);
            
            CompletableFuture<String> task2 = CompletableFuture.supplyAsync(() -> {
                try { Thread.sleep(50); } catch (Exception e) {}
                return "任务2";
            }, executor);
            
            CompletableFuture<String> task3 = CompletableFuture.supplyAsync(() -> {
                try { Thread.sleep(80); } catch (Exception e) {}
                return "任务3";
            }, executor);
            
            // 等待所有任务完成
            CompletableFuture.allOf(task1, task2, task3).join();
            
            System.out.println("   并行任务结果: " + task1.get() + ", " + task2.get() + ", " + task3.get());
            
        } finally {
            executor.shutdown();
        }
        
        System.out.println();
    }
    
    /**
     * 虚拟线程池处理阻塞操作
     */
    private static void virtualThreadWithBlockingOperations() throws Exception {
        System.out.println("5. 虚拟线程池处理阻塞操作:");
        
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        
        try {
            // 模拟 I/O 密集型任务
            List<CompletableFuture<String>> futures = new ArrayList<>();
            
            for (int i = 0; i < 5; i++) {
                final int taskId = i;
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                    return performBlockingOperation(taskId);
                }, executor);
                futures.add(future);
            }
            
            for (CompletableFuture<String> future : futures) {
                System.out.println("   " + future.get());
            }
            
        } finally {
            executor.shutdown();
        }
        
        System.out.println();
    }
    
    private static String performBlockingOperation(int taskId) {
        try {
            // 模拟阻塞操作（如数据库查询、HTTP 请求等）
            Thread.sleep(100 + (long)(Math.random() * 100));
            return "阻塞操作 " + taskId + " 完成，线程: " + Thread.currentThread().getName();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "阻塞操作 " + taskId + " 被中断";
        }
    }
    
    /**
     * 虚拟线程池的错误处理
     */
    private static void virtualThreadWithErrorHandling() throws Exception {
        System.out.println("6. 虚拟线程池的错误处理:");
        
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        
        try {
            List<CompletableFuture<String>> futures = new ArrayList<>();
            
            // 正常任务和失败任务混合
            for (int i = 0; i < 10; i++) {
                final int taskId = i;
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                    if (taskId == 3 || taskId == 7) {
                        throw new RuntimeException("任务 " + taskId + " 故意失败");
                    }
                    try {
                        Thread.sleep(50);
                        return "任务 " + taskId + " 成功";
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("任务 " + taskId + " 被中断");
                    }
                }, executor)
                .exceptionally(e -> {
                    System.out.println("   捕获异常: " + e.getMessage());
                    return "任务 " + taskId + " 失败";
                });
                
                futures.add(future);
            }
            
            // 获取所有结果
            for (CompletableFuture<String> future : futures) {
                System.out.println("   " + future.get());
            }
            
        } finally {
            executor.shutdown();
        }
        
        System.out.println();
    }
    
    /**
     * 自定义虚拟线程工厂
     */
    private static void customVirtualThreadFactory() throws Exception {
        System.out.println("7. 自定义虚拟线程工厂:");
        
        // 创建自定义虚拟线程工厂
        ThreadFactory virtualThreadFactory = Thread.ofVirtual()
            .name("custom-virtual-", 1)
            .uncaughtExceptionHandler((thread, throwable) -> {
                System.err.println("未捕获异常在线程 " + thread.getName() + ": " + throwable.getMessage());
            })
            .factory();
        
        // 使用自定义工厂创建线程池
        ExecutorService executor = Executors.newThreadPerTaskExecutor(virtualThreadFactory);
        
        try {
            List<Future<String>> futures = new ArrayList<>();
            
            for (int i = 0; i < 5; i++) {
                final int taskId = i;
                Future<String> future = executor.submit(() -> {
                    Thread.sleep(50);
                    return "任务 " + taskId + " 完成";
                });
                futures.add(future);
            }
            
            for (Future<String> future : futures) {
                System.out.println("   " + future.get());
            }
            
            // 测试异常处理
            executor.submit(() -> {
                throw new RuntimeException("测试未捕获异常");
            }).get();
            
        } catch (ExecutionException e) {
            System.out.println("   捕获到执行异常: " + e.getCause().getMessage());
        } finally {
            executor.shutdown();
        }
        
        System.out.println();
    }
}

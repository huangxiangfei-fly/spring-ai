package com.bigfly.langchain4j.demo.jdk21;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JDK 21 虚拟线程与传统线程对比演示
 * 
 * 本演示详细对比虚拟线程（Virtual Threads）和传统平台线程（Platform Threads）的区别。
 */
public class VirtualVsTraditionalThreadDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== JDK 21 虚拟线程与传统线程对比 ===\n");
        
        // 1. 基础特性对比
        basicFeaturesComparison();
        
        // 2. 创建方式对比
        creationMethodComparison();
        
        // 3. 资源消耗对比
        resourceConsumptionComparison();
        
        // 4. 性能对比（并发任务）
        performanceComparison();
        
        // 5. 阻塞操作对比
        blockingOperationComparison();
        
        // 6. 适用场景对比
        usageScenarioComparison();
        
        // 7. 线程池对比
        threadPoolComparison();
    }
    
    /**
     * 1. 基础特性对比
     */
    private static void basicFeaturesComparison() {
        System.out.println("1. 基础特性对比:");
        
        // 创建虚拟线程
        Thread virtualThread = Thread.startVirtualThread(() -> {
            System.out.println("   虚拟线程: " + Thread.currentThread());
            System.out.println("   - 是否为虚拟线程: " + Thread.currentThread().isVirtual());
            System.out.println("   - 线程名称: " + Thread.currentThread().getName());
        });
        
        // 创建平台线程
        Thread platformThread = Thread.ofPlatform().start(() -> {
            System.out.println("\n   平台线程: " + Thread.currentThread());
            System.out.println("   - 是否为虚拟线程: " + Thread.currentThread().isVirtual());
            System.out.println("   - 线程名称: " + Thread.currentThread().getName());
        });
        
        try {
            virtualThread.join();
            platformThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("\n   主要区别:");
        System.out.println("   - 虚拟线程: 由 JVM 管理，轻量级，可创建百万级");
        System.out.println("   - 平台线程: 由操作系统管理，重量级，通常几千个");
        System.out.println("   - 虚拟线程成本极低，平台线程成本较高");
        System.out.println();
    }
    
    /**
     * 2. 创建方式对比
     */
    private static void creationMethodComparison() throws Exception {
        System.out.println("2. 创建方式对比:");
        
        // 传统平台线程创建方式
        System.out.println("   平台线程创建方式:");
        System.out.println("   - 方式1: new Thread(() -> {...}).start()");
        System.out.println("   - 方式2: Thread.ofPlatform().start(() -> {...})");
        System.out.println("   - 方式3: Executors.newFixedThreadPool(n)");
        
        // 虚拟线程创建方式
        System.out.println("\n   虚拟线程创建方式:");
        System.out.println("   - 方式1: Thread.startVirtualThread(() -> {...})");
        System.out.println("   - 方式2: Thread.ofVirtual().start(() -> {...})");
        System.out.println("   - 方式3: Executors.newVirtualThreadPerTaskExecutor()");
        
        // 演示
        Thread v1 = Thread.startVirtualThread(() -> {});
        Thread v2 = Thread.ofVirtual().name("virtual-demo").start(() -> {});
        
        v1.join();
        v2.join();
        
        System.out.println("\n   ✓ 虚拟线程创建更简洁，API 更友好");
        System.out.println();
    }
    
    /**
     * 3. 资源消耗对比
     */
    private static void resourceConsumptionComparison() {
        System.out.println("3. 资源消耗对比:");
        
        int threadCount = 1000;
        
        // 虚拟线程
        long virtualStart = System.currentTimeMillis();
        long virtualMemoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        CountDownLatch virtualLatch = new CountDownLatch(threadCount);
        List<Thread> virtualThreads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Thread vt = Thread.startVirtualThread(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    virtualLatch.countDown();
                }
            });
            virtualThreads.add(vt);
        }
        
        try {
            virtualLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long virtualEnd = System.currentTimeMillis();
        long virtualMemoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        System.out.println("   虚拟线程 (" + threadCount + " 个):");
        System.out.println("   - 创建时间: " + (virtualEnd - virtualStart) + "ms");
        System.out.println("   - 内存消耗: " + ((virtualMemoryAfter - virtualMemoryBefore) / 1024) + "KB");
        
        // 平台线程（使用固定大小的线程池）
        long platformStart = System.currentTimeMillis();
        long platformMemoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        ExecutorService platformExecutor = Executors.newFixedThreadPool(100);
        CountDownLatch platformLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            platformExecutor.submit(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    platformLatch.countDown();
                }
            });
        }
        
        try {
            platformLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        platformExecutor.shutdown();
        
        long platformEnd = System.currentTimeMillis();
        long platformMemoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        System.out.println("\n   平台线程 (" + threadCount + " 个, 100线程池):");
        System.out.println("   - 创建时间: " + (platformEnd - platformStart) + "ms");
        System.out.println("   - 内存消耗: " + ((platformMemoryAfter - platformMemoryBefore) / 1024) + "KB");
        
        System.out.println("\n   ✓ 虚拟线程在内存和启动时间上优势明显");
        System.out.println();
    }
    
    /**
     * 4. 性能对比（并发任务）
     */
    private static void performanceComparison() throws Exception {
        System.out.println("4. 性能对比（并发任务）:");
        
        int taskCount = 500;
        
        // 虚拟线程
        long virtualStart = System.nanoTime();
        try (ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < taskCount; i++) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(50); // 模拟阻塞操作
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }, virtualExecutor);
                futures.add(future);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
        long virtualEnd = System.nanoTime();
        
        // 平台线程
        long platformStart = System.nanoTime();
        try (ExecutorService platformExecutor = Executors.newFixedThreadPool(50)) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < taskCount; i++) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(50); // 模拟阻塞操作
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }, platformExecutor);
                futures.add(future);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
        long platformEnd = System.nanoTime();
        
        System.out.println("   任务数量: " + taskCount + " 个");
        System.out.println("   虚拟线程耗时: " + (virtualEnd - virtualStart) / 1_000_000 + "ms");
        System.out.println("   平台线程(50)耗时: " + (platformEnd - platformStart) / 1_000_000 + "ms");
        
        double speedup = (double)(platformEnd - platformStart) / (virtualEnd - virtualStart);
        System.out.println("   性能提升: " + String.format("%.2f", speedup) + "x");
        
        System.out.println("\n   ✓ 虚拟线程在阻塞密集型任务中性能优势明显");
        System.out.println();
    }
    
    /**
     * 5. 阻塞操作对比
     */
    private static void blockingOperationComparison() throws Exception {
        System.out.println("5. 阻塞操作对比:");
        
        System.out.println("   虚拟线程的阻塞优势:");
        System.out.println("   - 虚拟线程阻塞时，会释放底层平台线程");
        System.out.println("   - 同一平台线程可以承载多个虚拟线程");
        System.out.println("   - 适合 I/O 密集型任务（HTTP请求、数据库查询）");
        System.out.println("   - 平台线程阻塞会占用操作系统线程，效率低");
        
        System.out.println("\n   阻塞操作演示:");
        long start = System.nanoTime();
        
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<String>> futures = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                final int taskId = i;
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                    simulateBlockingOperation(taskId);
                    return "任务" + taskId;
                }, executor);
                futures.add(future);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
        
        long end = System.nanoTime();
        System.out.println("   10个阻塞任务并发执行耗时: " + (end - start) / 1_000_000 + "ms");
        System.out.println("   注意：虚拟线程使阻塞操作几乎无成本");
        
        System.out.println();
    }
    
    private static void simulateBlockingOperation(int taskId) {
        try {
            Thread.sleep(50); // 模拟阻塞
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 6. 适用场景对比
     */
    private static void usageScenarioComparison() {
        System.out.println("6. 适用场景对比:");
        
        System.out.println("   虚拟线程适合:");
        System.out.println("   ✓ I/O 密集型任务");
        System.out.println("   ✓ 高并发场景（Web服务、API调用）");
        System.out.println("   ✓ 每请求一个线程的架构");
        System.out.println("   ✓ 异步任务的简化编程");
        System.out.println("   ✓ 微服务架构");
        
        System.out.println("\n   平台线程适合:");
        System.out.println("   ✓ CPU 密集型任务（计算密集）");
        System.out.println("   ✓ 需要与原生代码交互");
        System.out.println("   ✓ 需要精细控制线程的调度");
        System.out.println("   ✓ 长期运行的任务");
        System.out.println("   ✓ 需要固定线程池大小的场景");
        
        System.out.println("\n   不建议混合使用在同一任务池中");
        System.out.println();
    }
    
    /**
     * 7. 线程池对比
     */
    private static void threadPoolComparison() throws Exception {
        System.out.println("7. 线程池对比:");
        
        System.out.println("   平台线程池:");
        System.out.println("   - Executors.newFixedThreadPool(n)");
        System.out.println("   - Executors.newCachedThreadPool()");
        System.out.println("   - 需要管理线程数量、队列大小等");
        System.out.println("   - 过多线程会导致资源耗尽");
        
        System.out.println("\n   虚拟线程执行器:");
        System.out.println("   - Executors.newVirtualThreadPerTaskExecutor()");
        System.out.println("   - 每个任务一个虚拟线程");
        System.out.println("   - 不需要管理线程数量");
        System.out.println("   - 虚拟线程数量几乎无限制");
        
        System.out.println("\n   演示: 虚拟线程执行器的简单性");
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 5; i++) {
                final int taskId = i;
                executor.submit(() -> {
                    System.out.println("   虚拟线程执行任务: " + taskId);
                });
            }
        }
        
        System.out.println("\n   ✓ 虚拟线程池使用更简单，无需配置复杂参数");
        System.out.println();
    }
    
    /**
     * 总结对比表
     */
    public static void printSummaryComparison() {
        System.out.println("\n=== 虚拟线程 vs 平台线程 总结 ===");
        System.out.println("┌─────────────────┬─────────────────┬──────────────────┐");
        System.out.println("│      特性       │    虚拟线程      │    平台线程      │");
        System.out.println("├─────────────────┼─────────────────┼──────────────────┤");
        System.out.println("│   创建成本       │   极低 (纳秒级)   │   高 (毫秒级)     │");
        System.out.println("│   内存占用       │   几KB           │   几MB           │");
        System.out.println("│   可创建数量     │   百万级         │   几千级         │");
        System.out.println("│   阻塞行为       │   释放底层线程    │   占用操作系统线程│");
        System.out.println("│   适用场景       │   I/O密集型      │   CPU密集型      │");
        System.out.println("│   调度方式       │   JVM调度        │   操作系统调度    │");
        System.out.println("│   编程模型       │   同步阻塞        │   异步/同步      │");
        System.out.println("│   线程池管理     │   无需管理        │   需要精细管理    │");
        System.out.println("└─────────────────┴─────────────────┴──────────────────┘");
    }
}

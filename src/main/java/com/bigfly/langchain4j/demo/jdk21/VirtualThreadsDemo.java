package com.bigfly.langchain4j.demo.jdk21;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * JDK 21 虚拟线程 (Virtual Threads) 演示
 * 
 * 虚拟线程是 JDK 21 的正式特性，它允许创建数百万个轻量级线程，
 * 与传统线程（平台线程）相比，虚拟线程由 JVM 管理，资源消耗极低。
 */
public class VirtualThreadsDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== JDK 21 虚拟线程演示 ===\n");
        
        // 1. 基础虚拟线程创建
        basicVirtualThread();
        
        // 2. 虚拟线程与传统线程对比
        compareVirtualAndPlatformThreads();
        
        // 3. 批量创建虚拟线程
        batchVirtualThreads();
        
        // 4. 虚拟线程执行器服务
        virtualThreadExecutor();
    }
    
    /**
     * 基础虚拟线程创建
     */
    private static void basicVirtualThread() throws Exception {
        System.out.println("1. 基础虚拟线程创建:");
        
        // 方法1: 直接创建并启动虚拟线程
        Thread vThread = Thread.startVirtualThread(() -> {
            System.out.println("   虚拟线程正在运行: " + Thread.currentThread());
            System.out.println("   是否为虚拟线程: " + Thread.currentThread().isVirtual());
        });
        vThread.join();
        
        // 方法2: 使用 Thread.ofVirtual() 创建虚拟线程
        Thread vThread2 = Thread.ofVirtual().name("my-virtual-thread").start(() -> {
            System.out.println("   命名的虚拟线程: " + Thread.currentThread().getName());
        });
        vThread2.join();
        
        System.out.println();
    }
    
    /**
     * 虚拟线程与传统线程对比
     */
    private static void compareVirtualAndPlatformThreads() throws Exception {
        System.out.println("2. 虚拟线程与传统线程对比:");
        
        // 传统平台线程
        Thread platformThread = Thread.ofPlatform().name("platform-thread").start(() -> {
            System.out.println("   平台线程: " + Thread.currentThread().getName());
            System.out.println("   是否为虚拟线程: " + Thread.currentThread().isVirtual());
        });
        
        // 虚拟线程
        Thread virtualThread = Thread.ofVirtual().name("virtual-thread").start(() -> {
            System.out.println("   虚拟线程: " + Thread.currentThread().getName());
            System.out.println("   是否为虚拟线程: " + Thread.currentThread().isVirtual());
        });
        
        platformThread.join();
        virtualThread.join();
        System.out.println();
    }
    
    /**
     * 批量创建虚拟线程
     */
    private static void batchVirtualThreads() throws Exception {
        System.out.println("3. 批量创建1000个虚拟线程:");
        
        long startTime = System.currentTimeMillis();
        
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            final int index = i;
            Thread thread = Thread.ofVirtual().name("virtual-" + i).start(() -> {
                try {
                    Thread.sleep(100);
                    System.out.println("   虚拟线程 " + index + " 完成");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads.add(thread);
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("   总耗时: " + (endTime - startTime) + "ms");
        System.out.println();
    }
    
    /**
     * 虚拟线程执行器服务
     */
    private static void virtualThreadExecutor() throws Exception {
        System.out.println("4. 虚拟线程执行器服务:");
        
        // 创建虚拟线程执行器
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // 提交多个任务
            List<Future<String>> futures = new ArrayList<>();
            
            for (int i = 0; i < 10; i++) {
                final int index = i;
                Future<String> future = executor.submit(() -> {
                    Thread.sleep(50);
                    return "任务 " + index + " 完成，线程: " + Thread.currentThread().getName();
                });
                futures.add(future);
            }
            
            // 获取所有结果
            for (Future<String> future : futures) {
                String result = future.get();
                System.out.println("   " + result);
            }
        }
        
        System.out.println();
    }
}

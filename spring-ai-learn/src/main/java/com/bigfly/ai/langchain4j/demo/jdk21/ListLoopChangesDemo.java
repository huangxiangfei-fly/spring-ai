package com.bigfly.ai.langchain4j.demo.jdk21;

import java.util.*;

/**
 * JDK 21 List 循环变化演示
 * 
 * JDK 21 对 List 循环本身没有语法改变，
 * 但引入了 SequencedCollection 接口，提供了一致的 API 来访问有序集合的首尾元素。
 */
public class ListLoopChangesDemo {

    public static void main(String[] args) {
        System.out.println("=== JDK 21 List 循环和集合操作演示 ===\n");
        
        // 1. 传统循环方式（JDK 21 仍然支持）
        traditionalLoopMethods();
        
        // 2. JDK 21 SequencedCollection 新特性
        sequencedCollectionFeatures();
        
        // 3. 反转视图循环
        reversedLooping();
        
        // 4. SequencedCollection 循环模式
        sequencedCollectionLoopPatterns();
        
        // 5. 传统方式 vs SequencedCollection 对比
        traditionalVsSequencedComparison();
    }
    
    /**
     * 1. 传统循环方式（JDK 21 仍然支持）
     * 
     * 注意：JDK 21 对循环语法本身没有改变
     */
    private static void traditionalLoopMethods() {
        System.out.println("1. 传统循环方式（JDK 21 仍然支持）:");
        
        List<String> list = Arrays.asList("Apple", "Banana", "Cherry", "Date", "Elderberry");
        
        // 方式1: 传统 for 循环
        System.out.println("   传统 for 循环:");
        for (int i = 0; i < list.size(); i++) {
            System.out.print(" " + list.get(i));
        }
        System.out.println();
        
        // 方式2: 增强 for 循环（推荐）
        System.out.println("   增强 for 循环:");
        for (String item : list) {
            System.out.print(" " + item);
        }
        System.out.println();
        
        // 方式3: Iterator
        System.out.println("   Iterator:");
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            System.out.print(" " + iterator.next());
        }
        System.out.println();
        
        // 方式4: forEach + Lambda（JDK 8+）
        System.out.println("   forEach + Lambda:");
        list.forEach(item -> System.out.print(" " + item));
        System.out.println();
        
        // 方式5: Stream API（JDK 8+）
        System.out.println("   Stream API:");
        list.stream().forEach(item -> System.out.print(" " + item));
        System.out.println();
        
        // 方式6: 方法引用（JDK 8+）
        System.out.println("   方法引用:");
        list.forEach(System.out::print);
        System.out.println();
        
        System.out.println("   ✓ 结论：JDK 21 对循环语法本身没有改变，所有传统方式仍然有效");
        System.out.println();
    }
    
    /**
     * 2. JDK 21 SequencedCollection 新特性
     */
    private static void sequencedCollectionFeatures() {
        System.out.println("2. JDK 21 SequencedCollection 新特性:");
        
        SequencedCollection<String> seqList = new ArrayList<>();
        seqList.add("Middle");
        seqList.addFirst("First");
        seqList.addLast("Last");
        
        System.out.println("   SequencedCollection: " + seqList);
        System.out.println("   第一个元素: " + seqList.getFirst());
        System.out.println("   最后一个元素: " + seqList.getLast());
        System.out.println("   移除第一个: " + seqList.removeFirst());
        System.out.println("   移除最后一个: " + seqList.removeLast());
        System.out.println("   结果: " + seqList);
        
        // 反转视图
        SequencedCollection<String> reversed = seqList.reversed();
        System.out.println("   反转视图: " + reversed);
        
        System.out.println();
    }
    
    /**
     * 3. 反转视图循环
     */
    private static void reversedLooping() {
        System.out.println("3. 反转视图循环:");
        
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E"));
        
        // 正向循环
        System.out.println("   正向循环:");
        for (String item : list) {
            System.out.print(" " + item);
        }
        System.out.println();
        
        // 反向循环（JDK 21 之前需要手动处理）
        System.out.println("   反向循环（传统方式）:");
        for (int i = list.size() - 1; i >= 0; i--) {
            System.out.print(" " + list.get(i));
        }
        System.out.println();
        
        // 反向循环（JDK 21 SequencedCollection）
        System.out.println("   反向循环（JDK 21 方式）:");
        SequencedCollection<String> reversedList = ((SequencedCollection<String>) list).reversed();
        for (String item : reversedList) {
            System.out.print(" " + item);
        }
        reversedList.stream().forEach(item -> System.out.print(" " + item));
        System.out.println();
        
        System.out.println("   注意：反转视图是对原集合的视图，不是副本");
        System.out.println();
    }
    
    /**
     * 4. SequencedCollection 循环模式
     */
    private static void sequencedCollectionLoopPatterns() {
        System.out.println("4. SequencedCollection 循环模式:");
        
        SequencedCollection<Integer> numbers = new LinkedList<>();
        numbers.addAll(Arrays.asList(1, 2, 3, 4, 5));
        
        System.out.println("   原始集合: " + numbers);
        
        // 模式1: 从两端处理
        System.out.println("   从两端处理:");
        while (!numbers.isEmpty()) {
            System.out.print("  首:" + numbers.getFirst() + " 尾:" + numbers.getLast());
            numbers.removeFirst();
            if (!numbers.isEmpty()) {
                numbers.removeLast();
            }
        }
        System.out.println();
        
        // 重新填充
        numbers.addAll(Arrays.asList(1, 2, 3, 4, 5));
        
        // 模式2: 使用反转视图
        System.out.println("   使用反转视图:");
        SequencedCollection<Integer> reversed = numbers.reversed();
        System.out.println("   原集合同步遍历:");
        System.out.println("   正向: " + numbers);
        System.out.println("   反向: " + reversed);
        
        System.out.println();
    }
    
    /**
     * 5. 传统方式 vs SequencedCollection 对比
     */
    private static void traditionalVsSequencedComparison() {
        System.out.println("5. 传统方式 vs SequencedCollection 对比:");
        
        List<String> traditionalList = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E"));
        SequencedCollection<String> sequencedList = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E"));
        
        System.out.println("   获取第一个元素:");
        System.out.println("     传统方式: " + traditionalList.get(0) + "（需要索引）");
        System.out.println("     Sequenced方式: " + sequencedList.getFirst() + "（语义更清晰）");
        
        System.out.println("   获取最后一个元素:");
        System.out.println("     传统方式: " + traditionalList.get(traditionalList.size() - 1) + "（需要计算）");
        System.out.println("     Sequenced方式: " + sequencedList.getLast() + "（语义更清晰）");
        
        System.out.println("   添加到开头:");
        System.out.println("     传统方式: traditionalList.add(0, \"Z\")");
        System.out.println("     Sequenced方式: sequencedList.addFirst(\"Z\")");
        
        System.out.println("   添加到末尾:");
        System.out.println("     传统方式: traditionalList.add(\"Z\")");
        System.out.println("     Sequenced方式: sequencedList.addLast(\"Z\")");
        
        System.out.println("   从开头删除:");
        System.out.println("     传统方式: traditionalList.remove(0)");
        System.out.println("     Sequenced方式: sequencedList.removeFirst()");
        
        System.out.println("   从末尾删除:");
        System.out.println("     传统方式: traditionalList.remove(traditionalList.size() - 1)");
        System.out.println("     Sequenced方式: sequencedList.removeLast()");
        
        System.out.println("   反转遍历:");
        System.out.println("     传统方式: 需要 for(int i=size-1; i>=0; i--)");
        System.out.println("     Sequenced方式: sequencedList.reversed()");
        
        System.out.println();
        System.out.println("   ✓ SequencedCollection 提供了更一致、更语义化的 API");
        System.out.println("   ✓ 实现了 SequencedCollection 的接口:");
        System.out.println("     - List (ArrayList, LinkedList)");
        System.out.println("     - SequencedSet (LinkedHashSet, TreeSet)");
        System.out.println("     - SequencedMap (LinkedHashMap)");
    }
}

package com.bigfly.langchain4j.demo.jdk21;

import java.util.*;

/**
 * JDK 21 序列化集合 (Sequenced Collections) 演示
 * 
 * Sequenced Collections 是 JDK 21 引入的新特性，
 * 提供了一套统一的 API 来处理具有顺序的集合。
 */
public class SequencedCollectionsDemo {

    public static void main(String[] args) {
        System.out.println("=== JDK 21 序列化集合演示 ===\n");
        
        // 1. ArrayList 操作
        arrayListDemo();
        
        // 2. LinkedList 操作
        linkedListDemo();
        
        // 3. TreeSet 操作
        treeSetDemo();
        
        // 4. LinkedHashSet 操作
        linkedHashSetDemo();
        
        // 5. SequencedMap 操作
        sequencedMapDemo();
        
        // 6. 反转视图
        reversedViewDemo();
    }
    
    /**
     * ArrayList 操作
     */
    private static void arrayListDemo() {
        System.out.println("1. ArrayList 操作:");
        
        SequencedCollection<String> list = new ArrayList<>();
        list.addFirst("第一个");
        list.addLast("最后一个");
        list.addAll(List.of("中间1", "中间2"));
        
        System.out.println("   集合内容: " + list);
        System.out.println("   第一个: " + list.getFirst());
        System.out.println("   最后一个: " + list.getLast());
        
        list.removeFirst();
        System.out.println("   删除第一个后: " + list);
        
        list.removeLast();
        System.out.println("   删除最后一个后: " + list);
        
        System.out.println();
    }
    
    /**
     * LinkedList 操作
     */
    private static void linkedListDemo() {
        System.out.println("2. LinkedList 操作:");
        
        SequencedCollection<Integer> linkedList = new LinkedList<>();
        linkedList.addFirst(1);
        linkedList.addLast(5);
        linkedList.addAll(Arrays.asList(2, 3, 4));
        
        System.out.println("   集合内容: " + linkedList);
        System.out.println("   第一个: " + linkedList.getFirst());
        System.out.println("   最后一个: " + linkedList.getLast());
        
        System.out.println();
    }
    
    /**
     * TreeSet 操作
     */
    private static void treeSetDemo() {
        System.out.println("3. TreeSet 操作:");
        
        SequencedSet<String> treeSet = new TreeSet<>();
        treeSet.addAll(List.of("banana", "apple", "cherry", "date"));
        
        System.out.println("   集合内容: " + treeSet);
        System.out.println("   第一个 (最小): " + treeSet.getFirst());
        System.out.println("   最后一个 (最大): " + treeSet.getLast());
        
        System.out.println();
    }
    
    /**
     * LinkedHashSet 操作
     */
    private static void linkedHashSetDemo() {
        System.out.println("4. LinkedHashSet 操作:");
        
        SequencedSet<Integer> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.addAll(Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6));
        
        System.out.println("   集合内容: " + linkedHashSet);
        System.out.println("   第一个 (插入顺序): " + linkedHashSet.getFirst());
        System.out.println("   最后一个 (插入顺序): " + linkedHashSet.getLast());
        
        System.out.println();
    }
    
    /**
     * SequencedMap 操作
     */
    private static void sequencedMapDemo() {
        System.out.println("5. SequencedMap 操作:");
        
        SequencedMap<Integer, String> map = new LinkedHashMap<>();
        map.put(1, "一");
        map.put(2, "二");
        map.put(3, "三");
        map.put(4, "四");
        
        System.out.println("   Map 内容: " + map);
        System.out.println("   第一个键: " + map.firstEntry());
        System.out.println("   最后一个键: " + map.lastEntry());
        
        map.putFirst(0, "零");
        System.out.println("   添加第一个后: " + map);
        
        map.putLast(5, "五");
        System.out.println("   添加最后一个后: " + map);
        
        System.out.println("   轮询第一个: " + map.pollFirstEntry());
        System.out.println("   轮询后: " + map);
        
        System.out.println("   轮询最后一个: " + map.pollLastEntry());
        System.out.println("   轮询后: " + map);
        
        System.out.println();
    }
    
    /**
     * 反转视图
     */
    private static void reversedViewDemo() {
        System.out.println("6. 反转视图:");
        
        SequencedCollection<String> list = new ArrayList<>(List.of("A", "B", "C", "D", "E"));
        System.out.println("   原始集合: " + list);
        
        SequencedCollection<String> reversed = list.reversed();
        System.out.println("   反转视图: " + reversed);
        
        System.out.println("   反转视图的第一个: " + reversed.getFirst());
        System.out.println("   反转视图的最后一个: " + reversed.getLast());
        
        // 修改反转视图会影响原集合
        reversed.addFirst("Z");
        System.out.println("   反转视图添加后: " + reversed);
        System.out.println("   原始集合也受影响: " + list);
        
        // Map 的反转视图
        SequencedMap<String, Integer> map = new LinkedHashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        
        SequencedMap<String, Integer> reversedMap = map.reversed();
        System.out.println("   原始 Map: " + map);
        System.out.println("   反转 Map: " + reversedMap);
        
        System.out.println();
    }
}

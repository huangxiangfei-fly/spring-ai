package com.bigfly.langchain4j.demo.jdk21;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * JDK 21 字符串模板 (String Templates) 演示
 * 
 * 注意: String Templates 是 JDK 21 的预览特性 (Preview Feature)
 * 需要使用 --enable-preview 参数编译和运行
 * 
 * 字符串模板提供了一种更安全、更简洁的方式来构建字符串，
 * 支持嵌入式表达式和多行字符串。
 */
public class StringTemplatesDemo {

    public static void main(String[] args) {
        System.out.println("=== JDK 21 字符串模板演示 ===");
        System.out.println("注意: 这是预览特性，需要使用 --enable-preview 参数运行\n");
        
        // 由于 String Templates 是预览特性，以下代码使用传统方式演示
        // 在实际使用时需要启用预览特性
        
        // 1. 基础字符串插值 (使用传统方式)
        basicStringInterpolation();
        
        // 2. 多行字符串 (文本块，JDK 15+)
        textBlocksDemo();
        
        // 3. 格式化字符串
        formattedStringsDemo();
        
        // 4. 字符串拼接最佳实践
        stringConcatenationBestPractices();
    }
    
    /**
     * 基础字符串插值演示 (使用传统方式)
     * 
     * JDK 21 的 String Templates 语法:
     * String message = STR."Hello, \{name}!";
     */
    private static void basicStringInterpolation() {
        System.out.println("1. 基础字符串插值:");
        
        String name = "张三";
        int age = 25;
        
        // 传统方式
        String traditional = "姓名: " + name + ", 年龄: " + age;
        System.out.println("   传统方式: " + traditional);
        
        // String.format 方式
        String formatted = String.format("姓名: %s, 年龄: %d", name, age);
        System.out.println("   String.format: " + formatted);
        
        // String Templates (JDK 21 预览特性，需要 --enable-preview)
        // String template = STR."姓名: \{name}, 年龄: \{age}";
        // System.out.println("   String Templates: " + template);
        
        System.out.println("   (String Templates 需要启用预览特性)");
        System.out.println();
    }
    
    /**
     * 文本块演示 (JDK 15+ 特性)
     */
    private static void textBlocksDemo() {
        System.out.println("2. 文本块 (Text Blocks):");
        
        // 传统多行字符串
        String traditional = "SELECT id, name, email\n" +
                            "FROM users\n" +
                            "WHERE status = 'active'\n" +
                            "ORDER BY name";
        System.out.println("   传统方式:\n" + traditional);
        
        // 文本块
        String textBlock = """
            SELECT id, name, email
            FROM users
            WHERE status = 'active'
            ORDER BY name
            """;
        System.out.println("   文本块:\n" + textBlock);
        
        // HTML 示例
        String html = """
            <html>
                <head>
                    <title>网页标题</title>
                </head>
                <body>
                    <h1>Hello World</h1>
                </body>
            </html>
            """;
        System.out.println("   HTML 文本块:\n" + html);
        
        System.out.println();
    }
    
    /**
     * 格式化字符串
     */
    private static void formattedStringsDemo() {
        System.out.println("3. 格式化字符串:");
        
        double price = 99.99;
        int quantity = 5;
        double total = price * quantity;
        
        // 传统格式化
        String formatted = String.format("单价: %.2f, 数量: %d, 总价: %.2f", 
                                        price, quantity, total);
        System.out.println("   格式化结果: " + formatted);
        
        // 日期格式化
        LocalDate now = LocalDate.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        System.out.println("   当前日期: " + dateStr);
        
        LocalDateTime dateTime = LocalDateTime.now();
        String dateTimeStr = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("   当前时间: " + dateTimeStr);
        
        System.out.println();
    }
    
    /**
     * 字符串拼接最佳实践
     */
    private static void stringConcatenationBestPractices() {
        System.out.println("4. 字符串拼接最佳实践:");
        
        // 1. 少量字符串拼接，使用 +
        String result1 = "Hello" + " " + "World";
        System.out.println("   简单拼接 (+): " + result1);
        
        // 2. 多次拼接，使用 StringBuilder
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append("项").append(i).append(", ");
        }
        String result2 = sb.toString();
        System.out.println("   StringBuilder: " + result2);
        
        // 3. 格式化字符串，使用 String.format
        String name = "李四";
        int age = 30;
        String result3 = String.format("姓名: %-10s 年龄: %3d", name, age);
        System.out.println("   String.format: '" + result3 + "'");
        
        // 4. 文本块用于多行字符串
        String result4 = """
            {
                "name": "%s",
                "age": %d,
                "active": true
            }
            """.formatted(name, age);
        System.out.println("   文本块格式化: " + result4.trim());
        
        System.out.println();
    }
}

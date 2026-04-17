package com.bigfly.ai.langchain4j.demo.jdk21;

/**
 * JDK 21 Switch 表达式演示
 * 
 * JDK 21 中 switch 表达式已成为正式特性，支持：
 * 1. Switch 表达式（可以返回值）
 * 2. 箭头语法 (->)
 * 3. 多标签 case (case 1, 2, 3)
 * 4. 模式匹配（Pattern Matching for switch）
 * 5. when 守卫条件
 * 6. 空值处理
 */
public class SwitchExpressionsDemo {

    public static void main(String[] args) {
        System.out.println("=== JDK 21 Switch 表达式演示 ===\n");
        
        // 1. 传统 switch 与新式 switch 对比
        traditionalVsModernSwitch();
        
        // 2. Switch 表达式返回值
        switchExpressionReturnValue();
        
        // 3. 多标签 case
        multipleLabelsCase();
        
        // 4. 模式匹配 (Pattern Matching for switch)
        patternMatchingSwitch();
        
        // 5. when 守卫条件
        switchWithWhenGuard();
        
        // 6. 空值处理
        switchNullHandling();
        
        // 7. switch 表达式实现计算器
        switchCalculator();
    }
    
    /**
     * 传统 switch 与新式 switch 对比
     */
    private static void traditionalVsModernSwitch() {
        System.out.println("1. 传统 switch 与新式 switch 对比:");
        
        // 传统 switch
        int dayOfWeek = 3;
        String traditionalResult = "";
        switch (dayOfWeek) {
            case 1:
                traditionalResult = "星期一";
                break;
            case 2:
                traditionalResult = "星期二";
                break;
            case 3:
                traditionalResult = "星期三";
                break;
            case 4:
                traditionalResult = "星期四";
                break;
            case 5:
                traditionalResult = "星期五";
                break;
            case 6:
                traditionalResult = "星期六";
                break;
            case 7:
                traditionalResult = "星期日";
                break;
            default:
                traditionalResult = "未知";
        }
        System.out.println("   传统方式: " + traditionalResult);
        
        // 新式 switch 表达式（箭头语法）
        String modernResult = switch (dayOfWeek) {
            case 1 -> "星期一";
            case 2 -> "星期二";
            case 3 -> "星期三";
            case 4 -> "星期四";
            case 5 -> "星期五";
            case 6 -> "星期六";
            case 7 -> "星期日";
            default -> "未知";
        };
        System.out.println("   新式方式: " + modernResult);
        System.out.println();
    }
    
    /**
     * Switch 表达式返回值
     */
    private static void switchExpressionReturnValue() {
        System.out.println("2. Switch 表达式返回值:");
        
        String season = getSeason(5);
        System.out.println("   5月是: " + season);
        
        System.out.println();
    }
    
    private static String getSeason(int month) {
        return switch (month) {
            case 12, 1, 2 -> "冬季";
            case 3, 4, 5 -> "春季";
            case 6, 7, 8 -> "夏季";
            case 9, 10, 11 -> "秋季";
            default -> "无效月份";
        };
    }
    
    /**
     * 多标签 case
     */
    private static void multipleLabelsCase() {
        System.out.println("3. 多标签 case:");
        
        String dayType = getDayType(3);
        System.out.println("   第3天是: " + dayType);
        
        dayType = getDayType(6);
        System.out.println("   第6天是: " + dayType);
        
        System.out.println();
    }
    
    private static String getDayType(int day) {
        return switch (day) {
            case 1, 2, 3, 4, 5 -> "工作日";
            case 6, 7 -> "周末";
            default -> "无效";
        };
    }
    
    /**
     * 模式匹配 (Pattern Matching for switch)
     */
    private static void patternMatchingSwitch() {
        System.out.println("4. 模式匹配:");
        
        Object obj1 = "Hello";
        System.out.println("   对象 '" + obj1 + "' 的类型: " + getType(obj1));
        
        Object obj2 = 123;
        System.out.println("   对象 " + obj2 + " 的类型: " + getType(obj2));
        
        Object obj3 = 3.14;
        System.out.println("   对象 " + obj3 + " 的类型: " + getType(obj3));
        
        Object obj4 = new int[]{1, 2, 3};
        System.out.println("   数组的长度: " + getType(obj4));
        
        System.out.println();
    }
    
    private static String getType(Object obj) {
        return switch (obj) {
            case String s -> "String, 长度: " + s.length();
            case Integer i -> "Integer, 值: " + i;
            case Double d -> "Double, 值: " + d;
            case int[] arr -> "int[], 长度: " + arr.length;
            case null -> "null";
            default -> "未知类型: " + obj.getClass().getSimpleName();
        };
    }
    
    /**
     * when 守卫条件
     */
    private static void switchWithWhenGuard() {
        System.out.println("5. when 守卫条件:");
        
        Object value = 15;
        String result = switch (value) {
            case Integer i when i < 0 -> "负数: " + i;
            case Integer i when i == 0 -> "零";
            case Integer i when i % 2 == 0 -> "偶数: " + i;
            case Integer i -> "奇数: " + i;
            case String s when s.isEmpty() -> "空字符串";
            case String s -> "字符串: " + s;
            default -> "其他类型";
        };
        System.out.println("   结果: " + result);
        
        System.out.println();
    }
    
    /**
     * 空值处理
     */
    private static void switchNullHandling() {
        System.out.println("6. 空值处理:");
        
        String result = processValue(null);
        System.out.println("   处理 null: " + result);
        
        result = processValue("Hello");
        System.out.println("   处理 'Hello': " + result);
        
        System.out.println();
    }
    
    private static String processValue(String value) {
        return switch (value) {
            case null -> "值是 null";
            case "" -> "值是空字符串";
            case String s when s.length() > 5 -> "长字符串: " + s;
            default -> "普通字符串: " + value;
        };
    }
    
    /**
     * switch 表达式实现计算器
     */
    private static void switchCalculator() {
        System.out.println("7. switch 表达式实现计算器:");
        
        System.out.println("   5 + 3 = " + calculate(5, '+', 3));
        System.out.println("   10 - 4 = " + calculate(10, '-', 4));
        System.out.println("   6 * 7 = " + calculate(6, '*', 7));
        System.out.println("   15 / 3 = " + calculate(15, '/', 3));
        
        System.out.println();
    }
    
    private static double calculate(double a, char operator, double b) {
        return switch (operator) {
            case '+' -> a + b;
            case '-' -> a - b;
            case '*' -> a * b;
            case '/' -> {
                if (b == 0) {
                    throw new ArithmeticException("除数不能为零");
                }
                yield a / b;
            }
            default -> throw new IllegalArgumentException("不支持的运算符: " + operator);
        };
    }
}

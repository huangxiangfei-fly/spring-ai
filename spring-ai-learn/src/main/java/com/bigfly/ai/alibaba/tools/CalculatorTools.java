package com.bigfly.ai.alibaba.tools;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * 计算器工具类
 * 提供给 AI 模型调用的数学计算函数
 */
@Slf4j
@Component
public class CalculatorTools extends BaseTools {

    @Override
    public Object getToolInstance() {
        return this;
    }

    /**
     * 加法计算
     *
     * @param a 第一个数
     * @param b 第二个数
     * @return 两数之和
     */
    @Tool(description = "计算两个数的和")
    public double add(double a, double b) {
        log.info("调用工具：加法计算, {} + {}", a, b);
        double result = a + b;
        log.info("计算结果: {}", result);
        return result;
    }

    /**
     * 减法计算
     *
     * @param a 被减数
     * @param b 减数
     * @return 两数之差
     */
    @Tool(description = "计算两个数的差")
    public double subtract(double a, double b) {
        log.info("调用工具：减法计算, {} - {}", a, b);
        double result = a - b;
        log.info("计算结果: {}", result);
        return result;
    }

    /**
     * 乘法计算
     *
     * @param a 第一个数
     * @param b 第二个数
     * @return 两数之积
     */
    @Tool(description = "计算两个数的积")
    public double multiply(double a, double b) {
        log.info("调用工具：乘法计算, {} * {}", a, b);
        double result = a * b;
        log.info("计算结果: {}", result);
        return result;
    }

    /**
     * 除法计算
     *
     * @param a 被除数
     * @param b 除数
     * @return 两数之商
     */
    @Tool(description = "计算两个数的商")
    public double divide(double a, double b) {
        log.info("调用工具：除法计算, {} / {}", a, b);
        if (b == 0) {
            log.warn("除数不能为0");
            throw new IllegalArgumentException("除数不能为0");
        }
        double result = a / b;
        log.info("计算结果: {}", result);
        return result;
    }

    /**
     * 计算平均值
     *
     * @param numbers 数字列表
     * @return 平均值
     */
    @Tool(description = "计算一组数字的平均值")
    public double average(double[] numbers) {
        log.info("调用工具：计算平均值, 数组长度: {}", numbers.length);
        if (numbers.length == 0) {
            return 0;
        }
        double sum = 0;
        for (double num : numbers) {
            sum += num;
        }
        double result = sum / numbers.length;
        log.info("计算结果: {}", result);
        return result;
    }
}

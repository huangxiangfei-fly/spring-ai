package com.bigfly.ai.alibaba.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间工具类
 * 提供给 AI 模型调用的时间相关函数
 */
@Slf4j
@Component
public class TimeTools extends BaseTools {

    @Override
    public Object getToolInstance() {
        return this;
    }

    /**
     * 获取当前日期
     *
     * @return 当前日期字符串（格式：yyyy-MM-dd）
     */
    @Tool(description = "获取当前日期，返回格式为 yyyy-MM-dd")
    public String getCurrentDate() {
        log.info("调用工具：获取当前日期");
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        log.info("当前日期: {}", date);
        return date;
    }

    /**
     * 获取当前时间
     *
     * @return 当前时间字符串（格式：yyyy-MM-dd HH:mm:ss）
     */
    @Tool(description = "获取当前时间，返回格式为 yyyy-MM-dd HH:mm:ss")
    public String getCurrentTime() {
        log.info("调用工具：获取当前时间");
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("当前时间: {}", time);
        return time;
    }

    /**
     * 获取当前星期
     *
     * @return 星期几
     */
    @Tool(description = "获取今天是星期几")
    public String getCurrentDayOfWeek() {
        log.info("调用工具：获取当前星期");
        String dayOfWeek = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE"));
        log.info("今天星期: {}", dayOfWeek);
        return dayOfWeek;
    }
}

package com.bigfly.langchain4j.tools;

import com.bigfly.langchain4j.util.HistoryEvent;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class HistoryEventTool {
    /**
     * 从文本中提取历史事件信息
     * 注意，可以理解为：LangChain4j会使用这个方法的签名来构建function call，而不是实际执行这个方法
     *
     * @param mainCharacters 主要人物列表
     * @param year           发生年份
     * @param description    事件描述
     * @return 历史事件对象
     */
    @Tool("创建历史事件对象，包含主要人物、发生年份和事件描述")
    public HistoryEvent createHistoryEvent(List<String> mainCharacters, int year, String description) {
        log.info("创建历史事件对象，主要人物：{}，发生年份：{}，事件描述：{}", mainCharacters, year, description);
        return null;
    }

}

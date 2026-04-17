package com.bigfly.ai.alibaba.tools;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * 诗歌创作工具
 * 用于演示 Human-in-the-Loop 人工审批功能
 */
@Slf4j
@Component
public class PoemTools extends BaseTools {

    @Override
    public Object getToolInstance() {
        return this;
    }

    /**
     * 创作诗歌
     *
     * @param theme 诗歌主题
     * @param style 诗歌风格(如:现代诗、古体诗、抒情诗等)
     * @return 创作的诗歌
     */
    @Tool(description = "根据主题和风格创作一首诗歌")
    public PoemResult createPoem(String theme, String style) {
        log.info("调用诗歌创作工具 - 主题: {}, 风格: {}", theme, style);
        
        // 这里模拟诗歌创作,实际应该调用 AI 或其他服务
        String poem = switch (style) {
            case "古体诗" -> String.format("""
                    《%s》
                    
                    春风拂面柳丝长，
                    夏日炎炎荷花香。
                    秋月皎洁照千里，
                    冬雪皑皑映寒窗。
                    """, theme);
            case "现代诗" -> String.format("""
                    《%s》
                    
                    在时光的河流中，
                    %s如星辰般闪耀。
                    我们追逐着梦想的光芒，
                    在岁月的长河里徜徉。
                    """, theme, theme);
            default -> String.format("""
                    《%s》
                    
                    %s，
                    是心灵的呼唤，
                    是梦想的翅膀，
                    在风中轻轻飘扬。
                    """, theme, theme);
        };
        
        PoemResult result = new PoemResult();
        result.setTitle(theme);
        result.setStyle(style);
        result.setContent(poem);
        
        log.info("诗歌创作完成");
        return result;
    }

    @Data
    public static class PoemResult {
        private String title;
        private String style;
        private String content;
        
        @Override
        public String toString() {
            return content;
        }
    }
}

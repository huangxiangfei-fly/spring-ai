package com.bigfly.ai.langchain4j.util;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.mapdb.DB;

import java.util.List;
import java.util.Map;

import static dev.langchain4j.data.message.ChatMessageDeserializer.messagesFromJson;
import static dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson;

public class EmbeddedByIdDb implements ChatMemoryStore {

    private DB db;
    private Map<Integer, String> map;

    /**
     * 带Map参数的构造方法，使用用户提供的映射
     *
     * @param map 用户提供的映射实例
     */
    public EmbeddedByIdDb(DB db, Map<Integer, String> map) {
        this.db = db;
        this.map = map;
    }

    /**
     * 将 memoryId 转换为 Integer
     * 如果 memoryId 是 String，尝试解析为 Integer；如果失败，使用 hashCode
     */
    private Integer convertToInteger(Object memoryId) {
        if (memoryId == null) {
            return 0;
        }
        
        if (memoryId instanceof Integer) {
            return (Integer) memoryId;
        }
        
        if (memoryId instanceof Number) {
            return ((Number) memoryId).intValue();
        }
        
        // 如果是 String，尝试解析
        if (memoryId instanceof String) {
            try {
                return Integer.parseInt((String) memoryId);
            } catch (NumberFormatException e) {
                // 解析失败，使用 hashCode
                return Math.abs(memoryId.hashCode());
            }
        }
        
        // 其他类型，使用 hashCode
        return Math.abs(memoryId.hashCode());
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        Integer id = convertToInteger(memoryId);
        String json = map.get(id);
        return messagesFromJson(json);
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        Integer id = convertToInteger(memoryId);
        String json = messagesToJson(messages);
        map.put(id, json);
        // 只有当db不为null时才提交事务
        if (db != null) {
            db.commit();
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        Integer id = convertToInteger(memoryId);
        map.remove(id);
        // 只有当db不为null时才提交事务
        if (db != null) {
            db.commit();
        }
    }
}

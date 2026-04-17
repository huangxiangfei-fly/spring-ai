package com.bigfly.ai.langchain4j.util;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mapdb.Serializer.*;

public class Tools {
    // 用于缓存已创建的ChatMemoryStore实例，避免重复打开同一数据库文件导致文件锁冲突
    private static final Map<String, ChatMemoryStore> storeCache = new ConcurrentHashMap<>();
    /**
     * 创建一个ChatMemory实例，用于存储聊天记忆，存在内存中
     *
     * @return ChatMemory实例
     */
    public static ChatMemory createRamChatMemoryInstance() {
        // 设置记忆长度是基于token的，所以这里要根据模型名称设定分词方式
        String modelNameForToken = dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O.toString();
        // 可以基于最大token数量来创建，也可以基于最大消息数量来创建，方法是:MessageWindowChatMemory.withMaxMessages(100)
        return TokenWindowChatMemory.withMaxTokens(5000, new OpenAiTokenCountEstimator(modelNameForToken));
    }

    /**
     * 创建一个ChatMemoryStore实例，用于存储聊天记忆，存在数据库中
     *
     * @return ChatMemoryStore实例
     */
    public static ChatMemoryStore createStoreInstance(String dbName, boolean isById) {
        // 生成缓存key，确保相同参数的请求返回同一个实例
        String cacheKey = dbName + "_" + isById;
        
        // 如果实例已存在，直接返回，避免重复打开数据库文件导致文件锁冲突
        if (storeCache.containsKey(cacheKey)) {
            return storeCache.get(cacheKey);
        }
        
        ChatMemoryStore rlt = null;
        // 创建一个MapDB实例，用于存储聊天记忆
        DB db = DBMaker.fileDB(dbName).transactionEnable().make();

        if (isById) {
            Map<Integer, String> dbMap = db.hashMap("messages", INTEGER, STRING).createOrOpen();
            rlt = new EmbeddedByIdDb(db, dbMap);
        } else {
            Map<String, String> dbMap = db.hashMap("messages", STRING, STRING).createOrOpen();
            rlt = new EmbeddedGlobalDb(db, dbMap);
        }

        // 将新创建的实例存入缓存
        storeCache.put(cacheKey, rlt);
        return rlt;
    }

    /**
     * 创建一个ChatMemory实例，用于存储聊天记忆，存在数据库中
     *
     * @return ChatMemory实例
     */
    public static ChatMemory createDbChatMemoryInstance(String dbName, boolean isById) {
        // 创建一个MapDB实例，用于存储聊天记忆
        ChatMemoryStore store = createStoreInstance(dbName, isById);

        return MessageWindowChatMemory
                .builder()
                .maxMessages(100)
                .chatMemoryStore(store)
                .build();
    }


}

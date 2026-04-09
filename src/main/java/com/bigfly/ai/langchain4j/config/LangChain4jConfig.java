package com.bigfly.ai.langchain4j.config;

import com.bigfly.ai.langchain4j.service.*;
import com.bigfly.ai.langchain4j.tools.HistoryEventTool;
import com.bigfly.ai.langchain4j.tools.WeatherTools;
import com.bigfly.ai.langchain4j.util.ImageEditModelParam;
import com.bigfly.ai.langchain4j.util.Tools;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.community.model.dashscope.WanxImageSize;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.*;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import dev.langchain4j.service.AiServices;

import java.util.List;


/**
 * LangChain4j配置类
 */
@Configuration
public class LangChain4jConfig {

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:qwen-plus}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String baseUrl;

    // 图片生成模型的配置
    @Value("${langchain4j.open-ai.chat-model.image-gen-model.api-key}")
    private String imageGenModelApiKey;

    @Value("${langchain4j.open-ai.chat-model.image-gen-model.model-name}")
    private String imageGenModelName;
    // 图片编辑模型的配置
    @Value("${langchain4j.open-ai.chat-model.image-edit-model.api-key}")
    private String imageEditModelApiKey;

    @Value("${langchain4j.open-ai.chat-model.image-edit-model.model-name}")
    private String imageEditModelName;

    @Value("${langchain4j.open-ai.chat-model.image-edit-model.base-url}")
    private String imageEditModelBaseUrl;

    // 视觉理解模型的配置
    @Value("${langchain4j.open-ai.chat-model.image-vl-model.api-key}")
    private String imageVLModelApiKey;

    @Value("${langchain4j.open-ai.chat-model.image-vl-model.model-name}")
    private String imageVLModelName;

    @Value("${langchain4j.open-ai.chat-model.image-vl-model.base-url}")
    private String imageVLModelBaseUrl;


    @Value("${weather.tools.url}")
    private String weatherToolsUrl;

    @Value("${weather.tools.id}")
    private String weatherToolsId;

    @Value("${weather.tools.key}")
    private String weatherToolsKey;

    /**
     * 创建并配置OpenAiChatModel实例（使用通义千问的OpenAI兼容接口）
     *
     * @return OpenAiChatModel实例
     */
    @Bean
    public OpenAiChatModel openAiChatModel() {
        ChatModelListener logger = new ChatModelListener() {
            @Override
            public void onRequest(ChatModelRequestContext reqCtx) {
                // 1. 拿到 List<ChatMessage>
                List<ChatMessage> messages = reqCtx.chatRequest().messages();
                System.out.println("→ 请求: " + messages);
            }

            @Override
            public void onResponse(ChatModelResponseContext respCtx) {
                // 2. 先取 ChatModelResponse
                ChatResponse response = respCtx.chatResponse();
                // 3. 再取 AiMessage
                AiMessage aiMessage = response.aiMessage();

                // 4. 工具调用
                List<ToolExecutionRequest> tools = aiMessage.toolExecutionRequests();
                for (ToolExecutionRequest t : tools) {
                    System.out.println("← tool      : " + t.name());
                    System.out.println("← arguments : " + t.arguments()); // 原始 JSON
                }

                // 5. 纯文本
                if (aiMessage.text() != null) {
                    System.out.println("← text      : " + aiMessage.text());
                }
            }

            @Override
            public void onError(ChatModelErrorContext errorCtx) {
                errorCtx.error().printStackTrace();
            }
        };
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .listeners(List.of(logger))
                .build();
    }

    /**
     * 创建并配置StreamingChatModel实例（使用通义千问的OpenAI兼容接口）
     *
     * @return StreamingChatModel实例
     */
    @Bean
    public StreamingChatModel streamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * 创建并配置StreamingAssistant实例  --流响应
     *
     * @param streamingChatModel StreamingChatModel实例
     * @return StreamingAssistant实例
     */
    @Bean
    public StreamingAssistant streamingAssistant(@Qualifier("streamingChatModel")StreamingChatModel streamingChatModel) {
        // 创建一个ChatMemory实例，通过消息数量限制记忆长度，记录在数据库中 -- 高级
       ChatMemory chatMemory = Tools.createDbChatMemoryInstance("chat-memory-global.db", false);

        return AiServices.builder(StreamingAssistant.class)
                .streamingChatModel(streamingChatModel)
                .chatMemory(chatMemory)
                .build();
    }


    @Bean
    public Assistant assistantRamGlobal(@Qualifier("openAiChatModel") OpenAiChatModel chatModel) {
        // 创建一个ChatMemory实例，通过token数量限制记忆长度，记录在内存中
//        ChatMemory chatMemory = Tools.createRamChatMemoryInstance();

        // 生成Assistant服务实例已经绑定了chatMemory
//        return AiServices.builder(Assistant.class)
//                .chatModel(chatModel)
//                .chatMemory(chatMemory)
//                .build();

        // 注意，这里通过chatMemoryProvider来指定每个id和chatMemory的对应关系，记录在内存中--低级
//        return AiServices.builder(Assistant.class)
//                .chatModel(chatModel)
//                .chatMemoryProvider(memoryId -> Tools.createRamChatMemoryInstance())
//                .build();

        // 创建一个ChatMemory实例，通过消息数量限制记忆长度，记录在数据库中 -- 高级
//        ChatMemory chatMemory = Tools.createDbChatMemoryInstance("chat-memory-global.db", false);
//
//        // 生成Assistant服务实例已经绑定了chatMemory
//        return AiServices.builder(Assistant.class)
//                .chatModel(chatModel)
//                .chatMemory(chatMemory)
//                .build();

        ChatMemoryStore chatMemoryStore = Tools.createStoreInstance("chat-memory-byid.db", true);

        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(100)
                .chatMemoryStore(chatMemoryStore)
                .build();

        // 生成Assistant服务实例已经绑定了chatMemory
        return AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
    }

    @Bean
    public AssistantFuncationcall assistantFuncationcall(@Qualifier("openAiChatModel")OpenAiChatModel chatModel, HistoryEventTool historyEventTool) {
        // 创建一个ChatMemory实例，通过消息数量限制记忆长度，记录在数据库中 -- 高级
        ChatMemory chatMemory = Tools.createDbChatMemoryInstance("chat-memory-global.db", false);

        // 生成Assistant服务实例已经绑定了chatMemory
        return AiServices.builder(AssistantFuncationcall.class)
                .chatModel(chatModel)
                .tools(historyEventTool)
                .chatMemory(chatMemory)
                .build();
    }
    /**
     * 创建并配置用于图像生成的OpenAiChatModel实例
     *
     * @return OpenAiChatModel实例，Bean名称为imageGenModel
     */
    @Bean("imageGenModel")
    public WanxImageModel imageGenModel() {
        return WanxImageModel.builder()
                .apiKey(imageGenModelApiKey)
                .modelName(imageGenModelName)
                .size(WanxImageSize.SIZE_1024_1024)
                .build();
    }

    /**
     * 创建数据结构实例，这只是个保管数据的对象，里面包含了图像编辑模型的配置参数
     *
     * @return ImageEditModelParam实例，Bean名称为imageEditModelParam
     */
    @Bean("imageEditModelParam")
    public ImageEditModelParam imageEditModelParam() {
        ImageEditModelParam param = new ImageEditModelParam();
        param.setModelName(imageEditModelName);
        param.setBaseUrl(imageEditModelBaseUrl);
        param.setApiKey(imageEditModelApiKey);
        return param;
    }

    /**
     * 创建并配置用于视觉理解的OpenAiChatModel实例
     *
     * @return OpenAiChatModel实例，Bean名称为imageVLModel
     */
    @Bean("imageVLModel")
    public OpenAiChatModel imageVLModel() {
        return OpenAiChatModel.builder()
                .apiKey(imageVLModelApiKey)
                .modelName(imageVLModelName)
                .baseUrl(imageVLModelBaseUrl)
                .build();
    }

    @Bean("modelWithJSONFormat")
    public OpenAiChatModel modelFromObject() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .responseFormat(String.valueOf(ResponseFormat.JSON))
                .build();
    }

    @Bean
    public AssistantFormJson assistantWithModelFromObject(@Qualifier("modelWithJSONFormat") OpenAiChatModel modelWithJSONFormat) {
        return AiServices.create(AssistantFormJson.class, modelWithJSONFormat);
    }

    @Bean("modelFromSchema")
    public OpenAiChatModel modelFromSchema() {
        JsonSchema jsonSchema = JsonSchema.builder()
                .name("HistoryEvent") // OpenAI 要求顶层 schema 有名字
                .rootElement(
                        JsonObjectSchema.builder()
                                .addProperty("mainCharacters", // 字符串数组
                                        JsonArraySchema.builder()
                                                .items(new JsonStringSchema())
                                                .build())
                                .addProperty("year", new JsonIntegerSchema())
                                .addProperty("description", new JsonStringSchema())
                                .required("mainCharacters", "year", "description")
                                .build())
                .build();

        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .responseFormat(String.valueOf(ResponseFormat.builder()
                        .type(ResponseFormatType.JSON)
                        .jsonSchema(jsonSchema)
                        .build()))
                .build();
    }

    @Bean
    public AssistantSchemaJson assistantWithModelFromSchema(@Qualifier("modelFromSchema") OpenAiChatModel modelFromSchema) {
        return AiServices.create(AssistantSchemaJson.class, modelFromSchema);
    }

    @Bean
    public AssistantTool assistantTool(@Qualifier("openAiChatModel")OpenAiChatModel chatModel,WeatherTools weatherTools) {
        // 创建一个ChatMemory实例，通过消息数量限制记忆长度，记录在数据库中 -- 高级
        ChatMemory chatMemory = Tools.createDbChatMemoryInstance("chat-memory-global.db", false);

        // 生成Assistant服务实例已经绑定了chatMemory
        return AiServices.builder(AssistantTool.class)
                .chatModel(chatModel)
                .chatMemory(chatMemory)
                .tools(weatherTools)
                .build();
    }




    /**
     * 创建天气工具类实例
     *
     * @return WeatherTools实例，Bean名称为weatherTools
     */
    @Bean
    public WeatherTools weatherTools() {
        WeatherTools tools = new WeatherTools();
        tools.setWeatherToolsUrl(weatherToolsUrl);
        tools.setWeatherToolsId(weatherToolsId);
        tools.setWeatherToolsKey(weatherToolsKey);
        return tools;
    }

}

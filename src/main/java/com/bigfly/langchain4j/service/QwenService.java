package com.bigfly.langchain4j.service;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.bigfly.langchain4j.util.ImageEditModelParam;
import com.bigfly.langchain4j.util.ImageUtils;
import com.bigfly.langchain4j.util.Tools;
import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.*;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class  QwenService {
    private static final Logger logger = LoggerFactory.getLogger(QwenService.class);

    // 注入OpenAiChatModel，用于与通义千问进行交互
    private final OpenAiChatModel openAiChatModel;
    @Autowired
    private Assistant assistant;
    // 注入OpenAiChatModel，用于图像理解任务
    private final OpenAiChatModel imageVLModel;

    // 注入WanxImageModel，用于图像生成任务
    private final WanxImageModel imageGenModel;

    // 注入WanxImageModel，用于图像编辑任务
    private final ImageEditModelParam imageEditModelParam;

    //传统聊天对话存储
    private List<ChatMessage> history = new ArrayList<>();

    // 对话存储
    private ChatMemory chatMemory = null;


    /**
     * 构造函数，通过依赖注入获取OpenAiChatModel实例
     *
     * @param openAiChatModel OpenAiChatModel实例
     */
    @Autowired
    public QwenService(OpenAiChatModel openAiChatModel, @Qualifier("imageVLModel") OpenAiChatModel imageVLModel,
                       @Qualifier("imageGenModel") WanxImageModel imageGenModel,
                       @Qualifier("imageEditModelParam") ImageEditModelParam imageEditModelParam
) {
        this.openAiChatModel = openAiChatModel;
        this.imageVLModel = imageVLModel;
        this.imageGenModel = imageGenModel;
        this.imageEditModelParam = imageEditModelParam;
    }

    /**
     * 调用通义千问模型进行对话
     *
     * @param message 用户消息
     * @return AI回复
     */
    public String chat(String message) {
        return openAiChatModel.chat(message);
    }

    /**
     * 获取AI模型的响应（用于接口调用）
     *
     * @param prompt 用户提示词
     * @return AI生成的回答
     */
    public String getResponse(String prompt) {
        return openAiChatModel.chat(prompt);
    }



    /**
     * 调用AiService进行最简单的对话
     *
     * @param prompt 用户提示词
     * @return 助手生成的回答
     */
    public String aiServiceSimpleChat(String prompt) {
        return assistant.simpleChat(prompt) + "[from aiservice simpleChat]";
    }

    /**
     * 调用AiService进行模板对话
     *
     * @param name 模板中的变量
     * @return 助手生成的回答
     */
    public String aiServiceTemplateChat(String name) {
        return assistant.temlateChat(name) + "[from aiservice templateChat]";
    }

    /**
     * 调用AiService进行模板对话，包含系统消息
     *
     * @param name 模板中的变量
     * @return 助手生成的回答
     */
    public String aiServiceTemplateChatWithSysMsg(String name) {
        return assistant.temlateChatWithSysMsg(name) + "[from aiservice templateChatWithSysMsg]";
    }

    /**
     * 模拟多轮对话
     *
     * @param prompt 模板中的变量
     * @return 助手生成的回答
     */
    public String simulateMultiRoundChat(String prompt) {
        history.add(UserMessage.from(prompt));
        AiMessage reply = openAiChatModel.chat(history).aiMessage();
        history.add(reply);
        return reply.text() + "[from simulateMultiRoundChat]";
    }

    public String useChatRequest(String prompt) {
        List<ChatMessage> messages = List.of(
                SystemMessage.from("你是Java程序员，回答问题是简洁风格"),
                UserMessage.from(prompt));

        ChatRequest request = ChatRequest.builder()
                .messages(messages)
                .temperature(0.7)
                .maxOutputTokens(100)
                .build();

        return openAiChatModel.chat(request).aiMessage().text() + "[from useChatRequest]";
    }

    /**
     * 使用图片理解模型根据提示词处理图片
     *
     * @param imageUrl 图片URL
     * @param prompt   图片处理提示词
     * @return 处理结果或错误信息
     */
    public String useImage(String imageUrl, String prompt) {
        try {
            logger.info("开始处理图片: {}", imageUrl);

            // 使用ImageUtils类来创建Image对象，这样可以确保图片数据被正确加载
            Image image = ImageUtils.createImageFromUrl(imageUrl);

            // 验证图片是否成功加载（通过检查base64数据是否存在且有一定长度）
            String base64Data = ImageUtils.getImageBase64(image);
            if (base64Data == null || base64Data.isEmpty() || base64Data.length() < 10) {
                logger.error("图片加载失败：Base64数据无效或为空");
                return "图片加载失败，请检查URL或网络连接[from useImage]";
            }

            logger.info("图片成功加载，Base64数据长度: {} 字符", base64Data.length());

            // 创建图片内容
            ImageContent imageContent = new ImageContent(image, ImageContent.DetailLevel.HIGH);

            // 用户提问
            UserMessage messages = UserMessage.from(List.of(
                    TextContent.from(prompt),
                    imageContent));

            // 调用模型进行处理
            logger.info("将图片内容发送给模型处理...");
            String result = imageVLModel.chat(messages).aiMessage().text();

            logger.info("模型返回结果: {}", result);
            return result + "[from useImage]";
        } catch (Exception e) {
            logger.error("处理图片时发生错误: {}", e.getMessage(), e);
            return "处理图片时发生错误: " + e.getMessage() + "[from useImage]";
        }
    }

    /**
     * 使用通义千问qwen3-image-plus模型生成图片
     *
     * @param prompt 图片生成提示词
     * @return 生成的图片URL或相关信息
     */
    public String generateImage(String prompt, int imageNum) {
        try {
            logger.info("开始生成图片，提示词: {}", prompt);

            // 使用imageGenModel生成图片
            Response<List<Image>> result = imageGenModel.generate(prompt, imageNum);

            logger.info("图片生成成功，结果: {}", result);
            return result + "[from generateImage]";
        } catch (Exception e) {
            logger.error("生成图片时发生错误: {}", e.getMessage(), e);
            return "生成图片时发生错误: " + e.getMessage() + "[from generateImage]";
        }
    }

    public String editImage(List<String> imageUrls, String prompt) {
        MultiModalConversation conv = new MultiModalConversation();

        var contents = new ArrayList<Map<String, Object>>();
        for (String imageUrl : imageUrls) {
            contents.add(Collections.singletonMap("image", imageUrl));
        }
        contents.add(Collections.singletonMap("text", prompt));

        MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                .content(contents)
                .build();

        // qwen-image-edit-plus支持输出1-6张图片，此处以两张为例
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("watermark", false);
        parameters.put("negative_prompt", " ");
        parameters.put("n", 2);
        parameters.put("prompt_extend", true);
        // 仅当输出图像数量n=1时支持设置size参数，否则会报错
        // parameters.put("size", "1024*2048");

        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(imageEditModelParam.getApiKey())
                .model(imageEditModelParam.getModelName())
                .messages(Collections.singletonList(userMessage))
                .parameters(parameters)
                .build();

        try {
            MultiModalConversationResult result = conv.call(param);
            return result + "[from editImage]";
        } catch (Exception e) {
            logger.error("编辑图片时发生错误: {}", e.getMessage(), e);
            return "编辑图片时发生错误: " + e.getMessage() + "[from editImage]";
        }
    }

    /**
     * 2. 低级API，手动添加ChatMessage到ChatMemory，实现聊天记忆功能
     *
     * @param prompt 模板中的变量
     * @return 助手生成的回答
     */
    public String lowLevelAddChatMessageToChatMemory(String prompt) {
        // 创建一个ChatMemory实例，通过token数量限制记忆长度
        if (null == chatMemory) {
            chatMemory = Tools.createRamChatMemoryInstance();
        }

        // 每一次的请求都存入添加到ChatMemory中
        chatMemory.add(UserMessage.from(prompt));
        // 聊天
        AiMessage answer = openAiChatModel.chat(chatMemory.messages()).aiMessage();
        // 每一次的响应都存入添加到ChatMemory中
        chatMemory.add(answer);

        logger.info("响应：" + answer.text());
        return answer.text() + "[from lowLevelAddChatMessageToChatMemory]";
    }

    /**
     * 4. 高级API，基于内存的全局记忆
     *
     * @param prompt
     * @return
     */
    public String highLevelRamGlobal(String prompt) {
        String answer = assistant.simpleChat(prompt);
        logger.info("响应：" + answer);
        return answer + "[from highLevelRamGlobal]";
    }

    /**
     * 5. 高级API，基于内存的用户记忆
     *
     * @param userID
     * @param prompt
     * @return
     */
    public String highLevelRamByUserID(int userID, String prompt) {
        String secondAnswer = assistant.chatByMemoryId(userID, prompt);
        logger.info("响应：" + secondAnswer);
        return secondAnswer + "[from highLevelRamByUserID]";
    }

    /**
     * 6. 高级API，基于数据库的全局记忆
     *
     * @param prompt
     * @return
     */
    public String highLevelDbGlobal(String prompt) {
        String secondAnswer = assistant.simpleChat(prompt);
        logger.info("响应：" + secondAnswer);
        return secondAnswer + "[from highLevelDbGlobal]";
    }

    /**
     * 7. 高级API，基于数据库的用户记忆
     *
     * @param userID
     * @param prompt
     * @return
     */
    public String highLevelDbByUserID(int userID, String prompt) {
        String answer = assistant.chatByMemoryId(userID, prompt);
        logger.info("响应：" + answer);
        return answer + "[from highLevelDbByUserID]";

    }

}

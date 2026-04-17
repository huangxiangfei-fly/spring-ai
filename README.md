# Spring AI 智能代理平台

基于 **Spring AI Alibaba** 和 **LangChain4j** 双框架的 AI 应用开发平台。

## 🚀 技术栈

### AI 框架
- **Spring AI Alibaba 1.1.2.2** - 阿里系 AI 框架（DashScope 通义千问）
- **LangChain4j 1.0.1-beta6** - Java 版 LangChain
- **Ollama** - 本地模型支持

### 后端技术
- **Spring Boot 3.5.11** + **JDK 21**
- **MyBatis Plus** + **MySQL** - 数据持久化
- **Redis** - 缓存与向量存储
- **Sa-Token** - 权限认证

## ✨ 核心功能

- **智能对话** - 多轮对话、流式响应、聊天记忆
- **RAG 检索增强** - 文档向量化、语义检索、答案生成
- **Function Calling** - 天气、计算器等工具调用
- **Agent 智能体** - ReAct 推理、人工审批流程
- **多模态** - 文生图、图像编辑、视觉理解
- **权限管理** - RBAC 用户角色权限控制

## 🚀 快速开始

### 环境要求
- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+

### 启动步骤
1. 配置 API Key（`application.yml` 中的 `spring.ai.dashscope.api-key`）
2. 初始化数据库：执行 `src/main/resources/sql/` 下的 SQL 脚本
3. 启动 Redis
4. 运行项目：`mvn spring-boot:run`
5. 访问：http://localhost:8080/index.html

## 📖 主要 API

| 模块 | 接口前缀 | 说明 |
|------|---------|------|
| 认证 | `/auth/*` | 登录、登出 |
| 聊天 | `/ali/chat/*` | 基础对话、流式响应 |
| RAG | `/ali/rag/*` | 文档检索增强生成 |
| 工具 | `/ali/tool/*` | Function Calling |
| LangChain4j | `/qwen/*` | LangChain4j 实现 |

详细接口请参考 `src/main/resources/*.http` 文件。

## 公众号：淹死的虾
---

**Made with ❤️ by BigFly AI Team**

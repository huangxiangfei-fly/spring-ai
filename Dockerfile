# 第一阶段：构建应用
FROM maven:3.8-eclipse-temurin-21 AS builder

WORKDIR /app

# 先复制 pom.xml，利用 Maven 缓存加速构建
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# 复制源码并打包（跳过测试）
COPY src ./src
RUN mvn package -DskipTests -B

# 第二阶段：运行时镜像
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="spring-ai"

WORKDIR /app

# 从构建阶段复制 JAR 包
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 设置 JVM 参数和启动命令
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

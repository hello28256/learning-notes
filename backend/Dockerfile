# 使用 Maven 构建
FROM maven:3.8-openjdk-17-slim AS builder

WORKDIR /app

# 复制 pom.xml 并下载依赖（利用 Docker 缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests

# 运行时镜像
FROM openjdk:17.0.2-jdk-slim

WORKDIR /app

# 创建上传目录
RUN mkdir -p /app/uploads

# 从构建阶段复制 JAR 文件
COPY --from=builder /app/target/learning-notes-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
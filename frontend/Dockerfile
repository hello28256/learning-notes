# 构建阶段
FROM node:18.20 AS builder

WORKDIR /app

# 复制 package.json 和 package-lock.json
COPY package*.json ./

# 安装依赖（修复npm bug）
RUN rm -f package-lock.json && npm install

# 复制源代码
COPY . .

# 构建应用
RUN npm run build

# 运行时镜像
FROM nginx:alpine

# 复制构建产物到 Nginx
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# Nginx 会自动启动
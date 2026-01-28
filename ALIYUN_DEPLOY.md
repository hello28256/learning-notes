# 学习笔记管理系统 - 阿里云容器镜像服务部署指南

## 一、整体部署流程

```
Mac本地构建 → 推送镜像到阿里云ACR → ECS拉取镜像 → ECS运行服务
```

## 二、Mac本地环境准备

### 2.1 安装Docker Desktop
```bash
# 下载并安装Docker Desktop for Mac
# https://www.docker.com/products/docker-desktop/

# 验证安装
docker --version
docker-compose --version
```

### 2.2 配置阿里云容器镜像服务

#### 2.2.1 创建阿里云容器镜像服务实例
1. 登录阿里云控制台：https://cr.console.aliyun.com/
2. 进入**容器镜像服务ACR**
3. 创建实例（如果还没有）
   - 选择地域：建议选择与ECS相同的地域
   - 实例类型：选择**个人版**（免费）或**企业版**
   - 实例名称：`learning-notes-registry`

#### 2.2.2 创建命名空间
1. 在ACR控制台左侧菜单选择**命名空间**
2. 点击**创建命名空间**
   - 命名空间名称：`learning-notes`
   - 描述：学习笔记管理系统

#### 2.2.3 创建镜像仓库
1. 在ACR控制台左侧菜单选择**镜像仓库**
2. 点击**创建镜像仓库**
   - 仓库类型：**私有**
   - 命名空间：`learning-notes`
   - 仓库名称：`backend`（后端）
   - 描述：学习笔记后端服务

3. 重复创建前端仓库：
   - 仓库类型：**私有**
   - 命名空间：`learning-notes`
   - 仓库名称：`frontend`
   - 描述：学习笔记前端服务

#### 2.2.4 获取登录信息
1. 在ACR控制台右上角点击**登录凭证**
2. 记录以下信息：
   - 登录地址：`registry.cn-<region>.aliyuncs.com`
   - 命名空间：`learning-notes`
   - 用户名：`<你的阿里云账号>`
   - 密码：`<你的密码>`

## 三、Mac本地构建和推送镜像

### 3.1 登录阿里云容器镜像服务

```bash
# 登录阿里云ACR
docker login registry.cn-<region>.aliyuncs.com -u <你的阿里云账号> -p <你的密码>

# 示例（北京地域）：
docker login registry.cn-beijing.aliyuncs.com -u 1234567890 -p your_password
```

### 3.2 构建后端镜像

```bash
cd /path/to/learning-notes

# 构建后端镜像
docker build -t learning-notes-backend:latest ./backend

# 或者指定版本
docker build -t learning-notes-backend:v1.0.0 ./backend

# 查看镜像
docker images | grep learning-notes-backend
```

### 3.3 构建前端镜像

```bash
# 构建前端镜像
docker build -t learning-notes-frontend:latest ./frontend

# 或者指定版本
docker build -t learning-notes-frontend:v1.0.0 ./frontend

# 查看镜像
docker images | grep learning-notes-frontend
```

### 3.4 本地测试镜像

#### 3.4.1 启动MySQL容器
```bash
# 启动MySQL
docker run -d \
  --name learning-notes-mysql \
  -e MYSQL_ROOT_PASSWORD=root123456 \
  -e MYSQL_DATABASE=learning_notes \
  -p 3306:3306 \
  mysql:8.0 \
  --default-authentication-plugin=mysql_native_password \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci

# 等待MySQL启动
sleep 10

# 导入数据库结构
docker exec -i learning-notes-mysql mysql -uroot -proot123456 < database/schema.sql
```

#### 3.4.2 启动后端容器
```bash
# 启动后端
docker run -d \
  --name learning-notes-backend \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/learning_notes?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root123456 \
  -e FILE_UPLOAD_DIR=/app/uploads \
  -p 8080:8080 \
  learning-notes-backend:latest

# 查看日志
docker logs -f learning-notes-backend
```

#### 3.4.3 启动前端容器
```bash
# 启动前端
docker run -d \
  --name learning-notes-frontend \
  -p 80:80 \
  learning-notes-frontend:latest

# 查看日志
docker logs -f learning-notes-frontend
```

#### 3.4.4 测试访问
```bash
# 测试后端API
curl http://localhost:8080/api/notes

# 测试前端
# 浏览器访问: http://localhost
```

### 3.5 打包镜像（可选）

如果需要将镜像打包成文件传输：

```bash
# 打包后端镜像
docker save learning-notes-backend:latest -o learning-notes-backend.tar

# 打包前端镜像
docker save learning-notes-frontend:latest -o learning-notes-frontend.tar

# 压缩
gzip learning-notes-backend.tar
gzip learning-notes-frontend.tar
```

## 四、推送镜像到阿里云ACR

### 4.1 标记镜像

```bash
# 获取阿里云ACR登录地址
# 例如：registry.cn-beijing.aliyuncs.com

# 标记后端镜像
docker tag learning-notes-backend:latest registry.cn-beijing.aliyuncs.com/learning-notes/learning-notes-backend:latest
docker tag learning-notes-backend:latest registry.cn-beijing.aliyuncs.com/learning-notes/learning-notes-backend:v1.0.0

# 标记前端镜像
docker tag learning-notes-frontend:latest registry.cn-beijing.aliyuncs.com/learning-notes/learning-notes-frontend:latest
docker tag learning-notes-frontend:latest registry.cn-beijing.aliyuncs.com/learning-notes/learning-notes-frontend:v1.0.0
```

### 4.2 推送镜像

```bash
# 推送后端镜像
docker push registry.cn-beijing.aliyuncs.com/learning-notes/learning-notes-backend:latest
docker push registry.cn-beijing.aliyuncs.com/learning-notes/learning-notes-backend:v1.0.0

# 推送前端镜像
docker push registry.cn-beijing.aliyuncs.com/learning-notes/learning-notes-frontend:latest
docker push registry.cn-beijing.aliyuncs.com/learning-notes/learning-notes-frontend:v1.0.0
```

### 4.3 验证推送

在阿里云ACR控制台查看：
1. 进入**镜像仓库**
2. 选择命名空间 `learning-notes`
3. 查看 `backend` 和 `frontend` 仓库
4. 确认镜像已成功推送

## 五、ECS服务器部署

### 5.1 ECS环境准备

#### 5.1.1 安装Docker
```bash
# Ubuntu/Debian
apt update && apt install -y docker.io
systemctl enable docker
systemctl start docker

# CentOS/RHEL
yum install -y docker-ce docker-ce-cli containerd.io
systemctl enable docker
systemctl start docker
```

#### 5.1.2 登录阿里云ACR
```bash
# 登录阿里云ACR
docker login registry.cn-beijing.aliyuncs.com -u <你的阿里云账号> -p <你的密码>
```

### 5.2 拉取镜像

```bash
# 拉取后端镜像
docker pull registry.cn-beijing.aliyuncs.com/learning-notes/learning-notes-backend:latest

# 拉取前端镜像
docker pull registry.cn-beijing.aliyuncs.com/learning-notes/learning-notes-frontend:latest
```

### 5.3 创建Docker Compose文件

在ECS上创建 `docker-compose.yml` 文件：

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: learning-notes-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root123456
      MYSQL_DATABASE: learning_notes
      MYSQL_CHARSET: utf8mb4
      MYSQL_COLLATION: utf8mb4_unicode_ci
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./database/schema.sql:/docker-entrypoint-initdb.d/schema.sql
    command:
      --default-authentication-plugin=mysql_native_password
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
    networks:
      - learning-notes-network
    restart: unless-stopped

  backend:
    image: registry.cn-beijing.aliyuncs.com/learning-notes/learning-notes-backend:latest
    container_name: learning-notes-backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/learning_notes?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root123456
      FILE_UPLOAD_DIR: /app/uploads
    volumes:
      - uploads_data:/app/uploads
    depends_on:
      - mysql
    networks:
      - learning-notes-network
    restart: unless-stopped

  frontend:
    image: registry.cn-beijing.aliyuncs.com/learning-notes/learning-notes-frontend:latest
    container_name: learning-notes-frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    networks:
      - learning-notes-network
    restart: unless-stopped

volumes:
  mysql_data:
  uploads_data:

networks:
  learning-notes-network:
    driver: bridge
```

### 5.4 启动服务

```bash
cd /root/learning-notes

# 创建上传目录
mkdir -p uploads

# 启动服务
docker-compose up -d

# 查看状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 5.5 测试访问

```bash
# 测试后端API
curl http://localhost:8080/api/notes

# 测试前端
# 浏览器访问: http://你的ECS公网IP
```

## 六、自动化部署脚本

### 6.1 Mac本地构建和推送脚本

创建 `build-and-push.sh` 脚本：

```bash
#!/bin/bash

# 配置
ALIYUN_REGION="cn-beijing"
ALIYUN_ACCOUNT="你的阿里云账号"
ALIYUN_PASSWORD="你的阿里云密码"
NAMESPACE="learning-notes"
VERSION="v1.0.0"

# 登录阿里云ACR
echo "登录阿里云ACR..."
docker login registry.cn-${ALIYUN_REGION}.aliyuncs.com -u ${ALIYUN_ACCOUNT} -p ${ALIYUN_PASSWORD}

# 构建后端镜像
echo "构建后端镜像..."
docker build -t ${NAMESPACE}-backend:${VERSION} ./backend
docker tag ${NAMESPACE}-backend:${VERSION} registry.cn-${ALIYUN_REGION}.aliyuncs.com/${NAMESPACE}/${NAMESPACE}-backend:${VERSION}
docker tag ${NAMESPACE}-backend:${VERSION} registry.cn-${ALIYUN_REGION}.aliyuncs.com/${NAMESPACE}/${NAMESPACE}-backend:latest

# 构建前端镜像
echo "构建前端镜像..."
docker build -t ${NAMESPACE}-frontend:${VERSION} ./frontend
docker tag ${NAMESPACE}-frontend:${VERSION} registry.cn-${ALIYUN_REGION}.aliyuncs.com/${NAMESPACE}/${NAMESPACE}-frontend:${VERSION}
docker tag ${NAMESPACE}-frontend:${VERSION} registry.cn-${ALIYUN_REGION}.aliyuncs.com/${NAMESPACE}/${NAMESPACE}-frontend:latest

# 推送镜像
echo "推送后端镜像..."
docker push registry.cn-${ALIYUN_REGION}.aliyuncs.com/${NAMESPACE}/${NAMESPACE}-backend:${VERSION}
docker push registry.cn-${ALIYUN_REGION}.aliyuncs.com/${NAMESPACE}/${NAMESPACE}-backend:latest

echo "推送前端镜像..."
docker push registry.cn-${ALIYUN_REGION}.aliyuncs.com/${NAMESPACE}/${NAMESPACE}-frontend:${VERSION}
docker push registry.cn-${ALIYUN_REGION}.aliyuncs.com/${NAMESPACE}/${NAMESPACE}-frontend:latest

echo "部署完成！"
```

### 6.2 ECS部署脚本

创建 `deploy-ecs-aliyun.sh` 脚本：

```bash
#!/bin/bash

# 配置
ALIYUN_REGION="cn-beijing"
ALIYUN_ACCOUNT="你的阿里云账号"
ALIYUN_PASSWORD="你的阿里云密码"
NAMESPACE="learning-notes"

# 登录阿里云ACR
echo "登录阿里云ACR..."
docker login registry.cn-${ALIYUN_REGION}.aliyuncs.com -u ${ALIYUN_ACCOUNT} -p ${ALIYUN_PASSWORD}

# 拉取镜像
echo "拉取后端镜像..."
docker pull registry.cn-${ALIYUN_REGION}.aliyuncs.com/${NAMESPACE}/${NAMESPACE}-backend:latest

echo "拉取前端镜像..."
docker pull registry.cn-${ALIYUN_REGION}.aliyuncs.com/${NAMESPACE}/${NAMESPACE}-frontend:latest

# 创建上传目录
mkdir -p uploads

# 启动服务
echo "启动服务..."
docker-compose up -d

# 查看状态
echo "服务状态:"
docker-compose ps

echo "部署完成！"
echo "访问地址: http://你的ECS公网IP"
```

### 6.3 使用脚本

```bash
# Mac本地执行
chmod +x build-and-push.sh
./build-and-push.sh

# ECS服务器执行
chmod +x deploy-ecs-aliyun.sh
./deploy-ecs-aliyun.sh
```

## 七、常见问题

### 7.1 镜像拉取失败

```bash
# 检查网络连接
ping registry.cn-beijing.aliyuncs.com

# 检查登录状态
docker logout
docker login registry.cn-beijing.aliyuncs.com -u <账号> -p <密码>

# 重新拉取
docker pull registry.cn-beijing.aliyuncs.com/learning-notes/learning-notes-backend:latest
```

### 7.2 镜像构建失败

```bash
# 查看构建日志
docker build -t learning-notes-backend:latest ./backend --no-cache

# 检查Dockerfile语法
docker build -t learning-notes-backend:latest ./backend --dry-run
```

### 7.3 ECS无法访问镜像

```bash
# 检查ECS安全组
# 确保80和8080端口已开放

# 检查Docker Compose配置
docker-compose config

# 查看容器日志
docker-compose logs -f
```

## 八、镜像版本管理

### 8.1 使用语义化版本

```bash
# v1.0.0 - 初始版本
# v1.0.1 - 修复bug
# v1.1.0 - 新增功能
# v2.0.0 - 重大更新

# 构建特定版本
docker build -t learning-notes-backend:v1.1.0 ./backend
docker push registry.cn-beijing.aliyuncs.com/learning-notes/learning-notes-backend:v1.1.0
```

### 8.2 清理旧镜像

```bash
# Mac本地清理
docker image prune -a

# ECS清理
docker image prune -a

# 清理未使用的镜像
docker system prune -a
```

## 九、监控和维护

### 9.1 查看镜像列表

```bash
# Mac本地
docker images

# 阿里云ACR控制台
# https://cr.console.aliyun.com/
```

### 9.2 查看容器状态

```bash
# 查看所有容器
docker ps -a

# 查看运行中的容器
docker ps

# 查看容器资源使用
docker stats
```

### 9.3 日志管理

```bash
# 查看后端日志
docker logs -f learning-notes-backend

# 查看前端日志
docker logs -f learning-notes-frontend

# 查看MySQL日志
docker logs -f learning-notes-mysql
```

## 十、安全建议

### 10.1 使用强密码
- 数据库密码：使用强密码，不要使用默认密码
- 阿里云ACR密码：使用强密码，定期更换

### 10.2 限制访问
- 生产环境不要开放8080和3306端口
- 只开放80（HTTP）和443（HTTPS）端口

### 10.3 定期更新
- 定期更新Docker镜像
- 定期更新系统和依赖

---

**部署完成！** 你的学习笔记管理系统已经成功部署到阿里云容器镜像服务。

访问地址：`http://你的ECS公网IP`

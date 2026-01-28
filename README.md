# 学习笔记管理系统

一个基于 Vue + Spring Boot + MySQL 的学习笔记管理系统，支持上传 Markdown 文件并解析显示。

## 技术栈

- **前端**: Vue 3 + Vite + Element Plus + Markdown-it
- **后端**: Spring Boot 3.2 + JPA + MySQL
- **数据库**: MySQL 8.0

## 功能特性

- ✅ 上传 Markdown 文件（支持 .md, .txt, .markdown）
- ✅ 自动解析 Markdown 为 HTML 显示
- ✅ 笔记分类管理
- ✅ 笔记标签管理
- ✅ 笔记搜索（按标题）
- ✅ 笔记浏览次数统计
- ✅ 笔记删除和更新
- ✅ 响应式前端界面

## 项目结构

```
learning-notes/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/learning/notes/
│   │   ├── controller/        # 控制器
│   │   ├── service/          # 服务层
│   │   ├── repository/       # 数据访问层
│   │   ├── entity/           # 实体类
│   │   ├── config/           # 配置类
│   │   └── LearningNotesApplication.java  # 启动类
│   ├── src/main/resources/
│   │   ├── application.yml    # 主配置
│   │   └── application-dev.yml # 开发配置
│   └── pom.xml               # Maven 依赖
├── frontend/                  # Vue 前端
│   ├── src/
│   │   ├── components/       # 组件
│   │   ├── views/           # 页面
│   │   ├── router/          # 路由
│   │   ├── utils/           # 工具类
│   │   ├── App.vue          # 根组件
│   │   └── main.js          # 入口文件
│   ├── package.json         # npm 依赖
│   └── vite.config.js       # Vite 配置
├── database/                 # 数据库脚本
│   └── schema.sql          # 表结构
└── README.md               # 说明文档
```

## 快速开始

### 1. 环境准备

- JDK 17+
- Maven 3.6+
- Node.js 16+
- MySQL 8.0+
- npm 或 yarn

### 2. 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE learning_notes CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行数据库脚本：
```bash
mysql -u root -p learning_notes < database/schema.sql
```

3. 修改后端配置文件 `backend/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/learning_notes?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password  # 修改为你的数据库密码
```

### 3. 后端启动

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端默认运行在 http://localhost:8080

### 4. 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 http://localhost:5173

## API 接口

### 笔记管理

- `POST /api/notes/upload` - 上传 Markdown 文件
- `GET /api/notes` - 获取所有笔记
- `GET /api/notes/{id}` - 获取单个笔记详情
- `GET /api/notes/search?title={title}` - 按标题搜索
- `GET /api/notes/category/{category}` - 按分类获取
- `GET /api/notes/tag/{tag}` - 按标签获取
- `GET /api/notes/categories` - 获取所有分类
- `GET /api/notes/tags` - 获取所有标签
- `PUT /api/notes/{id}` - 更新笔记
- `DELETE /api/notes/{id}` - 删除笔记

### 请求示例

**上传文件**:
```bash
curl -X POST http://localhost:8080/api/notes/upload \
  -F "file=@example.md" \
  -F "category=编程学习" \
  -F "tags=Java,Spring"
```

**获取所有笔记**:
```bash
curl http://localhost:8080/api/notes
```

## 使用说明

1. **上传笔记**: 在前端页面点击"上传文件"按钮，选择 Markdown 文件，填写分类和标签后上传
2. **查看笔记**: 点击笔记列表中的任意笔记，即可查看详细内容（已解析为 HTML）
3. **搜索笔记**: 使用搜索框按标题查找笔记
4. **分类浏览**: 点击分类标签查看相关笔记
5. **标签浏览**: 点击标签查看相关笔记

## 开发说明

### 后端开发

- 使用 Lombok 简化代码
- 使用 JPA 进行数据库操作
- 使用 FlexMark 解析 Markdown
- 使用 Hutool 工具类

### 前端开发

- Vue 3 Composition API
- Element Plus UI 组件库
- Markdown-it 解析 Markdown
- Axios 进行 HTTP 请求

## Docker 部署

### 1. Docker Compose 部署

本项目支持使用 Docker Compose 一键部署：

```bash
# 1. 安装 Docker 和 Docker Compose
# Ubuntu/Debian:
apt update && apt install -y docker.io
curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# 2. 启动服务
docker-compose up -d

# 3. 访问应用
# 前端: http://localhost:5173
# 后端: http://localhost:8080/api/notes
```

### 2. 阿里云ECS部署

#### 2.1 直接部署（推荐）
详细的阿里云ECS部署指南请查看：
- **快速部署**: [QUICK_DEPLOY.md](QUICK_DEPLOY.md)
- **详细文档**: [DEPLOYMENT.md](DEPLOYMENT.md)

快速命令：
```bash
# 上传项目到ECS
scp -r /path/to/learning-notes root@ECS_IP:/root/

# 在ECS上执行
cd /root/learning-notes
./deploy-ecs.sh install
./deploy-ecs.sh start
```

#### 2.2 阿里云容器镜像服务部署（ACR）
使用阿里云容器镜像服务（ACR）进行部署：

**Mac本地构建和推送：**
```bash
# 1. 修改配置
# 编辑 build-and-push.sh，修改 ALIYUN_ACCOUNT 和 ALIYUN_PASSWORD

# 2. 运行脚本
./build-and-push.sh
```

**ECS拉取和运行：**
```bash
# 1. 修改配置
# 编辑 deploy-ecs-aliyun.sh，修改 ALIYUN_ACCOUNT 和 ALIYUN_PASSWORD

# 2. 运行脚本
./deploy-ecs-aliyun.sh
```

详细文档：[ALIYUN_DEPLOY.md](ALIYUN_DEPLOY.md)

### 3. 环境变量配置

在 `docker-compose.yml` 中可以配置以下环境变量：

```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/learning_notes
  SPRING_DATASOURCE_USERNAME: root
  SPRING_DATASOURCE_PASSWORD: root123456
  FILE_UPLOAD_DIR: /app/uploads
```

## 注意事项

1. 确保 MySQL 服务已启动
2. 确保上传目录有写入权限
3. 文件大小限制为 50MB
4. 支持的文件扩展名：.md, .txt, .markdown
5. **生产环境建议**：
   - 修改默认数据库密码
   - 配置SSL证书
   - 设置定期备份
   - 限制访问IP

## 许可证

MIT License
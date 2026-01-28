# 快速开始指南

## 方式一：使用 Docker Compose（推荐）

### 1. 环境准备

确保已安装：
- Docker
- Docker Compose

### 2. 启动服务

```bash
cd ~/Codes/learning-notes

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 3. 访问应用

- **前端**: http://localhost:5173
- **后端**: http://localhost:8080
- **MySQL**: localhost:3306

### 4. 数据库配置

数据库信息：
- 用户名: `root`
- 密码: `root123456`
- 数据库: `learning_notes`

## 方式二：本地开发环境

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

### 3. 启动后端

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端默认运行在 http://localhost:8080

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 http://localhost:5173

## 方式三：使用部署脚本

### 1. 构建项目

```bash
cd ~/Codes/learning-notes
./deploy.sh build
```

### 2. 启动服务

```bash
./deploy.sh start
```

### 3. 其他命令

```bash
# 停止服务
./deploy.sh stop

# 重启服务
./deploy.sh restart

# 查看日志
./deploy.sh logs

# 清理数据
./deploy.sh clean

# 显示帮助
./deploy.sh help
```

## 使用示例

### 1. 上传 Markdown 笔记

1. 访问 http://localhost:5173
2. 点击"上传笔记"按钮
3. 选择 Markdown 文件（.md, .txt, .markdown）
4. 填写标题、分类和标签
5. 点击"开始上传"

### 2. 查看笔记

1. 在首页点击任意笔记卡片
2. 查看笔记详情（已自动解析为 HTML）
3. 支持浏览次数统计

### 3. 搜索和筛选

- **搜索**: 在首页搜索框输入关键词
- **分类浏览**: 点击分类标签
- **标签浏览**: 点击标签

### 4. 编辑和删除

1. 进入笔记详情页
2. 点击"编辑"按钮修改信息
3. 点击"删除"按钮删除笔记

## 示例文件

项目根目录下有一个示例文件 `example.md`，可以用于测试上传功能。

## 常见问题

### Q: 上传文件失败？

A: 检查：
1. 文件大小是否超过 50MB
2. 文件扩展名是否为 .md, .txt, .markdown
3. 上传目录是否有写入权限

### Q: 数据库连接失败？

A: 检查：
1. MySQL 服务是否启动
2. 数据库配置是否正确
3. 数据库用户名和密码是否正确

### Q: 前端无法访问后端？

A: 检查：
1. 后端服务是否启动
2. 端口是否被占用（默认 8080）
3. 跨域配置是否正确

### Q: 如何修改上传文件大小限制？

A: 修改以下配置：
- 后端：`backend/src/main/resources/application.yml` 中的 `max-file-size` 和 `max-request-size`
- 前端：`frontend/src/views/Upload.vue` 中的提示信息

## 技术栈说明

### 后端
- **Spring Boot 3.2**: Web 框架
- **JPA**: 数据库 ORM
- **MySQL**: 数据库
- **FlexMark**: Markdown 解析器
- **Hutool**: 工具类库

### 前端
- **Vue 3**: 渐进式 JavaScript 框架
- **Vite**: 构建工具
- **Element Plus**: UI 组件库
- **Markdown-it**: Markdown 解析器
- **Axios**: HTTP 客户端

## 项目结构

```
learning-notes/
├── backend/          # Spring Boot 后端
├── frontend/         # Vue 前端
├── database/         # 数据库脚本
├── uploads/          # 上传文件目录（自动生成）
├── deploy.sh         # 部署脚本
├── docker-compose.yml # Docker 编排
├── example.md        # 示例文件
├── README.md         # 项目说明
└── QUICKSTART.md     # 快速开始（本文件）
```

## 下一步

1. 尝试上传自己的 Markdown 笔记
2. 添加自定义分类和标签
3. 修改前端样式
4. 添加更多功能（如笔记导出、分享等）

## 获取帮助

如有问题，请查看：
- [README.md](README.md) - 详细项目说明
- 项目源码中的注释
- 或联系项目维护者
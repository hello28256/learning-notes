# 学习笔记管理系统 - 阿里云ECS部署指南

## 一、ECS环境准备

### 1.1 购买ECS实例
- **操作系统**: Ubuntu 22.04 LTS 或 CentOS 7/8
- **配置建议**:
  - CPU: 2核
  - 内存: 4GB
  - 系统盘: 40GB
  - 数据盘: 50GB (用于存储上传文件和数据库)
- **带宽**: 1-5 Mbps (根据访问量调整)

### 1.2 安全组配置
在阿里云控制台配置安全组规则：

| 端口 | 协议 | 源IP | 说明 |
|------|------|------|------|
| 22   | TCP  | 你的IP | SSH管理 |
| 80   | TCP  | 0.0.0.0/0 | HTTP访问 |
| 443  | TCP  | 0.0.0.0/0 | HTTPS访问 (可选) |
| 8080 | TCP  | 你的IP | 后端API (可选，生产环境建议关闭) |
| 3306 | TCP  | 你的IP | MySQL (可选，生产环境建议关闭) |

## 二、服务器环境配置

### 2.1 连接ECS
```bash
# 通过SSH连接
ssh root@你的ECS公网IP

# 如果使用密钥登录
ssh -i /path/to/your-key.pem root@你的ECS公网IP
```

### 2.2 更新系统
```bash
# Ubuntu/Debian
apt update && apt upgrade -y

# CentOS/RHEL
yum update -y
```

### 2.3 安装Docker和Docker Compose

#### Ubuntu/Debian
```bash
# 安装Docker
apt install -y docker.io
systemctl enable docker
systemctl start docker

# 安装Docker Compose
curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# 验证安装
docker --version
docker-compose --version
```

#### CentOS/RHEL
```bash
# 安装Docker
yum install -y yum-utils
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
yum install -y docker-ce docker-ce-cli containerd.io
systemctl enable docker
systemctl start docker

# 安装Docker Compose
curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# 验证安装
docker --version
docker-compose --version
```

### 2.4 配置Docker镜像加速（可选，国内服务器推荐）
```bash
mkdir -p /etc/docker
cat > /etc/docker/daemon.json << EOF
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://registry.docker-cn.com"
  ]
}
EOF

systemctl daemon-reload
systemctl restart docker
```

## 三、项目部署

### 3.1 上传项目文件

#### 方法1: 使用SCP上传（推荐）
```bash
# 从本地上传项目到ECS
scp -r /path/to/learning-notes root@你的ECS公网IP:/root/

# 如果使用密钥
scp -i /path/to/your-key.pem -r /path/to/learning-notes root@你的ECS公网IP:/root/
```

#### 方法2: 使用Git克隆
```bash
# 在ECS上克隆项目
cd /root
git clone https://github.com/your-username/learning-notes.git
cd learning-notes
```

### 3.2 修改配置文件

#### 3.2.1 修改后端配置
编辑 `backend/src/main/resources/application.yml`：

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    # 修改为Docker容器内的MySQL地址
    url: jdbc:mysql://mysql:3306/learning_notes?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root123456  # 与docker-compose.yml中的密码一致
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true
    open-in-view: false

  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB

# 文件上传配置
file:
  upload:
    dir: /app/uploads
    allowed-extensions: .md,.txt,.markdown
    max-size: 52428800  # 50MB

# 日志配置
logging:
  level:
    com.learning.notes: DEBUG
    org.springframework.web: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

# Markdown 配置
markdown:
  parser:
    options:
      TASKS: "GITHUB_QUOTES,SMART_QUOTES,STRIKETHROUGH,TABLES,AUTOLINKS,LINKS,LISTS,HEADING,PARAGRAPH,INDENTED_CODE_BLOCK,FENCED_CODE_BLOCK,HTML_BLOCK,HTML_INLINE,IMAGE,EMPHASIS,STRONG,LIST_ITEM,LIST_MARKER,BLOCK_QUOTE,HR,SOFT_LINE_BREAK,EMOJI,DEFINITION_LINK"
      NO_TASKS: "EMOJI"
```

#### 3.2.2 修改docker-compose.yml（生产环境优化）

编辑 `docker-compose.yml`：

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
      - "3306:3306"  # 生产环境建议注释掉，只在容器内访问
    volumes:
      - mysql_data:/var/lib/mysql
      - ./database/schema.sql:/docker-entrypoint-initdb.d/schema.sql
    command:
      --default-authentication-plugin=mysql_native_password
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
    networks:
      - learning-notes-network
    restart: unless-stopped  # 自动重启

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: learning-notes-backend
    ports:
      - "8080:8080"  # 生产环境建议注释掉，只在容器内访问
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
    restart: unless-stopped  # 自动重启

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: learning-notes-frontend
    ports:
      - "80:80"  # 生产环境对外暴露80端口
    depends_on:
      - backend
    networks:
      - learning-notes-network
    restart: unless-stopped  # 自动重启

volumes:
  mysql_data:
  uploads_data:

networks:
  learning-notes-network:
    driver: bridge
```

### 3.3 构建和启动服务

#### 3.3.1 构建项目
```bash
------------------------------------------------------------
  0            /usr/lib/jvm/java-17-openjdk-amd64/bin/javac      1000      auto mode
* 1            /usr/lib/jvm/java-1.8.0-openjdk-amd64/bin/javac   800       manual mode
  2            /usr/lib/jvm/java-17-openjdk-amd64/bin/javac      1000      manual mode

Press <enter> to keep the current choice[*], or type selection number: 2
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/javac to provide /usr/bin/javac (javac) in manual mode
root@Ubuntu24:~# java -version
openjdk version "1.8.0_472"
OpenJDK Runtime Environment (build 1.8.0_472-8u472-ga-1~24.04-b08)
OpenJDK 64-Bit Server VM (build 25.472-b08, mixed mode)
cd /root/learning-notes

# 使用部署脚本构建
chmod +x deploy.sh
./deploy.sh build

# 或者手动构建
# 构建后端
cd backend
mvn clean package -DskipTests
cd ..

# 构建前端
cd frontend
npm install
npm run build
cd ..
```

#### 3.3.2 启动服务
```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql
```

### 3.4 验证部署

#### 3.4.1 检查服务状态
```bash
# 查看运行中的容器
docker ps

# 查看所有容器
docker ps -a

# 查看容器日志
docker logs learning-notes-backend
docker logs learning-notes-frontend
docker logs learning-notes-mysql
```

#### 3.4.2 测试访问
```bash
# 测试后端API
curl http://localhost:8080/api/notes

# 测试MySQL连接
docker exec -it learning-notes-mysql mysql -uroot -proot123456 -e "SHOW DATABASES;"
```

#### 3.4.3 浏览器访问
- **前端**: `http://你的ECS公网IP`
- **后端API**: `http://你的ECS公网IP:8080/api/notes` (如果开放了8080端口)

## 四、生产环境优化

### 4.1 配置域名和SSL证书

#### 4.1.1 配置域名解析
在阿里云DNS控制台添加A记录：
- 主机记录: `www` 或 `notes`
- 记录值: 你的ECS公网IP

#### 4.1.2 获取SSL证书（使用Let's Encrypt）
```bash
# 安装certbot
apt install -y certbot python3-certbot-nginx

# 获取证书（需要域名已解析）
certbot --nginx -d your-domain.com -d www.your-domain.com

# 自动续期
systemctl enable certbot.timer
```

#### 4.1.3 修改Nginx配置（支持HTTPS）
编辑 `frontend/nginx.conf`：

```nginx
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com www.your-domain.com;
    root /usr/share/nginx/html;
    index index.html;

    # SSL证书路径
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # Gzip 压缩
    gzip on;
    gzip_min_length 1k;
    gzip_comp_level 6;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
    gzip_vary on;

    # 防止 404 错误
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理
    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

### 4.2 数据备份

#### 4.2.1 数据库备份
```bash
# 创建备份目录
mkdir -p /root/backups

# 备份数据库
docker exec learning-notes-mysql mysqldump -uroot -proot123456 learning_notes > /root/backups/learning_notes_$(date +%Y%m%d_%H%M%S).sql

# 压缩备份
gzip /root/backups/learning_notes_*.sql

# 自动备份脚本（添加到crontab）
echo "0 2 * * * docker exec learning-notes-mysql mysqldump -uroot -proot123456 learning_notes | gzip > /root/backups/learning_notes_\$(date +\%Y\%m\%d_\%H\%M\%S).sql" >> /etc/crontab
```

#### 4.2.2 文件备份
```bash
# 备份上传文件
tar -czf /root/backups/uploads_$(date +%Y%m%d_%H%M%S).tar.gz uploads_data/

# 定期备份脚本
cat > /root/backup.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/root/backups"
DATE=$(date +%Y%m%d_%H%M%S)

# 备份数据库
docker exec learning-notes-mysql mysqldump -uroot -proot123456 learning_notes | gzip > $BACKUP_DIR/learning_notes_$DATE.sql.gz

# 备份上传文件
docker run --rm -v uploads_data:/data -v $BACKUP_DIR:/backup alpine tar czf /backup/uploads_$DATE.tar.gz -C /data .

# 删除30天前的备份
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete
find $BACKUP_DIR -name "*.tar.gz" -mtime +30 -delete
EOF

chmod +x /root/backup.sh

# 添加到crontab（每天凌晨2点执行）
echo "0 2 * * * /root/backup.sh" >> /etc/crontab
```

### 4.3 监控和日志

#### 4.3.1 查看服务状态
```bash
# 查看容器资源使用
docker stats

# 查看磁盘使用
df -h

# 查看内存使用
free -h
```

#### 4.3.2 日志管理
```bash
# 查看所有日志
docker-compose logs

# 查看特定服务日志
docker-compose logs backend

# 查看实时日志
docker-compose logs -f backend

# 清理旧日志
docker system prune -f
```

### 4.4 安全加固

#### 4.4.1 修改默认密码
```bash
# 修改MySQL密码
docker exec -it learning-notes-mysql mysql -uroot -proot123456 -e "ALTER USER 'root'@'%' IDENTIFIED BY 'YourNewStrongPassword123!';"

# 更新docker-compose.yml中的密码
# 更新application.yml中的密码
```

#### 4.4.2 限制访问
```bash
# 只允许特定IP访问MySQL（修改docker-compose.yml）
# 在mysql服务的command中添加：
# --bind-address=0.0.0.0

# 或者使用iptables限制
iptables -A INPUT -p tcp --dport 3306 -s 你的IP -j ACCEPT
iptables -A INPUT -p tcp --dport 3306 -j DROP
```

#### 4.4.3 定期更新
```bash
# 更新Docker镜像
docker-compose pull
docker-compose up -d

# 更新系统
apt update && apt upgrade -y
```

## 五、常见问题排查

### 5.1 端口冲突
```bash
# 查看端口占用
netstat -tlnp | grep :80
netstat -tlnp | grep :8080
netstat -tlnp | grep :3306

# 解决端口冲突
# 修改docker-compose.yml中的端口映射
```

### 5.2 构建失败
```bash
# 查看构建日志
docker-compose build --no-cache

# 清理Docker缓存
docker system prune -a

# 检查Dockerfile语法
```

### 5.3 数据库连接失败
```bash
# 检查MySQL容器状态
docker ps | grep mysql

# 查看MySQL日志
docker logs learning-notes-mysql

# 进入MySQL容器测试
docker exec -it learning-notes-mysql mysql -uroot -proot123456 -e "SHOW DATABASES;"
```

### 5.4 上传文件失败
```bash
# 检查上传目录权限
docker exec learning-notes-backend ls -la /app/uploads

# 检查磁盘空间
docker exec learning-notes-backend df -h /app/uploads

# 查看后端日志
docker-compose logs backend
```

### 5.5 前端无法访问
```bash
# 检查Nginx配置
docker exec learning-notes-frontend nginx -t

# 查看Nginx日志
docker-compose logs frontend

# 检查前端构建产物
docker exec learning-notes-frontend ls -la /usr/share/nginx/html
```

## 六、维护命令

### 6.1 常用命令
```bash
# 启动服务
docker-compose up -d

# 停止服务
docker-compose down

# 重启服务
docker-compose restart

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 重建服务
docker-compose up -d --build

# 清理所有数据（谨慎使用）
docker-compose down -v
```

### 6.2 数据管理
```bash
# 进入MySQL容器
docker exec -it learning-notes-mysql mysql -uroot -proot123456

# 查看数据库
SHOW DATABASES;
USE learning_notes;
SHOW TABLES;

# 查看数据
SELECT * FROM notes;
```

### 6.3 性能优化
```bash
# 限制容器资源
# 在docker-compose.yml中添加：
# deploy:
#   resources:
#     limits:
#       cpus: '1'
#       memory: 512M

# 使用Docker Compose v2
docker compose up -d  # 注意：是docker compose，不是docker-compose
```

## 七、部署完成检查清单

- [ ] ECS安全组配置完成（80、443端口开放）
- [ ] Docker和Docker Compose安装成功
- [ ] 项目文件上传到ECS
- [ ] 配置文件修改完成（application.yml、docker-compose.yml）
- [ ] 数据库密码已修改（非默认密码）
- [ ] 服务成功启动（docker-compose ps显示所有服务running）
- [ ] 前端可以正常访问（浏览器访问http://ECS_IP）
- [ ] 后端API可以访问（curl http://ECS_IP:8080/api/notes）
- [ ] 文件上传功能正常
- [ ] 数据库备份脚本配置完成
- [ ] 域名解析完成（如需要）
- [ ] SSL证书配置完成（如需要）
- [ ] 监控和日志配置完成

## 八、技术支持

如果遇到问题，可以：
1. 查看日志：`docker-compose logs -f`
2. 检查容器状态：`docker-compose ps`
3. 查看系统资源：`docker stats`
4. 检查网络连接：`curl http://localhost:8080/api/notes`

---

**部署完成！** 你的学习笔记管理系统已经成功部署到阿里云ECS。

访问地址：`http://你的ECS公网IP`

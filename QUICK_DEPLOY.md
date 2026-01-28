# 学习笔记管理系统 - 阿里云ECS快速部署指南

## 一、快速部署（5分钟完成）

### 1.1 上传项目到ECS

```bash
# 从本地电脑执行
scp -r /path/to/learning-notes root@你的ECS公网IP:/root/
```

### 1.2 在ECS上执行部署

```bash
# 连接到ECS
ssh root@你的ECS公网IP

# 进入项目目录
cd /root/learning-notes

# 安装Docker和Docker Compose（如果未安装）
# Ubuntu/Debian:
apt update && apt install -y docker.io
curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# CentOS/RHEL:
yum install -y docker-ce docker-ce-cli containerd.io
curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# 启动Docker
systemctl enable docker
systemctl start docker

# 运行部署脚本
./deploy-ecs.sh install
./deploy-ecs.sh start
```

### 1.3 访问应用

- **前端**: `http://你的ECS公网IP`
- **后端API**: `http://你的ECS公网IP:8080/api/notes`

## 二、常用命令

### 2.1 服务管理
```bash
# 启动服务
./deploy-ecs.sh start

# 停止服务
./deploy-ecs.sh stop

# 重启服务
./deploy-ecs.sh restart

# 查看状态
./deploy-ecs.sh status

# 查看日志
./deploy-ecs.sh logs
```

### 2.2 数据管理
```bash
# 清理数据（谨慎使用）
./deploy-ecs.sh clean
```

## 三、配置修改

### 3.1 修改数据库密码
编辑 `docker-compose.yml`：
```yaml
environment:
  MYSQL_ROOT_PASSWORD: 你的新密码  # 修改这里
```

编辑 `backend/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    password: 你的新密码  # 修改这里
```

### 3.2 修改端口
编辑 `docker-compose.yml`：
```yaml
services:
  mysql:
    ports:
      - "3307:3306"  # 修改MySQL端口

  backend:
    ports:
      - "8081:8080"  # 修改后端端口

  frontend:
    ports:
      - "8082:80"    # 修改前端端口
```

## 四、配置域名和SSL（可选）

### 4.1 配置域名解析
在阿里云DNS控制台添加A记录：
- 主机记录: `www` 或 `notes`
- 记录值: 你的ECS公网IP

### 4.2 获取SSL证书
```bash
# 安装certbot
apt install -y certbot python3-certbot-nginx

# 获取证书（需要域名已解析）
certbot --nginx -d your-domain.com -d www.your-domain.com
```

### 4.3 修改Nginx配置
编辑 `frontend/nginx.conf`，添加HTTPS支持（参考DEPLOYMENT.md）

## 五、数据备份

### 5.1 手动备份
```bash
# 备份数据库
docker exec learning-notes-mysql mysqldump -uroot -proot123456 learning_notes > backup_$(date +%Y%m%d).sql

# 备份上传文件
docker run --rm -v uploads_data:/data -v $(pwd):/backup alpine tar czf /backup/uploads_$(date +%Y%m%d).tar.gz -C /data .
```

### 5.2 自动备份
```bash
# 创建备份脚本
cat > /root/backup.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/root/backups"
mkdir -p $BACKUP_DIR
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

## 六、常见问题

### 6.1 端口被占用
```bash
# 查看端口占用
netstat -tlnp | grep :80
netstat -tlnp | grep :8080
netstat -tlnp | grep :3306

# 修改docker-compose.yml中的端口映射
```

### 6.2 构建失败
```bash
# 清理Docker缓存
docker system prune -a

# 重新构建
./deploy-ecs.sh install
```

### 6.3 无法访问
```bash
# 检查服务状态
./deploy-ecs.sh status

# 查看日志
./deploy-ecs.sh logs

# 检查安全组
# 确保阿里云安全组已开放80端口
```

### 6.4 数据库连接失败
```bash
# 检查MySQL容器
docker ps | grep mysql

# 查看MySQL日志
docker logs learning-notes-mysql

# 进入MySQL容器
docker exec -it learning-notes-mysql mysql -uroot -proot123456
```

## 七、安全建议

### 7.1 修改默认密码
```bash
# 修改MySQL密码
docker exec -it learning-notes-mysql mysql -uroot -proot123456 -e "ALTER USER 'root'@'%' IDENTIFIED BY 'YourNewStrongPassword123!';"

# 更新配置文件
# docker-compose.yml 和 application.yml
```

### 7.2 限制访问
- 生产环境不要开放8080和3306端口
- 只开放80（HTTP）和443（HTTPS）端口
- 使用强密码

### 7.3 定期更新
```bash
# 更新Docker镜像
docker-compose pull
docker-compose up -d

# 更新系统
apt update && apt upgrade -y
```

## 八、监控和维护

### 8.1 查看资源使用
```bash
# 查看容器资源使用
docker stats

# 查看磁盘空间
df -h

# 查看内存使用
free -h
```

### 8.2 查看日志
```bash
# 查看所有日志
docker-compose logs

# 查看特定服务日志
docker-compose logs backend
docker-compose logs frontend
docker-compose logs mysql

# 实时查看日志
docker-compose logs -f
```

### 8.3 重启服务
```bash
# 重启所有服务
docker-compose restart

# 重启特定服务
docker-compose restart backend
```

## 九、技术支持

如果遇到问题：

1. **查看日志**: `./deploy-ecs.sh logs`
2. **检查状态**: `./deploy-ecs.sh status`
3. **查看详细文档**: `cat DEPLOYMENT.md`
4. **检查安全组**: 确保阿里云安全组已开放80端口

## 十、部署完成检查清单

- [ ] ECS安全组配置完成（80端口开放）
- [ ] Docker和Docker Compose安装成功
- [ ] 项目文件上传到ECS
- [ ] 运行 `./deploy-ecs.sh install`
- [ ] 运行 `./deploy-ecs.sh start`
- [ ] 浏览器访问 `http://你的ECS公网IP`
- [ ] 文件上传功能正常
- [ ] 数据库备份脚本配置完成（可选）
- [ ] 域名解析完成（可选）
- [ ] SSL证书配置完成（可选）

---

**部署完成！** 你的学习笔记管理系统已经成功部署到阿里云ECS。

访问地址：`http://你的ECS公网IP`

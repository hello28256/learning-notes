#!/bin/bash

# 学习笔记管理系统部署脚本

set -e

echo "========================================"
echo "学习笔记管理系统部署脚本"
echo "========================================"

# 检查环境
check_environment() {
    echo "检查环境..."

    # 检查 Docker
    if ! command -v docker &> /dev/null; then
        echo "❌ 未安装 Docker，请先安装 Docker"
        exit 1
    fi

    # 检查 Docker Compose
    if ! command -v docker compose &> /dev/null; then
        echo "❌ 未安装 Docker Compose，请先安装 Docker Compose"
        exit 1
    fi

    echo "✅ Docker 环境检查通过"
}

# 配置数据库
setup_database() {
    echo "配置数据库..."

    # 检查 MySQL 配置
    if [ ! -f "backend/src/main/resources/application.yml" ]; then
        echo "❌ 配置文件不存在"
        exit 1
    fi

    echo "✅ 数据库配置完成"
}

# 构建后端
build_backend() {
    echo "构建后端..."

    cd backend

    # 检查 Maven
    if ! command -v mvn &> /dev/null; then
        echo "❌ 未安装 Maven，请先安装 Maven"
        exit 1
    fi

    # 编译打包
    mvn clean package -DskipTests

    if [ $? -eq 0 ]; then
        echo "✅ 后端构建成功"
    else
        echo "❌ 后端构建失败"
        exit 1
    fi

    cd ..
}

# 构建前端
build_frontend() {
    echo "构建前端..."

    cd frontend

    # 检查 Node.js
    if ! command -v node &> /dev/null; then
        echo "❌ 未安装 Node.js，请先安装 Node.js"
        exit 1
    fi

    # 安装依赖
    npm install

    # 构建
    npm run build

    if [ $? -eq 0 ]; then
        echo "✅ 前端构建成功"
    else
        echo "❌ 前端构建失败"
        exit 1
    fi

    cd ..
}

# 启动服务
start_services() {
    echo "启动服务..."

    # 创建上传目录
    mkdir -p uploads

    # 启动 Docker Compose
    docker compose up -d

    if [ $? -eq 0 ]; then
        echo "✅ 服务启动成功"
        echo ""
        echo "访问地址:"
        echo "  前端: http://localhost:5173"
        echo "  后端: http://localhost:8080"
        echo "  MySQL: localhost:3306"
        echo ""
        echo "数据库信息:"
        echo "  用户名: root"
        echo "  密码: root123456"
        echo "  数据库: learning_notes"
    else
        echo "❌ 服务启动失败"
        exit 1
    fi
}

# 停止服务
stop_services() {
    echo "停止服务..."
    docker compose down
    echo "✅ 服务已停止"
}

# 查看日志
show_logs() {
    echo "查看日志..."
    docker compose logs -f
}

# 重启服务
restart_services() {
    echo "重启服务..."
    docker compose restart
    echo "✅ 服务已重启"
}

# 清理数据
clean_data() {
    echo "清理数据..."

    read -p "确定要清理所有数据吗？(y/N): " confirm
    if [[ $confirm == [yY] || $confirm == [yY][eE][sS] ]]; then
        docker compose down -v
        rm -rf uploads
        echo "✅ 数据已清理"
    else
        echo "取消清理数据"
    fi
}

# 显示帮助
show_help() {
    echo ""
    echo "用法: $0 [command]"
    echo ""
    echo "命令:"
    echo "  build      构建项目"
    echo "  start      启动服务"
    echo "  stop       停止服务"
    echo "  restart    重启服务"
    echo "  logs       查看日志"
    echo "  clean      清理数据"
    echo "  help       显示帮助"
    echo ""
    echo "示例:"
    echo "  $0 build    # 构建项目"
    echo "  $0 start    # 启动服务"
    echo "  $0 logs     # 查看日志"
}

# 主函数
main() {
    case "$1" in
        build)
            check_environment
            setup_database
            build_backend
            build_frontend
            ;;
        start)
            check_environment
            start_services
            ;;
        stop)
            stop_services
            ;;
        restart)
            restart_services
            ;;
        logs)
            show_logs
            ;;
        clean)
            clean_data
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            echo "未知命令: $1"
            show_help
            exit 1
            ;;
    esac
}

# 如果没有参数，显示帮助
if [ $# -eq 0 ]; then
    show_help
    exit 0
fi

main "$1"
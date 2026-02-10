#!/bin/bash

# 学习笔记管理系统 - 阿里云ECS快速部署脚本
# 使用方法: ./deploy-ecs.sh [install|start|stop|restart|status|logs|clean]

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置
PROJECT_NAME="learning-notes"
BACKEND_PORT=8080
FRONTEND_PORT=80
MYSQL_PORT=3306
MYSQL_PASSWORD="root123456"

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查Docker环境
check_docker() {
    print_info "检查Docker环境..."

    if ! command -v docker &> /dev/null; then
        print_error "未安装Docker，请先安装Docker"
        print_info "Ubuntu/Debian: apt install -y docker.io"
        print_info "CentOS/RHEL: yum install -y docker-ce"
        exit 1
    fi

    if ! command -v docker compose &> /dev/null; then
        print_error "未安装Docker Compose，请先安装Docker Compose"
        print_info "下载地址: https://github.com/docker/compose/releases"
        exit 1
    fi

    print_success "Docker环境检查通过"
}

# 检查端口占用
check_ports() {
    print_info "检查端口占用情况..."

    local ports=($FRONTEND_PORT $BACKEND_PORT $MYSQL_PORT)
    local services=("前端" "后端" "MySQL")

    for i in "${!ports[@]}"; do
        local port=${ports[$i]}
        local service=${services[$i]}

        if netstat -tln 2>/dev/null | grep -q ":$port " || ss -tln 2>/dev/null | grep -q ":$port "; then
            print_warning "端口 $port ($service) 可能被占用"
            print_info "请检查是否有其他服务在使用该端口"
            print_info "可以使用命令查看: netstat -tlnp | grep :$port"
        else
            print_success "端口 $port ($service) 可用"
        fi
    done
}

# 安装依赖
install_dependencies() {
    print_info "安装项目依赖..."

    # 检查是否在项目根目录
    if [ ! -f "docker-compose.yml" ]; then
        print_error "请在项目根目录运行此脚本"
        exit 1
    fi

    # 安装后端依赖
    print_info "安装后端依赖..."
    cd backend
    if [ ! -f "target/learning-notes-1.0.0.jar" ]; then
        if command -v mvn &> /dev/null; then
            mvn clean package -DskipTests
            if [ $? -eq 0 ]; then
                print_success "后端构建成功"
            else
                print_error "后端构建失败"
                exit 1
            fi
        else
            print_warning "Maven未安装，跳过后端构建"
        fi
    else
        print_info "后端JAR已存在，跳过构建"
    fi
    cd ..

    # 安装前端依赖
    print_info "安装前端依赖..."
    cd frontend
    if [ ! -d "node_modules" ]; then
        if command -v npm &> /dev/null; then
            npm install
            if [ $? -eq 0 ]; then
                print_success "前端依赖安装成功"
            else
                print_error "前端依赖安装失败"
                exit 1
            fi
        else
            print_warning "Node.js未安装，跳过前端依赖安装"
        fi
    else
        print_info "前端依赖已存在，跳过安装"
    fi

    # 构建前端
    if [ ! -d "dist" ]; then
        if command -v npm &> /dev/null; then
            npm run build
            if [ $? -eq 0 ]; then
                print_success "前端构建成功"
            else
                print_error "前端构建失败"
                exit 1
            fi
        fi
    else
        print_info "前端构建产物已存在，跳过构建"
    fi
    cd ..
}

# 启动服务
start_services() {
    print_info "启动服务..."

    # 检查Docker环境
    check_docker

    # 检查端口
    check_ports

    # 创建上传目录
    mkdir -p uploads

    # 启动Docker Compose
    print_info "正在启动Docker容器..."
    docker compose up -d

    if [ $? -eq 0 ]; then
        print_success "服务启动成功"
        echo ""
        print_info "访问地址:"
        echo "  前端: http://$(curl -s ifconfig.me || curl -s ipinfo.io/ip || echo 'localhost'):$FRONTEND_PORT"
        echo "  后端: http://$(curl -s ifconfig.me || curl -s ipinfo.io/ip || echo 'localhost'):$BACKEND_PORT/api/notes"
        echo ""
        print_info "数据库信息:"
        echo "  用户名: root"
        echo "  密码: $MYSQL_PASSWORD"
        echo "  数据库: learning_notes"
        echo ""
        print_info "查看日志: ./deploy-ecs.sh logs"
        print_info "停止服务: ./deploy-ecs.sh stop"
    else
        print_error "服务启动失败"
        exit 1
    fi
}

# 停止服务
stop_services() {
    print_info "停止服务..."

    if [ ! -f "docker-compose.yml" ]; then
        print_error "未找到docker compose.yml文件"
        exit 1
    fi

    docker compose down

    if [ $? -eq 0 ]; then
        print_success "服务已停止"
    else
        print_error "停止服务失败"
        exit 1
    fi
}

# 重启服务
restart_services() {
    print_info "重启服务..."

    stop_services
    sleep 2
    start_services
}

# 查看服务状态
show_status() {
    print_info "服务状态..."

    if [ ! -f "docker-compose.yml" ]; then
        print_error "未找到docker compose.yml文件"
        exit 1
    fi

    docker compose ps

    echo ""
    print_info "容器资源使用情况:"
    docker stats --no-stream
}

# 查看日志
show_logs() {
    print_info "查看日志..."

    if [ ! -f "docker-compose.yml" ]; then
        print_error "未找到docker compose.yml文件"
        exit 1
    fi

    docker compose logs -f
}

# 清理数据
clean_data() {
    print_warning "此操作将删除所有数据，包括数据库和上传的文件！"
    read -p "确定要继续吗？(y/N): " confirm

    if [[ $confirm == [yY] || $confirm == [yY][eE][sS] ]]; then
        print_info "清理数据..."

        if [ ! -f "docker-compose.yml" ]; then
            print_error "未找到docker compose.yml文件"
            exit 1
        fi

        docker compose down -v
        rm -rf uploads

        print_success "数据已清理"
    else
        print_info "取消清理数据"
    fi
}

# 检查端口是否可用
check_port_available() {
    local port=$1
    local service=$2

    if netstat -tln 2>/dev/null | grep -q ":$port " || ss -tln 2>/dev/null | grep -q ":$port "; then
        print_warning "端口 $port ($service) 可能被占用"
        return 1
    else
        print_success "端口 $port ($service) 可用"
        return 0
    fi
}

# 显示帮助信息
show_help() {
    echo ""
    echo "学习笔记管理系统 - 阿里云ECS部署脚本"
    echo "========================================"
    echo ""
    echo "用法: $0 [command]"
    echo ""
    echo "命令:"
    echo "  install    安装依赖并构建项目"
    echo "  start      启动服务"
    echo "  stop       停止服务"
    echo "  restart    重启服务"
    echo "  status     查看服务状态"
    echo "  logs       查看日志"
    echo "  clean      清理数据"
    echo "  help       显示帮助"
    echo ""
    echo "示例:"
    echo "  $0 install    # 安装依赖并构建"
    echo "  $0 start      # 启动服务"
    echo "  $0 status     # 查看状态"
    echo "  $0 logs       # 查看日志"
    echo ""
    echo "部署步骤:"
    echo "  1. 上传项目到ECS: scp -r learning-notes root@ECS_IP:/root/"
    echo "  2. 进入目录: cd /root/learning-notes"
    echo "  3. 安装依赖: ./deploy-ecs.sh install"
    echo "  4. 启动服务: ./deploy-ecs.sh start"
    echo "  5. 访问: http://ECS_IP"
    echo ""
}

# 主函数
main() {
    case "$1" in
        install)
            check_docker
            install_dependencies
            ;;
        start)
            start_services
            ;;
        stop)
            stop_services
            ;;
        restart)
            restart_services
            ;;
        status)
            show_status
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
            print_error "未知命令: $1"
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

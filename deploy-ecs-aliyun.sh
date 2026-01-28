#!/bin/bash

# 学习笔记管理系统 - ECS部署脚本（从阿里云ACR拉取镜像）

set -e

# 配置
ALIYUN_REGION="cn-beijing"  # 阿里云地域
ALIYUN_REGISTRY="crpi-zqmddwfip4cwc7n1.cn-beijing.personal.cr.aliyuncs.com"  # 阿里云ACR地址
ALIYUN_ACCOUNT="helloyang"  # 阿里云账号
ALIYUN_PASSWORD="yq379958"  # 阿里云密码
NAMESPACE="learning-notes"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# 检查配置
check_config() {
    print_info "检查配置..."

    if [ "$ALIYUN_ACCOUNT" = "你的阿里云账号" ]; then
        print_error "请修改脚本中的 ALIYUN_ACCOUNT 为你的阿里云账号"
        exit 1
    fi

    if [ "$ALIYUN_PASSWORD" = "你的阿里云密码" ]; then
        print_error "请修改脚本中的 ALIYUN_PASSWORD 为你的阿里云密码"
        exit 1
    fi

    print_success "配置检查通过"
}

# 检查Docker环境
check_docker() {
    print_info "检查Docker环境..."

    if ! command -v docker &> /dev/null; then
        print_error "未安装Docker，请先安装Docker"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null; then
        print_error "未安装Docker Compose，请先安装Docker Compose"
        exit 1
    fi

    print_success "Docker环境检查通过"
}

# 登录阿里云ACR
login_acr() {
    print_info "登录阿里云ACR..."

    docker logout ${ALIYUN_REGISTRY} 2>/dev/null || true

    if docker login ${ALIYUN_REGISTRY} -u ${ALIYUN_ACCOUNT} -p ${ALIYUN_PASSWORD} > /dev/null 2>&1; then
        print_success "登录阿里云ACR成功"
    else
        print_error "登录阿里云ACR失败，请检查账号和密码"
        exit 1
    fi
}

# 拉取镜像
pull_images() {
    print_info "拉取后端镜像..."

    if docker pull ${ALIYUN_REGISTRY}/${NAMESPACE}/backend:latest > /dev/null 2>&1; then
        print_success "后端镜像拉取成功"
    else
        print_error "后端镜像拉取失败"
        exit 1
    fi

    print_info "拉取前端镜像..."

    if docker pull ${ALIYUN_REGISTRY}/${NAMESPACE}/frontend:latest > /dev/null 2>&1; then
        print_success "前端镜像拉取成功"
    else
        print_error "前端镜像拉取失败"
        exit 1
    fi
}

# 创建上传目录
create_upload_dir() {
    print_info "创建上传目录..."

    mkdir -p uploads

    print_success "上传目录创建成功"
}

# 启动服务
start_services() {
    print_info "启动服务..."

    if docker-compose up -d > /dev/null 2>&1; then
        print_success "服务启动成功"
    else
        print_error "服务启动失败"
        exit 1
    fi
}

# 显示服务状态
show_status() {
    print_info "服务状态:"
    docker-compose ps

    echo ""
    print_info "访问地址:"
    echo "  前端: http://$(curl -s ifconfig.me || curl -s ipinfo.io/ip || echo 'localhost')"
    echo "  后端: http://$(curl -s ifconfig.me || curl -s ipinfo.io/ip || echo 'localhost')/api/notes"
}

# 主函数
main() {
    echo "========================================"
    echo "学习笔记管理系统 - ECS部署"
    echo "========================================"
    echo ""

    check_config
    check_docker
    login_acr
    pull_images
    create_upload_dir
    start_services
    show_status

    echo ""
    print_success "部署完成！"
}

main "$@"

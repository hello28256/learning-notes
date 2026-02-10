#!/bin/bash

# 学习笔记管理系统 - Mac本地构建和推送镜像到阿里云ACR

set -e

# 配置
ALIYUN_REGION="cn-beijing"  # 阿里云地域
ALIYUN_REGISTRY="crpi-zqmddwfip4cwc7n1.cn-beijing.personal.cr.aliyuncs.com"  # 阿里云ACR地址
ALIYUN_ACCOUNT="helloyang"  # 阿里云账号
ALIYUN_PASSWORD="Yq379958"  # 阿里云密码
NAMESPACE="learning-notes"
VERSION="v1.0.0"

# 多架构支持 (ARM64 + AMD64)
PLATFORMS="linux/amd64,linux/arm64"

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
        print_error "未安装Docker，请先安装Docker Desktop for Mac"
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

# 构建后端镜像
build_backend() {
    print_info "构建后端镜像 (多架构: ${PLATFORMS})..."

    cd backend

    # 使用 Docker Buildx 构建多架构镜像
    if docker buildx build \
        --platform ${PLATFORMS} \
        -t ${ALIYUN_REGISTRY}/${NAMESPACE}/backend:${VERSION} \
        -t ${ALIYUN_REGISTRY}/${NAMESPACE}/backend:latest \
        --push \
        . > /dev/null 2>&1; then
        print_success "后端镜像构建并推送成功 (v${VERSION} 和 latest)"
    else
        print_error "后端镜像构建失败"
        exit 1
    fi

    cd ..
}

# 构建前端镜像
build_frontend() {
    print_info "构建前端镜像 (多架构: ${PLATFORMS})..."

    cd frontend

    # 使用 Docker Buildx 构建多架构镜像
    if docker buildx build \
        --platform ${PLATFORMS} \
        -t ${ALIYUN_REGISTRY}/${NAMESPACE}/frontend:${VERSION} \
        -t ${ALIYUN_REGISTRY}/${NAMESPACE}/frontend:latest \
        --push \
        . > /dev/null 2>&1; then
        print_success "前端镜像构建并推送成功 (v${VERSION} 和 latest)"
    else
        print_error "前端镜像构建失败"
        exit 1
    fi

    cd ..
}

# 检查镜像是否已推送
check_images() {
    print_info "检查镜像是否已推送..."

    # 检查后端镜像
    if docker pull ${ALIYUN_REGISTRY}/${NAMESPACE}/backend:latest > /dev/null 2>&1; then
        print_success "后端镜像已推送 (latest)"
    else
        print_error "后端镜像未找到，请检查构建是否成功"
        exit 1
    fi

    # 检查前端镜像
    if docker pull ${ALIYUN_REGISTRY}/${NAMESPACE}/frontend:latest > /dev/null 2>&1; then
        print_success "前端镜像已推送 (latest)"
    else
        print_error "前端镜像未找到，请检查构建是否成功"
        exit 1
    fi
}

# 显示镜像信息
show_info() {
    print_info "镜像信息 (已推送):"
    echo "  后端: ${ALIYUN_REGISTRY}/${NAMESPACE}/backend:${VERSION}"
    echo "  前端: ${ALIYUN_REGISTRY}/${NAMESPACE}/frontend:${VERSION}"
    echo ""
    print_info "镜像支持架构:"
    echo "  linux/amd64 (阿里云ECS)"
    echo "  linux/arm64 (Mac M1/M2)"
    echo ""
    print_info "ECS部署命令:"
    echo "  docker login ${ALIYUN_REGISTRY} -u ${ALIYUN_ACCOUNT} -p ${ALIYUN_PASSWORD}"
    echo "  docker pull ${ALIYUN_REGISTRY}/${NAMESPACE}/backend:latest"
    echo "  docker pull ${ALIYUN_REGISTRY}/${NAMESPACE}/frontend:latest"
    echo "  docker compose up -d"
}

# 主函数
main() {
    echo "========================================"
    echo "学习笔记管理系统 - Mac本地构建和推送"
    echo "========================================"
    echo ""

    check_config
    check_docker
    login_acr
    build_backend
    build_frontend
    check_images
    show_info

    echo ""
    print_success "部署完成！"
}

main "$@"

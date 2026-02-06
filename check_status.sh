#!/bin/bash

echo "========================================"
echo "   学习平台系统状态检查"
echo "========================================"
echo

# 检查前端
echo "[1/3] 检查前端服务 (端口3000)..."
if curl -s -m 2 http://localhost:3000 > /dev/null 2>&1; then
    echo "✅ 前端服务运行正常"
else
    echo "❌ 前端服务未运行或无法访问"
fi

# 检查后端
echo "[2/3] 检查后端服务 (端口8084)..."
if curl -s -m 2 http://localhost:8084/actuator/health > /dev/null 2>&1; then
    echo "✅ 后端服务运行正常"
else
    echo "❌ 后端服务未运行或无法访问"
fi

# 检查MongoDB
echo "[3/3] 检查MongoDB服务..."
if curl -s -m 2 http://localhost:27017 > /dev/null 2>&1; then
    echo "✅ MongoDB服务运行正常"
else
    echo "⚠️  MongoDB服务未运行 (如果使用本地MongoDB，请先启动)"
fi

echo
echo "========================================"
echo "   前端访问地址: http://localhost:3000"
echo "   后端API地址: http://localhost:8084"
echo "========================================"
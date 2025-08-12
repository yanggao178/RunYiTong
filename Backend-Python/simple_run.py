#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
简化的启动脚本，用于在没有完整Python环境时运行
"""

import sys
import os

# 添加当前目录到Python路径
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

try:
    import uvicorn
    from main import app
    
    print("=" * 50)
    print("AI Medical Backend Server")
    print("=" * 50)
    print("启动服务器...")
    print("服务地址: http://localhost:8000")
    print("API文档: http://localhost:8000/docs")
    print("按 Ctrl+C 停止服务器")
    print("=" * 50)
    
    # 启动服务器
    uvicorn.run(
        app,
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info"
    )
    
except ImportError as e:
    print(f"缺少依赖包: {e}")
    print("请先安装依赖包:")
    print("pip install fastapi uvicorn sqlalchemy python-multipart python-jose[cryptography] passlib[bcrypt] python-dotenv")
    sys.exit(1)
    
except Exception as e:
    print(f"启动失败: {e}")
    sys.exit(1)
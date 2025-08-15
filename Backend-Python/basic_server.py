#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
基础HTTP服务器 - 仅使用Python标准库
用于在无法安装第三方依赖时提供基本API服务
"""

import http.server
import socketserver
import json
import urllib.parse
from datetime import datetime

# 模拟数据
products_data = [
    {
        "id": 1,
        "name": "养生茶",
        "price": 98.0,
        "description": "纯天然草本配方，滋阴补肾",
        "image_url": "https://example.com/tea.jpg",
        "category": "保健品",
        "stock": 100
    },
    {
        "id": 2,
        "name": "艾灸贴",
        "price": 68.0,
        "description": "缓解疲劳，促进血液循环",
        "image_url": "https://example.com/moxibustion.jpg",
        "category": "保健品",
        "stock": 200
    },
    {
        "id": 3,
        "name": "按摩仪",
        "price": 199.0,
        "description": "智能按摩，舒缓肌肉紧张",
        "image_url": "https://example.com/massager.jpg",
        "category": "保健品",
        "stock": 50
    }
]

books_data = [
    {
        "id": 1,
        "name": "黄帝内经",
        "author": "佚名",
        "category": "中医基础",
        "description": "中国最早的医学典籍，传统医学四大经典著作之一。",
        "cover_url": "https://example.com/huangdi.jpg",
        "publish_date": "2023-01-01T00:00:00.000000"
    },
    {
        "id": 2,
        "name": "本草纲目",
        "author": "李时珍",
        "category": "中药学",
        "description": "集我国16世纪以前药学成就之大成。",
        "cover_url": "https://example.com/bencao.jpg",
        "publish_date": "2023-01-02T00:00:00.000000"
    },
    {
        "id": 3,
        "name": "希波克拉底文集",
        "author": "希波克拉底",
        "category": "医学理论",
        "description": "西方医学的奠基之作。",
        "cover_url": "https://example.com/hippocrates.jpg",
        "publish_date": "2023-01-03T00:00:00.000000"
    }
]

class APIHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        # 解析URL
        parsed_path = urllib.parse.urlparse(self.path)
        path = parsed_path.path
        query_params = urllib.parse.parse_qs(parsed_path.query)
        
        # 设置CORS头
        self.send_response(200)
        self.send_header('Content-type', 'application/json; charset=utf-8')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization')
        self.end_headers()
        
        response_data = {"success": True, "message": "操作成功", "data": None}
        
        try:
            if path == '/':
                response_data["data"] = {
                    "message": "AI Medical Backend API - 基础版",
                    "version": "1.0.0",
                    "status": "running",
                    "time": datetime.now().isoformat()
                }
            
            elif path == '/health':
                response_data["message"] = "服务运行正常"
            
            elif path == '/api/products/':
                # 商品列表
                search = query_params.get('search', [None])[0]
                category = query_params.get('category', [None])[0]
                skip = int(query_params.get('skip', [0])[0])
                limit = int(query_params.get('limit', [10])[0])
                
                filtered_products = products_data
                
                if search:
                    filtered_products = [p for p in filtered_products if search.lower() in p["name"].lower()]
                
                if category:
                    filtered_products = [p for p in filtered_products if p["category"] == category]
                
                total = len(filtered_products)
                items = filtered_products[skip:skip + limit]
                
                response_data["data"] = {
                    "items": items,
                    "total": total,
                    "skip": skip,
                    "limit": limit
                }
            
            elif path.startswith('/api/products/'):
                # 单个商品
                product_id = int(path.split('/')[-1])
                product = next((p for p in products_data if p["id"] == product_id), None)
                if product:
                    response_data["data"] = product
                else:
                    response_data["success"] = False
                    response_data["message"] = "商品不存在"
            
            elif path == '/api/books/':
                # 图书列表
                search = query_params.get('search', [None])[0]
                category = query_params.get('category', [None])[0]
                skip = int(query_params.get('skip', [0])[0])
                limit = int(query_params.get('limit', [10])[0])
                
                filtered_books = books_data
                
                if search:
                    filtered_books = [b for b in filtered_books if search.lower() in b["name"].lower() or search.lower() in b["author"].lower()]
                
                if category:
                    filtered_books = [b for b in filtered_books if category in b["category"]]
                
                total = len(filtered_books)
                items = filtered_books[skip:skip + limit]
                
                response_data["data"] = {
                    "items": items,
                    "total": total,
                    "skip": skip,
                    "limit": limit
                }
            
            elif path == '/api/v1/books/chinese-medicine':
                # 中医图书
                chinese_books = [b for b in books_data if "中医" in b["category"] or "中药" in b["category"]]
                response_data["data"] = chinese_books
            
            elif path == '/api/v1/books/western-medicine':
                # 西医图书
                western_books = [b for b in books_data if "医学理论" in b["category"] or b["author"] in ["希波克拉底", "维萨里", "奥斯勒"]]
                response_data["data"] = western_books
            
            elif path.startswith('/api/books/'):
                # 单个图书
                book_id = int(path.split('/')[-1])
                book = next((b for b in books_data if b["id"] == book_id), None)
                if book:
                    response_data["data"] = book
                else:
                    response_data["success"] = False
                    response_data["message"] = "图书不存在"
            
            elif path == '/api/appointments/departments':
                # 科室列表
                departments = [
                    {"id": 1, "name": "内科", "description": "内科疾病诊治"},
                    {"id": 2, "name": "外科", "description": "外科手术治疗"},
                    {"id": 3, "name": "中医科", "description": "中医诊疗"},
                    {"id": 4, "name": "儿科", "description": "儿童疾病诊治"}
                ]
                response_data["data"] = {"items": departments}
            
            else:
                response_data["success"] = False
                response_data["message"] = "接口不存在"
        
        except Exception as e:
            response_data["success"] = False
            response_data["message"] = f"服务器错误: {str(e)}"
        
        # 发送响应
        response_json = json.dumps(response_data, ensure_ascii=False, indent=2)
        self.wfile.write(response_json.encode('utf-8'))
    
    def do_POST(self):
        # 设置CORS头
        self.send_response(200)
        self.send_header('Content-type', 'application/json; charset=utf-8')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization')
        self.end_headers()
        
        response_data = {"success": True, "message": "操作成功", "data": None}
        
        try:
            # 读取请求体
            content_length = int(self.headers.get('Content-Length', 0))
            post_data = self.rfile.read(content_length)
            
            if self.path == '/api/users/login':
                # 模拟登录
                try:
                    data = json.loads(post_data.decode('utf-8'))
                    username = data.get('username', '')
                    password = data.get('password', '')
                    
                    if username in ['admin', 'testuser'] and password in ['admin123', 'test123']:
                        response_data["data"] = {
                            "token": f"mock_token_{username}",
                            "user": {
                                "id": 1 if username == "admin" else 2,
                                "username": username,
                                "full_name": "系统管理员" if username == "admin" else "测试用户"
                            }
                        }
                    else:
                        response_data["success"] = False
                        response_data["message"] = "用户名或密码错误"
                except:
                    response_data["success"] = False
                    response_data["message"] = "请求格式错误"
            
            elif self.path == '/api/prescriptions/analyze-symptoms':
                # AI症状分析
                try:
                    data = json.loads(post_data.decode('utf-8'))
                    symptoms = data.get('symptoms', '')
                    
                    analysis = {
                        "symptoms": symptoms,
                        "possible_conditions": ["感冒", "咽喉炎"],
                        "recommendations": ["多喝水", "注意休息", "如症状持续请就医"],
                        "confidence": 0.75
                    }
                    response_data["data"] = analysis
                except:
                    response_data["success"] = False
                    response_data["message"] = "请求格式错误"
            
            else:
                response_data["success"] = False
                response_data["message"] = "接口不存在"
        
        except Exception as e:
            response_data["success"] = False
            response_data["message"] = f"服务器错误: {str(e)}"
        
        # 发送响应
        response_json = json.dumps(response_data, ensure_ascii=False, indent=2)
        self.wfile.write(response_json.encode('utf-8'))
    
    def do_OPTIONS(self):
        # 处理预检请求
        self.send_response(200)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization')
        self.end_headers()

def main():
    PORT = 8000
    
    print("=" * 60)
    print("           AI Medical Backend - 基础版")
    print("=" * 60)
    print(f"启动HTTP服务器...")
    print(f"服务地址: http://localhost:{PORT}")
    print(f"测试接口: http://localhost:{PORT}/api/products/")
    print(f"按 Ctrl+C 停止服务器")
    print("=" * 60)
    print()
    
    try:
        with socketserver.TCPServer(("", PORT), APIHandler) as httpd:
            print(f"服务器已启动，监听端口 {PORT}")
            httpd.serve_forever()
    except KeyboardInterrupt:
        print("\n服务器已停止")
    except Exception as e:
        print(f"启动失败: {e}")
        print("可能是端口被占用，请尝试关闭占用8000端口的程序")

if __name__ == "__main__":
    main()
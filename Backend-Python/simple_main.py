#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
简化版AI Medical Backend
最小依赖版本，用于快速启动和测试
"""

try:
    from fastapi import FastAPI, HTTPException
    from fastapi.middleware.cors import CORSMiddleware
    from pydantic import BaseModel
    from typing import List, Optional
    import uvicorn
    import json
    import os
    from datetime import datetime
except ImportError as e:
    print(f"缺少依赖包: {e}")
    print("请安装基本依赖: pip install fastapi uvicorn")
    exit(1)

# 创建FastAPI应用
app = FastAPI(
    title="AI Medical Backend",
    description="智能医疗后端服务 - 简化版",
    version="1.0.0"
)

# 配置CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 基础响应模型
class BaseResponse(BaseModel):
    success: bool = True
    message: str = "操作成功"
    data: Optional[dict] = None

# 商品模型
class Product(BaseModel):
    id: int
    name: str
    price: float
    description: str
    image_url: str
    category: str
    stock: int

# 图书模型
class Book(BaseModel):
    id: int
    name: str
    author: str
    category: str
    description: str
    cover_url: str

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
        "cover_url": "https://example.com/huangdi.jpg"
    },
    {
        "id": 2,
        "name": "本草纲目",
        "author": "李时珍",
        "category": "中药学",
        "description": "集我国16世纪以前药学成就之大成。",
        "cover_url": "https://example.com/bencao.jpg"
    },
    {
        "id": 3,
        "name": "希波克拉底文集",
        "author": "希波克拉底",
        "category": "医学理论",
        "description": "西方医学的奠基之作。",
        "cover_url": "https://example.com/hippocrates.jpg"
    }
]

# 根路径
@app.get("/")
async def root():
    return {
        "message": "AI Medical Backend API",
        "version": "1.0.0",
        "docs": "/docs",
        "status": "running"
    }

# 健康检查
@app.get("/health")
async def health_check():
    return BaseResponse(message="服务运行正常")

# 商品相关API
@app.get("/api/products/", response_model=BaseResponse)
async def get_products(skip: int = 0, limit: int = 10, search: str = None, category: str = None):
    """获取商品列表"""
    filtered_products = products_data
    
    # 搜索过滤
    if search:
        filtered_products = [p for p in filtered_products if search.lower() in p["name"].lower()]
    
    # 分类过滤
    if category:
        filtered_products = [p for p in filtered_products if p["category"] == category]
    
    # 分页
    total = len(filtered_products)
    items = filtered_products[skip:skip + limit]
    
    return BaseResponse(
        data={
            "items": items,
            "total": total,
            "skip": skip,
            "limit": limit
        }
    )

@app.get("/api/products/{product_id}", response_model=BaseResponse)
async def get_product(product_id: int):
    """获取单个商品详情"""
    product = next((p for p in products_data if p["id"] == product_id), None)
    if not product:
        raise HTTPException(status_code=404, detail="商品不存在")
    return BaseResponse(data=product)

# 图书相关API
@app.get("/api/books/", response_model=BaseResponse)
async def get_books(skip: int = 0, limit: int = 10, search: str = None, category: str = None):
    """获取图书列表"""
    filtered_books = books_data
    
    # 搜索过滤
    if search:
        filtered_books = [b for b in filtered_books if search.lower() in b["name"].lower() or search.lower() in b["author"].lower()]
    
    # 分类过滤
    if category:
        filtered_books = [b for b in filtered_books if category in b["category"]]
    
    # 分页
    total = len(filtered_books)
    items = filtered_books[skip:skip + limit]
    
    return BaseResponse(
        data={
            "items": items,
            "total": total,
            "skip": skip,
            "limit": limit
        }
    )

@app.get("/api/books/chinese", response_model=BaseResponse)
async def get_chinese_books():
    """获取中医图书"""
    chinese_books = [b for b in books_data if "中医" in b["category"] or "中药" in b["category"]]
    return BaseResponse(data={"items": chinese_books})

@app.get("/api/books/western", response_model=BaseResponse)
async def get_western_books():
    """获取西医图书"""
    western_books = [b for b in books_data if "医学理论" in b["category"] or b["author"] in ["希波克拉底", "维萨里", "奥斯勒"]]
    return BaseResponse(data={"items": western_books})

@app.get("/api/books/{book_id}", response_model=BaseResponse)
async def get_book(book_id: int):
    """获取单个图书详情"""
    book = next((b for b in books_data if b["id"] == book_id), None)
    if not book:
        raise HTTPException(status_code=404, detail="图书不存在")
    return BaseResponse(data=book)

# 用户相关API（简化版）
@app.post("/api/users/login", response_model=BaseResponse)
async def login(username: str, password: str):
    """用户登录（模拟）"""
    # 简单的模拟登录
    if username in ["admin", "testuser"] and password in ["admin123", "test123"]:
        return BaseResponse(
            data={
                "token": f"mock_token_{username}",
                "user": {
                    "id": 1 if username == "admin" else 2,
                    "username": username,
                    "full_name": "系统管理员" if username == "admin" else "测试用户"
                }
            }
        )
    else:
        raise HTTPException(status_code=401, detail="用户名或密码错误")

# 处方相关API（简化版）
@app.post("/api/prescriptions/analyze-symptoms", response_model=BaseResponse)
async def analyze_symptoms(symptoms: str):
    """AI症状分析（模拟）"""
    # 简单的模拟分析
    analysis = {
        "symptoms": symptoms,
        "possible_conditions": ["感冒", "咽喉炎"],
        "recommendations": ["多喝水", "注意休息", "如症状持续请就医"],
        "confidence": 0.75
    }
    return BaseResponse(data=analysis)

# 预约相关API（简化版）
@app.get("/api/appointments/departments", response_model=BaseResponse)
async def get_departments():
    """获取科室列表"""
    departments = [
        {"id": 1, "name": "内科", "description": "内科疾病诊治"},
        {"id": 2, "name": "外科", "description": "外科手术治疗"},
        {"id": 3, "name": "中医科", "description": "中医诊疗"},
        {"id": 4, "name": "儿科", "description": "儿童疾病诊治"}
    ]
    return BaseResponse(data={"items": departments})

@app.get("/api/appointments/hospitals", response_model=BaseResponse)
async def get_hospitals():
    """获取医院列表"""
    hospitals = [
        {
            "id": 1,
            "name": "北京协和医院",
            "address": "北京市东城区东单帅府园1号",
            "phone": "010-69156114",
            "level": "三甲",
            "description": "综合性三级甲等医院",
            "departments": [1, 2, 3, 4]
        },
        {
            "id": 2,
            "name": "北京大学第一医院",
            "address": "北京市西城区西什库大街8号",
            "phone": "010-83572211",
            "level": "三甲",
            "description": "综合性三级甲等医院",
            "departments": [1, 2, 4]
        },
        {
            "id": 3,
            "name": "中国中医科学院广安门医院",
            "address": "北京市西城区北线阁5号",
            "phone": "010-88001122",
            "level": "三甲",
            "description": "中医特色三级甲等医院",
            "departments": [1, 3, 4]
        },
        {
            "id": 4,
            "name": "首都医科大学附属北京儿童医院",
            "address": "北京市西城区南礼士路56号",
            "phone": "010-59616161",
            "level": "三甲",
            "description": "儿科专科三级甲等医院",
            "departments": [4]
        }
    ]
    return BaseResponse(data={"items": hospitals})

@app.get("/api/appointments/doctors", response_model=BaseResponse)
async def get_doctors(department_id: Optional[int] = None, hospital_id: Optional[int] = None):
    """获取医生列表"""
    doctors = [
        {
            "id": 1,
            "name": "张明华",
            "title": "主任医师",
            "department_id": 1,
            "department_name": "内科",
            "hospital_id": 1,
            "hospital_name": "北京协和医院",
            "specialties": ["心血管疾病", "高血压", "糖尿病"],
            "experience_years": 25,
            "education": "北京协和医学院博士",
            "introduction": "从事内科临床工作25年，擅长心血管疾病诊治",
            "available_times": ["周一上午", "周三下午", "周五上午"]
        },
        {
            "id": 2,
            "name": "李建国",
            "title": "副主任医师",
            "department_id": 2,
            "department_name": "外科",
            "hospital_id": 1,
            "hospital_name": "北京协和医院",
            "specialties": ["普通外科", "腹腔镜手术", "胆囊疾病"],
            "experience_years": 18,
            "education": "北京大学医学部硕士",
            "introduction": "专注普通外科及微创手术，手术经验丰富",
            "available_times": ["周二上午", "周四上午", "周六上午"]
        },
        {
            "id": 3,
            "name": "王中医",
            "title": "主任医师",
            "department_id": 3,
            "department_name": "中医科",
            "hospital_id": 3,
            "hospital_name": "中国中医科学院广安门医院",
            "specialties": ["中医内科", "针灸", "中药调理"],
            "experience_years": 30,
            "education": "北京中医药大学博士",
            "introduction": "中医世家传承，擅长中医诊疗和针灸治疗",
            "available_times": ["周一下午", "周三上午", "周五下午"]
        },
        {
            "id": 4,
            "name": "陈小儿",
            "title": "主任医师",
            "department_id": 4,
            "department_name": "儿科",
            "hospital_id": 4,
            "hospital_name": "首都医科大学附属北京儿童医院",
            "specialties": ["儿童呼吸系统疾病", "儿童感染性疾病", "新生儿疾病"],
            "experience_years": 22,
            "education": "首都医科大学博士",
            "introduction": "专业从事儿科临床工作，对儿童常见病有丰富经验",
            "available_times": ["周一上午", "周二下午", "周四上午"]
        },
        {
            "id": 5,
            "name": "赵心脏",
            "title": "主任医师",
            "department_id": 1,
            "department_name": "内科",
            "hospital_id": 2,
            "hospital_name": "北京大学第一医院",
            "specialties": ["心脏病", "心律失常", "心力衰竭"],
            "experience_years": 28,
            "education": "北京大学医学部博士",
            "introduction": "心血管疾病专家，在心脏病诊治方面有深厚造诣",
            "available_times": ["周二上午", "周四下午", "周六上午"]
        }
    ]
    
    # 根据科室ID筛选
    if department_id:
        doctors = [d for d in doctors if d["department_id"] == department_id]
    
    # 根据医院ID筛选
    if hospital_id:
        doctors = [d for d in doctors if d["hospital_id"] == hospital_id]
    
    return BaseResponse(data={"items": doctors})

@app.get("/api/appointments/hospitals/{hospital_id}/departments", response_model=BaseResponse)
async def get_hospital_departments(hospital_id: int):
    """获取指定医院的科室列表"""
    # 医院科室映射
    hospital_departments = {
        1: [1, 2, 3, 4],  # 北京协和医院
        2: [1, 2, 4],     # 北京大学第一医院
        3: [1, 3, 4],     # 广安门医院
        4: [4]            # 儿童医院
    }
    
    all_departments = [
        {"id": 1, "name": "内科", "description": "内科疾病诊治"},
        {"id": 2, "name": "外科", "description": "外科手术治疗"},
        {"id": 3, "name": "中医科", "description": "中医诊疗"},
        {"id": 4, "name": "儿科", "description": "儿童疾病诊治"}
    ]
    
    if hospital_id not in hospital_departments:
        raise HTTPException(status_code=404, detail="医院不存在")
    
    available_dept_ids = hospital_departments[hospital_id]
    available_departments = [d for d in all_departments if d["id"] in available_dept_ids]
    
    return BaseResponse(data={"items": available_departments})

if __name__ == "__main__":
    print("=" * 60)
    print("           AI Medical Backend - 简化版")
    print("=" * 60)
    print("启动服务器...")
    print("服务地址: http://localhost:8000")
    print("API文档: http://localhost:8000/docs")
    print("按 Ctrl+C 停止服务器")
    print("=" * 60)
    
    uvicorn.run(
        app,
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info"
    )
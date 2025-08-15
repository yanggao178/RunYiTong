#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
养生Fragment搜索功能测试脚本
测试后端API的书籍搜索功能
"""

import requests
import json
from typing import Dict, List

# API基础URL
BASE_URL = "http://localhost:8000"

def test_api_connection():
    """测试API连接"""
    try:
        response = requests.get(f"{BASE_URL}/docs")
        if response.status_code == 200:
            print("✅ API服务器连接正常")
            return True
        else:
            print(f"❌ API服务器连接失败: {response.status_code}")
            return False
    except requests.exceptions.ConnectionError:
        print("❌ 无法连接到API服务器，请确保服务器正在运行")
        return False

def test_chinese_medicine_books():
    """测试中医古籍API"""
    print("\n🔍 测试中医古籍API...")
    try:
        response = requests.get(f"{BASE_URL}/api/v1/books/chinese-medicine")
        if response.status_code == 200:
            data = response.json()
            if data.get("success"):
                books = data.get("data", [])
                print(f"✅ 获取到 {len(books)} 本中医古籍")
                for i, book in enumerate(books[:3]):  # 显示前3本
                    print(f"   {i+1}. 《{book['name']}》 - {book['author']}")
                return books
            else:
                print(f"❌ API返回错误: {data.get('message')}")
                return []
        else:
            print(f"❌ HTTP错误: {response.status_code}")
            return []
    except Exception as e:
        print(f"❌ 请求失败: {e}")
        return []

def test_western_medicine_books():
    """测试西医经典API"""
    print("\n🔍 测试西医经典API...")
    try:
        response = requests.get(f"{BASE_URL}/api/v1/books/western-medicine")
        if response.status_code == 200:
            data = response.json()
            if data.get("success"):
                books = data.get("data", [])
                print(f"✅ 获取到 {len(books)} 本西医经典")
                for i, book in enumerate(books[:3]):  # 显示前3本
                    print(f"   {i+1}. 《{book['name']}》 - {book['author']}")
                return books
            else:
                print(f"❌ API返回错误: {data.get('message')}")
                return []
        else:
            print(f"❌ HTTP错误: {response.status_code}")
            return []
    except Exception as e:
        print(f"❌ 请求失败: {e}")
        return []

def test_search_functionality(chinese_books: List[Dict], western_books: List[Dict]):
    """测试搜索功能"""
    print("\n🔍 测试搜索功能...")
    
    # 测试中医古籍搜索
    print("\n📚 中医古籍搜索测试:")
    test_queries = ["黄帝", "张仲景", "本草", "针灸", "内经"]
    
    for query in test_queries:
        filtered_books = [
            book for book in chinese_books
            if (query.lower() in book['name'].lower() or 
                query.lower() in book['author'].lower() or
                (book.get('description') and query.lower() in book['description'].lower()))
        ]
        print(f"  搜索 '{query}': 找到 {len(filtered_books)} 本")
        for book in filtered_books:
            print(f"    - 《{book['name']}》")
    
    # 测试西医经典搜索
    print("\n📚 西医经典搜索测试:")
    test_queries = ["希波克拉底", "解剖", "内科", "病理", "结合"]
    
    for query in test_queries:
        filtered_books = [
            book for book in western_books
            if (query.lower() in book['name'].lower() or 
                query.lower() in book['author'].lower() or
                (book.get('description') and query.lower() in book['description'].lower()))
        ]
        print(f"  搜索 '{query}': 找到 {len(filtered_books)} 本")
        for book in filtered_books:
            print(f"    - 《{book['name']}》")

def test_book_details(chinese_books: List[Dict], western_books: List[Dict]):
    """测试书籍详情"""
    print("\n📖 测试书籍详情...")
    
    # 测试中医古籍详情
    if chinese_books:
        book = chinese_books[0]
        print(f"中医古籍示例: 《{book['name']}》")
        print(f"  作者: {book['author']}")
        print(f"  分类: {book['category']}")
        print(f"  描述: {book.get('description', '暂无描述')}")
    
    # 测试西医经典详情
    if western_books:
        book = western_books[0]
        print(f"\n西医经典示例: 《{book['name']}》")
        print(f"  作者: {book['author']}")
        print(f"  分类: {book['category']}")
        print(f"  描述: {book.get('description', '暂无描述')}")

def main():
    """主测试函数"""
    print("=" * 60)
    print("🏥 养生Fragment搜索功能测试")
    print("=" * 60)
    
    # 1. 测试API连接
    if not test_api_connection():
        print("\n❌ 无法连接到API服务器，请先启动后端服务:")
        print("   cd Backend-Python")
        print("   python run.py")
        return
    
    # 2. 测试中医古籍API
    chinese_books = test_chinese_medicine_books()
    
    # 3. 测试西医经典API
    western_books = test_western_medicine_books()
    
    # 4. 测试搜索功能
    if chinese_books or western_books:
        test_search_functionality(chinese_books, western_books)
        test_book_details(chinese_books, western_books)
    
    # 5. 总结
    print("\n" + "=" * 60)
    print("📊 测试总结:")
    print(f"  中医古籍: {len(chinese_books)} 本")
    print(f"  西医经典: {len(western_books)} 本")
    print(f"  总计: {len(chinese_books) + len(western_books)} 本")
    
    if chinese_books and western_books:
        print("\n✅ 搜索功能测试完成！")
        print("   现在可以在Android应用中测试搜索功能了")
    else:
        print("\n⚠️  部分数据缺失，请检查数据库初始化")
    
    print("=" * 60)

if __name__ == "__main__":
    main()

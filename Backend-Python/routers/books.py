from fastapi import APIRouter, Depends, HTTPException, Query
from fastapi.responses import FileResponse
from sqlalchemy.orm import Session
from typing import List, Optional
import os
from database import get_db
from models import Book as BookModel, BookPage as BookPageModel
from schemas import BookSchema, BookCreate, BookUpdate, BookPage as BookPageSchema, PaginatedResponse
from datetime import datetime

router = APIRouter()

# 测试端点
@router.get("/test")
def test_endpoint():
    """测试端点"""
    return {"message": "books router is working"}

# 获取中医古籍列表
@router.get("/chinese-medicine")
def get_chinese_medicine_books(db: Session = Depends(get_db)):
    """获取中医古籍列表"""
    try:
        books = db.query(BookModel).filter(
            BookModel.category.in_(["中医基础", "中医临床", "中药学", "针灸学"])
        ).all()
        
        # 使用Pydantic模型转换，确保datetime序列化
        books_data = []
        for book in books:
            book_schema = BookSchema.model_validate(book)
            book_dict = book_schema.model_dump()
            # 确保datetime字段被正确序列化
            if 'publish_date' in book_dict and book_dict['publish_date']:
                book_dict['publish_date'] = book_dict['publish_date'].isoformat() if hasattr(book_dict['publish_date'], 'isoformat') else str(book_dict['publish_date'])
            if 'created_time' in book_dict and book_dict['created_time']:
                book_dict['created_time'] = book_dict['created_time'].isoformat() if hasattr(book_dict['created_time'], 'isoformat') else str(book_dict['created_time'])
            if 'updated_time' in book_dict and book_dict['updated_time']:
                book_dict['updated_time'] = book_dict['updated_time'].isoformat() if hasattr(book_dict['updated_time'], 'isoformat') else str(book_dict['updated_time'])
            books_data.append(book_dict)
        
        return {
            "success": True,
            "message": "获取中医古籍成功",
            "data": books_data
        }
    except Exception as e:
        import traceback
        traceback.print_exc()
        return {
            "success": False,
            "message": f"获取中医古籍失败: {str(e)}",
            "data": []
        }

# 获取图书列表（支持分页和搜索）
@router.get("/", response_model=PaginatedResponse)
async def get_books(
    page: int = Query(1, ge=1, description="页码"),
    size: int = Query(10, ge=1, le=100, description="每页数量"),
    search: Optional[str] = Query(None, description="搜索关键词"),
    category: Optional[str] = Query(None, description="图书分类"),
    db: Session = Depends(get_db)
):
    """获取图书列表"""
    query = db.query(BookModel)
    
    # 搜索过滤（书名或作者）
    if search:
        query = query.filter(
            (BookModel.name.contains(search)) | 
            (BookModel.author.contains(search))
        )
    
    # 分类过滤
    if category:
        query = query.filter(BookModel.category == category)
    
    # 计算总数
    total = query.count()
    
    # 分页
    offset = (page - 1) * size
    books = query.offset(offset).limit(size).all()
    
    # 计算总页数
    pages = (total + size - 1) // size
    
    return PaginatedResponse(
        items=[BookSchema.model_validate(book).model_dump() for book in books],
        total=total,
        page=page,
        size=size,
        pages=pages
    )

# 获取西医经典列表
@router.get("/western-medicine")
def get_western_medicine_books(db: Session = Depends(get_db)):
    """获取西医经典列表"""
    try:
        books = db.query(BookModel).filter(
            BookModel.category.in_(["医学理论", "解剖学", "内科学", "病理学", "中西医结合"])
        ).all()
        
        # 手动转换为字典格式
        books_data = []
        for book in books:
            books_data.append({
                "id": book.id,
                "name": book.name,
                "author": book.author,
                "category": book.category,
                "description": book.description,
                "cover_url": book.cover_url,
                "publish_date": book.publish_date.isoformat() if book.publish_date else None,
                "created_time": book.created_time.isoformat() if book.created_time else None,
                "updated_time": book.updated_time.isoformat() if book.updated_time else None
            })
        
        return {
            "success": True,
            "message": "获取西医经典成功",
            "data": books_data
        }
    except Exception as e:
        import traceback
        traceback.print_exc()
        return {
            "success": False,
            "message": f"获取西医经典失败: {str(e)}",
            "data": []
        }

# 获取图书分类列表
@router.get("/categories/list")
async def get_categories(db: Session = Depends(get_db)):
    """获取所有图书分类"""
    categories = db.query(BookModel.category).distinct().all()
    return {"categories": [cat[0] for cat in categories if cat[0]]}

# 批量创建示例图书数据
@router.post("/init-sample-data")
async def init_sample_books(db: Session = Depends(get_db)):
    """初始化示例图书数据"""
    from datetime import datetime
    
    sample_books = [
        # 中医古籍
        {"name": "黄帝内经", "author": "佚名", "category": "中医基础", 
         "description": "中国最早的医学典籍，传统医学四大经典著作之一。", 
         "cover_url": "https://example.com/huangdi.jpg", "publish_date": datetime.now(),
         "pdf_file_path": "/books/sample_book.pdf"},
        {"name": "伤寒杂病论", "author": "张仲景", "category": "中医临床", 
         "description": "确立了辨证论治原则，是中医临床的基本原则。", 
         "cover_url": "https://example.com/shanghan.jpg", "publish_date": datetime.now(),
         "pdf_file_path": "/books/shanghan_zabing_lun.pdf"},
        {"name": "神农本草经", "author": "佚名", "category": "中药学", 
         "description": "中国现存最早的中药学著作。", 
         "cover_url": "https://example.com/shennong.jpg", "publish_date": datetime.now(),
         "pdf_file_path": "/books/sample_book.pdf"},
        {"name": "本草纲目", "author": "李时珍", "category": "中药学", 
         "description": "集我国16世纪以前药学成就之大成。", 
         "cover_url": "https://example.com/bencao.jpg", "publish_date": datetime.now(),
         "pdf_file_path": "/books/sample_book.pdf"},
        {"name": "针灸甲乙经", "author": "皇甫谧", "category": "针灸学", 
         "description": "中国现存最早的针灸学专著。", 
         "cover_url": "https://example.com/zhenjiu.jpg", "publish_date": datetime.now(),
         "pdf_file_path": "/books/sample_book.pdf"},
        
        # 西医经典
        {"name": "希波克拉底文集", "author": "希波克拉底", "category": "医学理论", 
         "description": "西方医学的奠基之作。", 
         "cover_url": "https://example.com/hippocrates.jpg", "publish_date": datetime.now(),
         "pdf_file_path": "/books/sample_book.pdf"},
        {"name": "人体的构造", "author": "维萨里", "category": "解剖学", 
         "description": "近代解剖学的奠基之作。", 
         "cover_url": "https://example.com/structure.jpg", "publish_date": datetime.now(),
         "pdf_file_path": "/books/sample_book.pdf"},
        {"name": "内科学原理与实践", "author": "奥斯勒", "category": "内科学", 
         "description": "现代内科学的奠基之作。", 
         "cover_url": "https://example.com/internal.jpg", "publish_date": datetime.now(),
         "pdf_file_path": "/books/sample_book.pdf"},
        {"name": "细胞病理学", "author": "微尔啸", "category": "病理学", 
         "description": "细胞病理学的创始之作。", 
         "cover_url": "https://example.com/cell.jpg", "publish_date": datetime.now(),
         "pdf_file_path": "/books/sample_book.pdf"},
        {"name": "医学衷中参西录", "author": "张锡纯", "category": "中西医结合", 
         "description": "试图结合中西医理论的著作。", 
         "cover_url": "https://example.com/combination.jpg", "publish_date": datetime.now(),
         "pdf_file_path": "/books/sample_book.pdf"},
    ]
    
    created_books = []
    for book_data in sample_books:
        # 检查是否已存在
        existing = db.query(BookModel).filter(
            BookModel.name == book_data["name"],
            BookModel.author == book_data["author"]
        ).first()
        
        if not existing:
            db_book = BookModel(**book_data)
            db.add(db_book)
            created_books.append(book_data["name"])
    
    db.commit()
    return {"message": f"成功创建 {len(created_books)} 本示例图书", "books": created_books}

# 获取书籍页面内容（分页）
@router.get("/{book_id}/pages")
async def get_book_pages(
    book_id: int,
    page: int = Query(1, ge=1, description="页码"),
    size: int = Query(10, ge=1, le=50, description="每页数量"),
    db: Session = Depends(get_db)
):
    """获取指定书籍的页面内容"""
    # 检查书籍是否存在
    book = db.query(BookModel).filter(BookModel.id == book_id).first()
    if not book:
        raise HTTPException(status_code=404, detail="书籍不存在")
    
    # 计算偏移量
    skip = (page - 1) * size
    
    # 查询书籍页面
    pages = db.query(BookPageModel).filter(
        BookPageModel.book_id == book_id
    ).order_by(BookPageModel.page_number).offset(skip).limit(size).all()
    
    # 如果没有页面数据，创建示例页面
    if not pages and page == 1:
        sample_pages = create_sample_pages(db, book_id, book.name)
        pages = db.query(BookPageModel).filter(
            BookPageModel.book_id == book_id
        ).order_by(BookPageModel.page_number).offset(skip).limit(size).all()
    
    # 转换为schema格式并返回标准API响应
    page_schemas = [BookPageSchema.model_validate(page) for page in pages]
    
    return {
        "success": True,
        "message": f"获取书籍页面成功，共{len(page_schemas)}页",
        "data": page_schemas
    }



# 创建图书
@router.post("/", response_model=BookSchema)
async def create_book(book: BookCreate, db: Session = Depends(get_db)):
    """创建图书"""
    db_book = BookModel(**book.dict())
    db.add(db_book)
    db.commit()
    db.refresh(db_book)
    return db_book

# 更新图书
@router.put("/{book_id}", response_model=BookSchema)
async def update_book(
    book_id: int, 
    book_update: BookUpdate, 
    db: Session = Depends(get_db)
):
    """更新图书"""
    db_book = db.query(BookModel).filter(BookModel.id == book_id).first()
    if not db_book:
        raise HTTPException(status_code=404, detail="图书不存在")
    
    # 更新字段
    update_data = book_update.dict(exclude_unset=True)
    for field, value in update_data.items():
        setattr(db_book, field, value)
    
    db.commit()
    db.refresh(db_book)
    return db_book

# 删除图书
@router.delete("/{book_id}")
async def delete_book(book_id: int, db: Session = Depends(get_db)):
    """删除图书"""
    db_book = db.query(BookModel).filter(BookModel.id == book_id).first()
    if not db_book:
        raise HTTPException(status_code=404, detail="图书不存在")
    
    db.delete(db_book)
    db.commit()
    return {"message": "图书删除成功"}

# 创建示例页面数据
def create_sample_pages(db: Session, book_id: int, book_name: str):
    """为书籍创建示例页面数据"""
    sample_pages = []
    
    # 创建20页示例内容
    for i in range(1, 21):
        page_content = f"""这是《{book_name}》第{i}页的内容。
        
本页主要讲述了医学相关的重要知识点，包括：

1. 基础理论概念的阐述
2. 临床实践的应用方法
3. 相关案例的分析讨论
4. 注意事项和禁忌症

通过系统的学习和理解，读者可以更好地掌握相关医学知识，为临床实践打下坚实的基础。

本页内容仅为示例，实际书籍内容会更加详细和专业。"""
        
        page_data = {
            "book_id": book_id,
            "page_number": i,
            "title": f"第{i}章 - 重要概念" if i % 5 == 1 else None,
            "content": page_content,
            "image_url": f"https://example.com/book_{book_id}_page_{i}.jpg" if i % 3 == 0 else None
        }
        
        db_page = BookPageModel(**page_data)
        db.add(db_page)
        sample_pages.append(db_page)
    
    db.commit()
    return sample_pages

# 获取单本图书详情 - 放在所有具体路径之后
@router.get("/{book_id}", response_model=BookSchema)
async def get_book(book_id: int, db: Session = Depends(get_db)):
    """获取单本图书详情"""
    book = db.query(BookModel).filter(BookModel.id == book_id).first()
    if not book:
        raise HTTPException(status_code=404, detail="图书不存在")
    return book

@router.get("/{book_id}/download")
async def download_book_pdf(book_id: int, db: Session = Depends(get_db)):
    """下载图书PDF文件"""
    book = db.query(BookModel).filter(BookModel.id == book_id).first()
    if not book:
        raise HTTPException(status_code=404, detail="图书不存在")
    
    if not book.pdf_file_path:
        raise HTTPException(status_code=404, detail="该图书没有PDF文件")
    
    # 构建完整的文件路径
    file_path = os.path.join("static", book.pdf_file_path.lstrip("/"))
    
    if not os.path.exists(file_path):
        raise HTTPException(status_code=404, detail="PDF文件不存在")
    
    # 返回文件下载响应
    return FileResponse(
        path=file_path,
        filename=f"{book.name}.pdf",
        media_type="application/pdf"
    )
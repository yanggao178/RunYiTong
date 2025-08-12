from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List, Optional
from database import get_db
from models import Book as BookModel
from schemas import Book, BookCreate, BookUpdate, PaginatedResponse

router = APIRouter()

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
        items=[Book.from_orm(book).dict() for book in books],
        total=total,
        page=page,
        size=size,
        pages=pages
    )

# 获取中医古籍列表
@router.get("/chinese-medicine", response_model=List[Book])
async def get_chinese_medicine_books(db: Session = Depends(get_db)):
    """获取中医古籍列表"""
    books = db.query(BookModel).filter(
        BookModel.category.in_(["中医基础", "中医临床", "中药学", "针灸学"])
    ).all()
    return books

# 获取西医经典列表
@router.get("/western-medicine", response_model=List[Book])
async def get_western_medicine_books(db: Session = Depends(get_db)):
    """获取西医经典列表"""
    books = db.query(BookModel).filter(
        BookModel.category.in_(["医学理论", "解剖学", "内科学", "病理学", "中西医结合"])
    ).all()
    return books

# 获取单本图书详情
@router.get("/{book_id}", response_model=Book)
async def get_book(book_id: int, db: Session = Depends(get_db)):
    """获取图书详情"""
    book = db.query(BookModel).filter(BookModel.id == book_id).first()
    if not book:
        raise HTTPException(status_code=404, detail="图书不存在")
    return book

# 创建图书
@router.post("/", response_model=Book)
async def create_book(book: BookCreate, db: Session = Depends(get_db)):
    """创建图书"""
    db_book = BookModel(**book.dict())
    db.add(db_book)
    db.commit()
    db.refresh(db_book)
    return db_book

# 更新图书
@router.put("/{book_id}", response_model=Book)
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
         "cover_url": "https://example.com/huangdi.jpg", "publish_date": datetime.now()},
        {"name": "伤寒杂病论", "author": "张仲景", "category": "中医临床", 
         "description": "确立了辨证论治原则，是中医临床的基本原则。", 
         "cover_url": "https://example.com/shanghan.jpg", "publish_date": datetime.now()},
        {"name": "神农本草经", "author": "佚名", "category": "中药学", 
         "description": "中国现存最早的中药学著作。", 
         "cover_url": "https://example.com/shennong.jpg", "publish_date": datetime.now()},
        {"name": "本草纲目", "author": "李时珍", "category": "中药学", 
         "description": "集我国16世纪以前药学成就之大成。", 
         "cover_url": "https://example.com/bencao.jpg", "publish_date": datetime.now()},
        {"name": "针灸甲乙经", "author": "皇甫谧", "category": "针灸学", 
         "description": "中国现存最早的针灸学专著。", 
         "cover_url": "https://example.com/zhenjiu.jpg", "publish_date": datetime.now()},
        
        # 西医经典
        {"name": "希波克拉底文集", "author": "希波克拉底", "category": "医学理论", 
         "description": "西方医学的奠基之作。", 
         "cover_url": "https://example.com/hippocrates.jpg", "publish_date": datetime.now()},
        {"name": "人体的构造", "author": "维萨里", "category": "解剖学", 
         "description": "近代解剖学的奠基之作。", 
         "cover_url": "https://example.com/structure.jpg", "publish_date": datetime.now()},
        {"name": "内科学原理与实践", "author": "奥斯勒", "category": "内科学", 
         "description": "现代内科学的奠基之作。", 
         "cover_url": "https://example.com/internal.jpg", "publish_date": datetime.now()},
        {"name": "细胞病理学", "author": "微尔啸", "category": "病理学", 
         "description": "细胞病理学的创始之作。", 
         "cover_url": "https://example.com/cell.jpg", "publish_date": datetime.now()},
        {"name": "医学衷中参西录", "author": "张锡纯", "category": "中西医结合", 
         "description": "试图结合中西医理论的著作。", 
         "cover_url": "https://example.com/combination.jpg", "publish_date": datetime.now()},
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
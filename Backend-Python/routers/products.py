from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List, Optional
from database import get_db
from models import Product as ProductModel
from schemas import Product, ProductCreate, ProductUpdate, PaginatedResponse

router = APIRouter()

# 获取商品列表（支持分页和搜索）
@router.get("/", response_model=PaginatedResponse)
async def get_products(
    page: int = Query(1, ge=1, description="页码"),
    size: int = Query(10, ge=1, le=100, description="每页数量"),
    search: Optional[str] = Query(None, description="搜索关键词"),
    category: Optional[str] = Query(None, description="商品分类"),
    db: Session = Depends(get_db)
):
    """获取商品列表"""
    query = db.query(ProductModel)
    
    # 搜索过滤
    if search:
        query = query.filter(ProductModel.name.contains(search))
    
    # 分类过滤
    if category:
        query = query.filter(ProductModel.category == category)
    
    # 计算总数
    total = query.count()
    
    # 分页
    offset = (page - 1) * size
    products = query.offset(offset).limit(size).all()
    
    # 计算总页数
    pages = (total + size - 1) // size
    
    return PaginatedResponse(
        items=[Product.from_orm(product).dict() for product in products],
        total=total,
        page=page,
        size=size,
        pages=pages
    )

# 获取单个商品详情
@router.get("/{product_id}", response_model=Product)
async def get_product(product_id: int, db: Session = Depends(get_db)):
    """获取商品详情"""
    product = db.query(ProductModel).filter(ProductModel.id == product_id).first()
    if not product:
        raise HTTPException(status_code=404, detail="商品不存在")
    return product

# 创建商品
@router.post("/", response_model=Product)
async def create_product(product: ProductCreate, db: Session = Depends(get_db)):
    """创建商品"""
    db_product = ProductModel(**product.dict())
    db.add(db_product)
    db.commit()
    db.refresh(db_product)
    return db_product

# 更新商品
@router.put("/{product_id}", response_model=Product)
async def update_product(
    product_id: int, 
    product_update: ProductUpdate, 
    db: Session = Depends(get_db)
):
    """更新商品"""
    db_product = db.query(ProductModel).filter(ProductModel.id == product_id).first()
    if not db_product:
        raise HTTPException(status_code=404, detail="商品不存在")
    
    # 更新字段
    update_data = product_update.dict(exclude_unset=True)
    for field, value in update_data.items():
        setattr(db_product, field, value)
    
    db.commit()
    db.refresh(db_product)
    return db_product

# 删除商品
@router.delete("/{product_id}")
async def delete_product(product_id: int, db: Session = Depends(get_db)):
    """删除商品"""
    db_product = db.query(ProductModel).filter(ProductModel.id == product_id).first()
    if not db_product:
        raise HTTPException(status_code=404, detail="商品不存在")
    
    db.delete(db_product)
    db.commit()
    return {"message": "商品删除成功"}

# 获取商品分类列表
@router.get("/categories/list")
async def get_categories(db: Session = Depends(get_db)):
    """获取所有商品分类"""
    categories = db.query(ProductModel.category).distinct().all()
    return {"categories": [cat[0] for cat in categories if cat[0]]}

# 增加商品购买人数
@router.post("/{product_id}/purchase")
async def purchase_product(product_id: int, db: Session = Depends(get_db)):
    """增加商品购买人数"""
    db_product = db.query(ProductModel).filter(ProductModel.id == product_id).first()
    if not db_product:
        raise HTTPException(status_code=404, detail="商品不存在")
    
    db_product.purchase_count += 1
    db.commit()
    db.refresh(db_product)
    return {"message": "购买成功", "purchase_count": db_product.purchase_count}
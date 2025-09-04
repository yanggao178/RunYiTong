from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, Query
from database import get_db
from models import Product as ProductModel
from schemas import Product, ProductCreate, ProductUpdate, PaginatedResponse, BaseResponse

router = APIRouter()

# 获取商品列表（支持分页和搜索）
@router.get("/")
async def get_products(
    skip: int = Query(0, ge=0, description="跳过数量"),
    limit: int = Query(10, ge=1, le=100, description="限制数量"),
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
    products = query.offset(skip).limit(limit).all()
    
    # 手动构造商品数据，确保字段匹配
    items = []
    for product in products:
        item = {
            "id": product.id,
            "name": product.name,
            "price": float(product.price) if product.price else 0.0,
            "description": product.description or "",
            "featured_image_url": product.featured_image_url or "",
            "category_id": product.category_id,
            "stock_quantity": product.stock_quantity or 0,
            "manufacturer": product.manufacturer or "",
            "pharmacy_name": product.pharmacy_name or "",
            "sales_count": product.sales_count or 0,
            "created_at": product.created_at.isoformat() if product.created_at else None,
            "updated_at": product.updated_at.isoformat() if product.updated_at else None
        }
        items.append(item)
    
    # 构造ProductListResponse格式的数据
    product_list_data = {
        "items": items,
        "total": total,
        "skip": skip,
        "limit": limit
    }
    
    return {
        "success": True,
        "message": "获取商品列表成功",
        "data": product_list_data
    }

# 根据药店名称获取商品列表（必须在{product_id}路由之前定义）
@router.get("/pharmacy")
async def get_products_by_pharmacy(
    pharmacy_name: str = Query(..., description="药店名称"),
    db: Session = Depends(get_db)
):
    """根据药店名称获取商品列表"""
    try:
        # 检查药店名称是否为空
        if not pharmacy_name or pharmacy_name.strip() == "":
            raise HTTPException(status_code=400, detail="药店名称不能为空")
        
        # 查询指定药店的所有商品
        products = db.query(ProductModel).filter(ProductModel.pharmacy_name == pharmacy_name).all()
        
        # 手动构造商品数据，确保字段匹配
        items = []
        for product in products:
            item = {
                "id": product.id,
                "name": product.name,
                "price": float(product.price) if product.price else 0.0,
                "description": product.description or "",
                "featured_image_url": product.featured_image_url or "",
                "category_id": product.category_id,
                "stock_quantity": product.stock_quantity or 0,
                "manufacturer": product.manufacturer or "",
                "pharmacy_name": product.pharmacy_name or "",
                "sales_count": product.sales_count or 0,
                "created_at": product.created_at.isoformat() if product.created_at else None,
                "updated_at": product.updated_at.isoformat() if product.updated_at else None
            }
            items.append(item)
        
        # 构造ProductListResponse格式的数据
        product_list_data = {
            "items": items,
            "total": len(items),
            "skip": 0,
            "limit": len(items)
        }
        
        # 返回符合BaseResponse格式的响应
        return {
            "success": True,
            "message": f"获取{pharmacy_name}的商品列表成功",
            "data": product_list_data
        }
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"服务器内部错误: {str(e)}")

# 获取单个商品详情
@router.get("/{product_id}")
async def get_product(product_id: int, db: Session = Depends(get_db)):
    """获取商品详情"""
    product = db.query(ProductModel).filter(ProductModel.id == product_id).first()
    if not product:
        raise HTTPException(status_code=404, detail="商品不存在")
    
    # 手动构造商品数据
    product_data = {
        "id": product.id,
        "name": product.name,
        "price": float(product.price) if product.price else 0.0,
        "description": product.description or "",
        "featured_image_url": product.featured_image_url or "",
        "category_id": product.category_id,
        "stock_quantity": product.stock_quantity or 0,
        "manufacturer": product.manufacturer or "",
        "pharmacy_name": product.pharmacy_name or "",
        "sales_count": product.sales_count or 0,
        "created_at": product.created_at.isoformat() if product.created_at else None,
        "updated_at": product.updated_at.isoformat() if product.updated_at else None
    }
    
    return {
        "success": True,
        "message": "获取商品详情成功",
        "data": product_data
    }

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
    
    db_product.sales_count += 1  # 修复：使用正确的字段名
    db.commit()
    db.refresh(db_product)
    return {"message": "购买成功", "purchase_count": db_product.sales_count}  # 保持API响应兼容性


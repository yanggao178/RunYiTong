from sqlalchemy.orm import Session
from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, Query
from database import get_db
from models import Order as OrderModel
from schemas import OrderCreate, OrderUpdate, OrderResponse, OrderListResponse, BaseResponse
import uuid
from datetime import datetime

router = APIRouter()

# 创建订单
@router.post("/")
async def create_order(
    order: OrderCreate,
    db: Session = Depends(get_db)
):
    """创建新订单"""
    # 检查订单ID是否已存在
    existing_order = db.query(OrderModel).filter(OrderModel.order_id == order.order_id).first()
    if existing_order:
        raise HTTPException(status_code=400, detail="订单ID已存在")
    
    # 创建订单对象
    db_order = OrderModel(
        order_id=order.order_id,
        user_id=order.user_id,
        product_name=order.product_name,
        status=order.status,
        price=order.price,
        create_time=order.create_time or datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        pay_time=order.pay_time,
        shipping_address=order.shipping_address
    )
    
    # 添加到数据库
    db.add(db_order)
    db.commit()
    db.refresh(db_order)
    
    # 转换为响应模型
    response_order = OrderResponse.from_orm(db_order)
    
    return {
        "success": True,
        "message": "订单创建成功",
        "data": response_order
    }

# 获取订单列表
@router.get("/")
async def get_orders(
    skip: int = Query(0, ge=0, description="跳过数量"),
    limit: int = Query(10, ge=1, le=100, description="限制数量"),
    user_id: Optional[int] = Query(None, description="用户ID"),
    status: Optional[str] = Query(None, description="订单状态"),
    db: Session = Depends(get_db)
):
    """获取订单列表，支持分页和筛选"""
    query = db.query(OrderModel)
    
    # 根据用户ID筛选
    if user_id:
        query = query.filter(OrderModel.user_id == user_id)
    
    # 根据状态筛选
    if status:
        query = query.filter(OrderModel.status == status)
    
    # 计算总数
    total = query.count()
    
    # 分页
    orders = query.offset(skip).limit(limit).all()
    
    # 转换为响应模型列表
    order_responses = [OrderResponse.from_orm(order) for order in orders]
    
    return OrderListResponse(
        data=order_responses,
        total=total,
        page=skip // limit + 1,
        size=limit
    )

# 获取订单详情
@router.get("/{order_id}")
async def get_order(
    order_id: str,
    db: Session = Depends(get_db)
):
    """获取订单详情"""
    # 根据订单ID查找订单
    db_order = db.query(OrderModel).filter(OrderModel.order_id == order_id).first()
    
    if not db_order:
        raise HTTPException(status_code=404, detail="订单不存在")
    
    # 转换为响应模型
    response_order = OrderResponse.from_orm(db_order)
    
    return {
        "success": True,
        "message": "获取订单详情成功",
        "data": response_order
    }

# 更新订单
@router.put("/{order_id}")
async def update_order(
    order_id: str,
    order: OrderUpdate,
    db: Session = Depends(get_db)
):
    """更新订单信息"""
    # 查找订单
    db_order = db.query(OrderModel).filter(OrderModel.order_id == order_id).first()
    
    if not db_order:
        raise HTTPException(status_code=404, detail="订单不存在")
    
    # 更新订单信息
    update_data = order.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_order, key, value)
    
    # 如果状态更新为支付成功，设置支付时间
    if "status" in update_data and update_data["status"] == "paid" and not db_order.pay_time:
        db_order.pay_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    
    db.commit()
    db.refresh(db_order)
    
    # 转换为响应模型
    response_order = OrderResponse.from_orm(db_order)
    
    return {
        "success": True,
        "message": "订单更新成功",
        "data": response_order
    }

# 删除订单
@router.delete("/{order_id}")
async def delete_order(
    order_id: str,
    db: Session = Depends(get_db)
):
    """删除订单"""
    # 查找订单
    db_order = db.query(OrderModel).filter(OrderModel.order_id == order_id).first()
    
    if not db_order:
        raise HTTPException(status_code=404, detail="订单不存在")
    
    # 删除订单
    db.delete(db_order)
    db.commit()
    
    return {
        "success": True,
        "message": "订单删除成功",
        "data": None
    }

# 批量获取用户订单
@router.get("/user/{user_id}", response_model=OrderListResponse)
async def get_user_orders(
    user_id: int,
    skip: int = Query(0, ge=0, description="跳过数量"),
    limit: int = Query(10, ge=1, le=100, description="限制数量"),
    status: Optional[str] = Query(None, description="订单状态"),
    db: Session = Depends(get_db)
):
    """批量获取指定用户的订单"""
    query = db.query(OrderModel).filter(OrderModel.user_id == user_id)
    
    # 根据状态筛选
    # if status:
    #     query = query.filter(OrderModel.status == status)
    
    # 按创建时间降序排序
    query = query.order_by(OrderModel.created_at.desc())
    
    # 计算总数
    total = query.count()
    
    # 分页
    orders = query.offset(skip).limit(limit).all()
    
    # 转换为响应模型列表
    order_responses = [OrderResponse.from_orm(order) for order in orders]
    
    return OrderListResponse(
        data=order_responses,
        total=total,
        page=skip // limit + 1,
        size=limit
    )

# 生成唯一订单ID
@router.get("/generate-id")
async def generate_order_id():
    """生成唯一的订单ID"""
    # 生成基于时间戳和随机数的订单ID
    timestamp = datetime.now().strftime("%Y%m%d%H%M%S")
    random_part = str(uuid.uuid4())[:8]
    order_id = f"ORDER_{timestamp}_{random_part}"
    
    return {
        "success": True,
        "message": "订单ID生成成功",
        "data": {"order_id": order_id}
    }
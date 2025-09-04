#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
收货地址管理API路由
基于Address.java模型类的后端API实现
"""

from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from sqlalchemy import and_, or_
from typing import List, Optional
from pydantic import BaseModel, Field
from database import get_db
from models import Address, User
from datetime import datetime
import logging
from schemas import AddressBase,AddressCreate,AddressUpdate,AddressResponse,AddressListResponse,BaseResponse

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# 创建路由器
# router = APIRouter(prefix="/addresses", tags=["收货地址管理"])
router = APIRouter()
# Pydantic 数据模型
# class AddressBase(BaseModel):
#     """地址基础模型"""
#     name: str = Field(..., description="收件人姓名", min_length=1, max_length=100)
#     phone: str = Field(..., description="联系电话", min_length=1, max_length=20)
#     province: str = Field(..., description="省份", min_length=1, max_length=50)
#     city: str = Field(..., description="城市", min_length=1, max_length=50)
#     district: str = Field(..., description="区县", min_length=1, max_length=50)
#     detail_address: str = Field(..., description="详细地址", min_length=1, max_length=500)
#     is_default: bool = Field(False, description="是否默认地址")
#     latitude: Optional[str] = Field(None, description="纬度", max_length=20)
#     longitude: Optional[str] = Field(None, description="经度", max_length=20)
#
# class AddressCreate(AddressBase):
#     """创建地址模型"""
#     user_id: int = Field(..., description="用户ID", gt=0)
#
# class AddressUpdate(AddressBase):
#     """更新地址模型"""
#     name: Optional[str] = Field(None, description="收件人姓名", min_length=1, max_length=100)
#     phone: Optional[str] = Field(None, description="联系电话", min_length=1, max_length=20)
#     province: Optional[str] = Field(None, description="省份", min_length=1, max_length=50)
#     city: Optional[str] = Field(None, description="城市", min_length=1, max_length=50)
#     district: Optional[str] = Field(None, description="区县", min_length=1, max_length=50)
#     detail_address: Optional[str] = Field(None, description="详细地址", min_length=1, max_length=500)
#     is_default: Optional[bool] = Field(None, description="是否默认地址")
#     latitude: Optional[str] = Field(None, description="纬度", max_length=20)
#     longitude: Optional[str] = Field(None, description="经度", max_length=20)
#
# class AddressResponse(AddressBase):
#     """地址响应模型 - 与Address.java字段完全对应"""
#     id: int = Field(..., description="地址ID")
#     user_id: int = Field(..., description="用户ID")
#     created_time: datetime = Field(..., description="创建时间")
#     updated_time: datetime = Field(..., description="更新时间")
#
#     class Config:
#         from_attributes = True
#
# class AddressListResponse(BaseModel):
#     """地址列表响应模型"""
#     addresses: List[AddressResponse]
#     total: int
#     page: int
#     size: int

# API端点实现

@router.post("/", response_model=BaseResponse, status_code=status.HTTP_201_CREATED)
async def create_address(
    address_data: AddressCreate,
    db: Session = Depends(get_db)
):
    """
    创建新的收货地址
    
    - **user_id**: 用户ID
    - **name**: 收件人姓名
    - **phone**: 联系电话
    - **province**: 省份
    - **city**: 城市
    - **district**: 区县
    - **detail_address**: 详细地址
    - **is_default**: 是否默认地址
    - **latitude**: 纬度（可选）
    - **longitude**: 经度（可选）
    """
    try:
        # 验证用户是否存在
        # user = db.query(User).filter(User.id == address_data.user_id).first()
        # if not user:
        #     raise HTTPException(
        #         status_code=status.HTTP_404_NOT_FOUND,
        #         detail=f"用户ID {address_data.user_id} 不存在"
        #     )
        
        # 如果设置为默认地址，先取消该用户的其他默认地址
        if address_data.is_default:
            db.query(Address).filter(
                and_(Address.user_id == address_data.user_id, Address.is_default == True)
            ).update({Address.is_default: False})
        
        # 创建新地址
        new_address = Address(
            user_id=address_data.user_id,
            name=address_data.name,
            phone=address_data.phone,
            province=address_data.province,
            city=address_data.city,
            district=address_data.district,
            detail_address=address_data.detail_address,
            is_default=address_data.is_default,
            latitude=address_data.latitude,
            longitude=address_data.longitude
        )
        
        db.add(new_address)
        db.commit()
        db.refresh(new_address)
        
        logger.info(f"用户 {address_data.user_id} 创建新地址: {new_address.id}")
        
        # Convert the SQLAlchemy model to a dict before returning
        address_dict = {
            "id": new_address.id,
            "user_id": new_address.user_id,
            "name": new_address.name,
            "phone": new_address.phone,
            "province": new_address.province,
            "city": new_address.city,
            "district": new_address.district,
            "detail_address": new_address.detail_address,
            "is_default": new_address.is_default,
            "latitude": new_address.latitude,
            "longitude": new_address.longitude,
            "created_time": new_address.created_time,
            "updated_time": new_address.updated_time
        }
        
        return BaseResponse(
            success=True,
            message="地址创建成功",
            data=address_dict
        )
        
    except HTTPException:
        raise
    except Exception as e:
        db.rollback()
        logger.error(f"创建地址失败: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"创建地址失败: {str(e)}"
        )

@router.get("/user/{user_id}", response_model=AddressListResponse)
async def get_user_addresses(
    user_id: int,
    page: int = Query(1, ge=1, description="页码"),
    size: int = Query(10, ge=1, le=100, description="每页数量"),
    db: Session = Depends(get_db)
):
    """
    获取用户的收货地址列表
    
    - **user_id**: 用户ID
    - **page**: 页码（从1开始）
    - **size**: 每页数量（最大100）
    """
    try:
        # 验证用户是否存在
        user = db.query(User).filter(User.id == user_id).first()
        if not user:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"用户ID {user_id} 不存在"
            )
        
        # 计算偏移量
        offset = (page - 1) * size
        
        # 查询地址列表（默认地址优先显示）
        addresses_query = db.query(Address).filter(Address.user_id == user_id)
        total = addresses_query.count()
        
        addresses = addresses_query.order_by(
            Address.is_default.desc(),  # 默认地址优先
            Address.created_time.desc()  # 创建时间倒序
        ).offset(offset).limit(size).all()
        
        # Convert SQLAlchemy models to dicts before returning
        address_dicts = []
        for address in addresses:
            address_dict = {
                "id": address.id,
                "user_id": address.user_id,
                "name": address.name,
                "phone": address.phone,
                "province": address.province,
                "city": address.city,
                "district": address.district,
                "detail_address": address.detail_address,
                "is_default": address.is_default,
                "latitude": address.latitude,
                "longitude": address.longitude,
                "created_time": address.created_time,
                "updated_time": address.updated_time
            }
            address_dicts.append(address_dict)
        
        return AddressListResponse(
            data=address_dicts,
            total=total,
            page=page,
            size=size
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"获取用户地址列表失败: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"获取地址列表失败: {str(e)}"
        )

@router.get("/{address_id}", response_model=AddressResponse)
async def get_address(
    address_id: int,
    db: Session = Depends(get_db)
):
    """
    获取单个地址详情
    
    - **address_id**: 地址ID
    """
    try:
        address = db.query(Address).filter(Address.id == address_id).first()
        if not address:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"地址ID {address_id} 不存在"
            )
        
        # Convert the SQLAlchemy model to a dict before returning
        address_dict = {
            "id": address.id,
            "user_id": address.user_id,
            "name": address.name,
            "phone": address.phone,
            "province": address.province,
            "city": address.city,
            "district": address.district,
            "detail_address": address.detail_address,
            "is_default": address.is_default,
            "latitude": address.latitude,
            "longitude": address.longitude,
            "created_time": address.created_time,
            "updated_time": address.updated_time
        }
        
        return address_dict
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"获取地址详情失败: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"获取地址详情失败: {str(e)}"
        )

@router.put("/{address_id}", response_model=BaseResponse)
async def update_address(
    address_id: int,
    address_data: AddressUpdate,
    db: Session = Depends(get_db)
):
    """
    更新收货地址
    
    - **address_id**: 地址ID
    - 其他字段为可选更新字段
    """
    try:
        # 查找地址
        address = db.query(Address).filter(Address.id == address_id).first()
        if not address:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"地址ID {address_id} 不存在"
            )
        
        # 如果设置为默认地址，先取消该用户的其他默认地址
        if address_data.is_default is True:
            db.query(Address).filter(
                and_(
                    Address.user_id == address.user_id, 
                    Address.id != address_id,
                    Address.is_default == True
                )
            ).update({Address.is_default: False})
        
        # 更新地址字段
        update_data = address_data.dict(exclude_unset=True)
        for field, value in update_data.items():
            setattr(address, field, value)
        
        address.updated_time = datetime.utcnow()
        
        db.commit()
        db.refresh(address)
        
        logger.info(f"地址 {address_id} 更新成功")
        
        # Convert the SQLAlchemy model to a dict before returning
        address_dict = {
            "id": address.id,
            "user_id": address.user_id,
            "name": address.name,
            "phone": address.phone,
            "province": address.province,
            "city": address.city,
            "district": address.district,
            "detail_address": address.detail_address,
            "is_default": address.is_default,
            "latitude": address.latitude,
            "longitude": address.longitude,
            "created_time": address.created_time,
            "updated_time": address.updated_time
        }
        
        return BaseResponse(
            success=True,
            message="地址更新成功",
            data=address_dict
        )
        
    except HTTPException:
        raise
    except Exception as e:
        db.rollback()
        logger.error(f"更新地址失败: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"更新地址失败: {str(e)}"
        )

@router.delete("/{address_id}", response_model=BaseResponse)
async def delete_address(
    address_id: int,
    db: Session = Depends(get_db)
):
    """
    删除收货地址
    
    - **address_id**: 地址ID
    """
    try:
        address = db.query(Address).filter(Address.id == address_id).first()
        if not address:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"地址ID {address_id} 不存在"
            )
        
        db.delete(address)
        db.commit()
        
        logger.info(f"地址 {address_id} 删除成功")
        return {
            "success":True,
            "message":"地址删除成功",
            "data":None
        }
        
    except HTTPException:
        raise
    except Exception as e:
        db.rollback()
        logger.error(f"删除地址失败: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"删除地址失败: {str(e)}"
        )

@router.put("/{address_id}/set-default", response_model=AddressResponse)
async def set_default_address(
    address_id: int,
    db: Session = Depends(get_db)
):
    """
    设置默认地址
    
    - **address_id**: 地址ID
    """
    try:
        address = db.query(Address).filter(Address.id == address_id).first()
        if not address:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"地址ID {address_id} 不存在"
            )
        
        # 取消该用户的其他默认地址
        db.query(Address).filter(
            and_(
                Address.user_id == address.user_id,
                Address.id != address_id,
                Address.is_default == True
            )
        ).update({Address.is_default: False})
        
        # 设置当前地址为默认
        address.is_default = True
        address.updated_time = datetime.utcnow()
        
        db.commit()
        db.refresh(address)
        
        logger.info(f"地址 {address_id} 设置为默认地址")
        
        # Convert the SQLAlchemy model to a dict before returning
        address_dict = {
            "id": address.id,
            "user_id": address.user_id,
            "name": address.name,
            "phone": address.phone,
            "province": address.province,
            "city": address.city,
            "district": address.district,
            "detail_address": address.detail_address,
            "is_default": address.is_default,
            "latitude": address.latitude,
            "longitude": address.longitude,
            "created_time": address.created_time,
            "updated_time": address.updated_time
        }
        
        return address_dict
        
    except HTTPException:
        raise
    except Exception as e:
        db.rollback()
        logger.error(f"设置默认地址失败: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"设置默认地址失败: {str(e)}"
        )

@router.get("/user/{user_id}/default", response_model=Optional[AddressResponse])
async def get_default_address(
    user_id: int,
    db: Session = Depends(get_db)
):
    """
    获取用户的默认地址
    
    - **user_id**: 用户ID
    """
    try:
        # 验证用户是否存在
        user = db.query(User).filter(User.id == user_id).first()
        if not user:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"用户ID {user_id} 不存在"
            )
        
        # 查找默认地址
        default_address = db.query(Address).filter(
            and_(Address.user_id == user_id, Address.is_default == True)
        ).first()
        
        if default_address:
            # Convert the SQLAlchemy model to a dict before returning
            address_dict = {
                "id": default_address.id,
                "user_id": default_address.user_id,
                "name": default_address.name,
                "phone": default_address.phone,
                "province": default_address.province,
                "city": default_address.city,
                "district": default_address.district,
                "detail_address": default_address.detail_address,
                "is_default": default_address.is_default,
                "latitude": default_address.latitude,
                "longitude": default_address.longitude,
                "created_time": default_address.created_time,
                "updated_time": default_address.updated_time
            }
            return address_dict
        
        return None
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"获取默认地址失败: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"获取默认地址失败: {str(e)}"
        )
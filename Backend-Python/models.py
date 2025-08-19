from sqlalchemy import Column, Integer, String, Float, DateTime, Text, Boolean, ForeignKey
from sqlalchemy.orm import relationship
from database import Base
from datetime import datetime

# 商品模型 - 与ai_medical.db products表结构保持一致
class Product(Base):
    __tablename__ = "products"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(200), nullable=False, index=True)
    slug = Column(String(50))
    description = Column(Text)
    short_description = Column(String(500))
    category_id = Column(Integer)
    department_id = Column(Integer)
    price = Column(Float, nullable=False)
    original_price = Column(Float)
    stock_quantity = Column(Integer, default=0)
    min_stock_level = Column(Integer)
    sku = Column(String(100))
    barcode = Column(String(100))
    weight = Column(Float)
    dimensions = Column(String(100))
    featured_image_url = Column(String(500))
    gallery_images = Column(Text)
    tags = Column(String(200))
    status = Column(String(20))
    is_featured = Column(Boolean, default=False)
    is_prescription_required = Column(Boolean, default=False)
    manufacturer = Column(String(200))
    expiry_date = Column(DateTime)
    usage_instructions = Column(Text)
    side_effects = Column(Text)
    contraindications = Column(Text)
    views_count = Column(Integer, default=0)
    sales_count = Column(Integer, default=0)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

# 图书模型
class Book(Base):
    __tablename__ = "books"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), nullable=False, index=True)
    author = Column(String(255), nullable=False)
    category = Column(String(100), index=True)
    description = Column(Text)
    cover_url = Column(String(500))
    publish_date = Column(DateTime)
    created_time = Column(DateTime, default=datetime.utcnow)
    updated_time = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

# 用户模型
class User(Base):
    __tablename__ = "users"
    
    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(100), unique=True, nullable=False, index=True)
    email = Column(String(255), unique=True, nullable=False, index=True)
    hashed_password = Column(String(255), nullable=False)
    full_name = Column(String(255))
    phone = Column(String(20))
    avatar_url = Column(String(500))
    is_active = Column(Boolean, default=True)
    created_time = Column(DateTime, default=datetime.utcnow)
    updated_time = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # 关联关系
    appointments = relationship("Appointment", back_populates="user")
    prescriptions = relationship("Prescription", back_populates="user")

# 预约挂号模型
class Appointment(Base):
    __tablename__ = "appointments"
    
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    department = Column(String(100), nullable=False)  # 科室
    doctor_name = Column(String(100))  # 医生姓名
    appointment_date = Column(DateTime, nullable=False)  # 预约日期
    appointment_time = Column(String(20))  # 预约时间段
    status = Column(String(20), default="pending")  # 状态：pending, confirmed, cancelled, completed
    symptoms = Column(Text)  # 症状描述
    notes = Column(Text)  # 备注
    created_time = Column(DateTime, default=datetime.utcnow)
    updated_time = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # 关联关系
    user = relationship("User", back_populates="appointments")

# 处方模型
class Prescription(Base):
    __tablename__ = "prescriptions"
    
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    symptoms = Column(Text, nullable=False)  # 症状描述
    diagnosis = Column(Text)  # 诊断结果
    prescription_content = Column(Text)  # 处方内容
    doctor_name = Column(String(100))  # 开方医生
    status = Column(String(20), default="draft")  # 状态：draft, issued, dispensed
    image_url = Column(String(500))  # 处方图片URL
    created_time = Column(DateTime, default=datetime.utcnow)
    updated_time = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # 关联关系
    user = relationship("User", back_populates="prescriptions")

# 健康档案模型
class HealthRecord(Base):
    __tablename__ = "health_records"
    
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    record_type = Column(String(50), nullable=False)  # 记录类型：blood_pressure, weight, etc.
    value = Column(String(100))  # 记录值
    unit = Column(String(20))  # 单位
    notes = Column(Text)  # 备注
    recorded_date = Column(DateTime, default=datetime.utcnow)
    created_time = Column(DateTime, default=datetime.utcnow)
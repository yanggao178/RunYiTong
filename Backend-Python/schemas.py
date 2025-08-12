from pydantic import BaseModel, EmailStr
from datetime import datetime
from typing import Optional, List

# 基础响应模型
class BaseResponse(BaseModel):
    success: bool = True
    message: str = "操作成功"
    data: Optional[dict] = None

# 商品相关模式
class ProductBase(BaseModel):
    name: str
    price: float
    description: Optional[str] = None
    image_url: Optional[str] = None
    category: Optional[str] = None
    stock: int = 0
    specification: Optional[str] = None
    manufacturer: Optional[str] = None
    purchase_count: int = 0

class ProductCreate(ProductBase):
    pass

class ProductUpdate(BaseModel):
    name: Optional[str] = None
    price: Optional[float] = None
    description: Optional[str] = None
    image_url: Optional[str] = None
    category: Optional[str] = None
    stock: Optional[int] = None
    specification: Optional[str] = None
    manufacturer: Optional[str] = None
    purchase_count: Optional[int] = None

class Product(ProductBase):
    id: int
    created_time: datetime
    updated_time: datetime
    
    class Config:
        from_attributes = True

# 图书相关模式
class BookBase(BaseModel):
    name: str
    author: str
    category: Optional[str] = None
    description: Optional[str] = None
    cover_url: Optional[str] = None
    publish_date: Optional[datetime] = None

class BookCreate(BookBase):
    pass

class BookUpdate(BaseModel):
    name: Optional[str] = None
    author: Optional[str] = None
    category: Optional[str] = None
    description: Optional[str] = None
    cover_url: Optional[str] = None
    publish_date: Optional[datetime] = None

class Book(BookBase):
    id: int
    created_time: datetime
    updated_time: datetime
    
    class Config:
        from_attributes = True

# 用户相关模式
class UserBase(BaseModel):
    username: str
    email: EmailStr
    full_name: Optional[str] = None
    phone: Optional[str] = None
    avatar_url: Optional[str] = None

class UserCreate(UserBase):
    password: str

class UserUpdate(BaseModel):
    username: Optional[str] = None
    email: Optional[EmailStr] = None
    full_name: Optional[str] = None
    phone: Optional[str] = None
    avatar_url: Optional[str] = None
    password: Optional[str] = None

class User(UserBase):
    id: int
    is_active: bool
    created_time: datetime
    updated_time: datetime
    
    class Config:
        from_attributes = True

# 预约挂号相关模式
class AppointmentBase(BaseModel):
    department: str
    doctor_name: Optional[str] = None
    appointment_date: datetime
    appointment_time: Optional[str] = None
    symptoms: Optional[str] = None
    notes: Optional[str] = None

class AppointmentCreate(AppointmentBase):
    user_id: int

class AppointmentUpdate(BaseModel):
    department: Optional[str] = None
    doctor_name: Optional[str] = None
    appointment_date: Optional[datetime] = None
    appointment_time: Optional[str] = None
    status: Optional[str] = None
    symptoms: Optional[str] = None
    notes: Optional[str] = None

class Appointment(AppointmentBase):
    id: int
    user_id: int
    status: str
    created_time: datetime
    updated_time: datetime
    
    class Config:
        from_attributes = True

# 处方相关模式
class PrescriptionBase(BaseModel):
    symptoms: str
    diagnosis: Optional[str] = None
    prescription_content: Optional[str] = None
    doctor_name: Optional[str] = None
    image_url: Optional[str] = None

class PrescriptionCreate(PrescriptionBase):
    user_id: int

class PrescriptionUpdate(BaseModel):
    symptoms: Optional[str] = None
    diagnosis: Optional[str] = None
    prescription_content: Optional[str] = None
    doctor_name: Optional[str] = None
    status: Optional[str] = None
    image_url: Optional[str] = None

class Prescription(PrescriptionBase):
    id: int
    user_id: int
    status: str
    created_time: datetime
    updated_time: datetime
    
    class Config:
        from_attributes = True

# 健康档案相关模式
class HealthRecordBase(BaseModel):
    record_type: str
    value: str
    unit: Optional[str] = None
    notes: Optional[str] = None
    recorded_date: Optional[datetime] = None

class HealthRecordCreate(HealthRecordBase):
    user_id: int

class HealthRecord(HealthRecordBase):
    id: int
    user_id: int
    created_time: datetime
    
    class Config:
        from_attributes = True

# 分页响应模式
class PaginatedResponse(BaseModel):
    items: List[dict]
    total: int
    page: int
    size: int
    pages: int
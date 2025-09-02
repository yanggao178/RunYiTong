from pydantic import BaseModel, EmailStr
from datetime import datetime
from typing import Optional, List
from pydantic import BaseModel, EmailStr

# 基础响应模型
from typing import Any

class BaseResponse(BaseModel):
    success: bool = True
    message: str = "操作成功"
    data: Optional[Any] = None

# 商品相关模式
class ProductBase(BaseModel):
    name: str
    price: float
    description: Optional[str] = None
    featured_image_url: Optional[str] = None
    category_id: Optional[int] = None
    stock_quantity: int = 0
    manufacturer: Optional[str] = None
    pharmacy_name: Optional[str] = None
    sales_count: int = 0

class ProductCreate(ProductBase):
    pass

class ProductUpdate(BaseModel):
    name: Optional[str] = None
    price: Optional[float] = None
    description: Optional[str] = None
    featured_image_url: Optional[str] = None
    category_id: Optional[int] = None
    stock_quantity: Optional[int] = None
    manufacturer: Optional[str] = None
    sales_count: Optional[int] = None

class Product(ProductBase):
    id: int
    created_at: datetime
    updated_at: datetime
    
    class Config:
        from_attributes = True

# 图书相关模式
class BookBase(BaseModel):
    name: str
    author: str
    category: Optional[str] = None
    description: Optional[str] = None
    cover_url: Optional[str] = None
    pdf_file_path: Optional[str] = None
    file_size: Optional[int] = None
    publish_date: Optional[datetime] = None

class BookCreate(BookBase):
    pass

class BookUpdate(BaseModel):
    name: Optional[str] = None
    author: Optional[str] = None
    category: Optional[str] = None
    description: Optional[str] = None
    cover_url: Optional[str] = None
    pdf_file_path: Optional[str] = None
    file_size: Optional[int] = None
    publish_date: Optional[datetime] = None

class BookSchema(BookBase):
    id: int
    created_time: datetime
    updated_time: datetime
    
    class Config:
        from_attributes = True

# 图书页面相关模式
class BookPageBase(BaseModel):
    book_id: int
    page_number: int
    title: Optional[str] = None
    content: Optional[str] = None
    image_url: Optional[str] = None

class BookPageCreate(BookPageBase):
    pass

class BookPageUpdate(BaseModel):
    page_number: Optional[int] = None
    title: Optional[str] = None
    content: Optional[str] = None
    image_url: Optional[str] = None

class BookPage(BookPageBase):
    id: int
    created_time: datetime
    updated_time: datetime
    
    class Config:
        from_attributes = True

# 用户相关模式
class UserBase(BaseModel):
    username: str
    email: Optional[EmailStr] = None
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
    name: Optional[str] = None
    gender: Optional[str] = None
    birthdate: Optional[datetime] = None
    height: Optional[float] = None
    weight: Optional[float] = None
    blood_type: Optional[str] = None
    allergy_history: Optional[str] = None
    medication_history: Optional[str] = None
    family_medical_history: Optional[str] = None
    emergency_contact_name: Optional[str] = None
    emergency_contact_phone: Optional[str] = None
    created_time: Optional[datetime] = None
    updated_time: Optional[datetime] = None

class HealthRecordCreate(HealthRecordBase):
    pass

class HealthRecordUpdate(BaseModel):
    name: Optional[str] = None
    gender: Optional[str] = None
    birthdate: Optional[datetime] = None
    height: Optional[float] = None
    weight: Optional[float] = None
    blood_type: Optional[str] = None
    allergy_history: Optional[str] = None
    chronic_diseases: Optional[str] = None
    medication_history: Optional[str] = None
    family_medical_history: Optional[str] = None
    emergency_contact_name: Optional[str] = None
    emergency_contact_phone: Optional[str] = None

class HealthRecord(HealthRecordBase):
    id: int
    user_id: int
    physical_exam_reports: Optional[List['PhysicalExamReport']] = None
    
    class Config:
        from_attributes = True

# 体检报告相关模式
class PhysicalExamReportBase(BaseModel):
    report_name: Optional[str] = None
    exam_date: Optional[str] = None  # 改为字符串类型以匹配Android端
    hospital_name: Optional[str] = None
    doctor_comments: Optional[str] = None
    summary: Optional[str] = None
    key_findings: Optional[dict] = None
    normal_items: Optional[dict] = None
    abnormal_items: Optional[dict] = None
    recommendations: Optional[str] = None  # 改为字符串类型以匹配Android端
    report_url: Optional[str] = None
    created_time: Optional[datetime] = None
    updated_time: Optional[datetime] = None

class PhysicalExamReportCreate(PhysicalExamReportBase):
    pass

class PhysicalExamReportUpdate(BaseModel):
    report_name: Optional[str] = None
    exam_date: Optional[str] = None  # 改为字符串类型以匹配Android端
    hospital_name: Optional[str] = None
    doctor_comments: Optional[str] = None  # 使用doctor_comments而不是doctor_name以匹配Android端
    summary: Optional[str] = None
    key_findings: Optional[dict] = None
    normal_items: Optional[dict] = None
    abnormal_items: Optional[dict] = None
    recommendations: Optional[str] = None  # 添加recommendations字段

class PhysicalExamReport(PhysicalExamReportBase):
    id: int
    health_record_id: int
    
    class Config:
        from_attributes = True

# 解决循环引用
HealthRecord.update_forward_refs()

class HealthRecord(HealthRecordBase):
    id: int
    user_id: int
    created_time: datetime
    
    class Config:
        from_attributes = True

# 短信验证码相关模式
# 短信验证码请求
class SmsCodeRequest(BaseModel):
    phone: str

# 短信验证码响应
class SmsCodeResponse(BaseModel):
    phone: str
    message: str = "验证码发送成功"
    expires_in: int = 300  # 5分钟过期
    
    class Config:
        from_attributes = True

# 用户登录请求
class LoginRequest(BaseModel):
    username: str
    password: str

# 短信注册请求模式
class SmsRegisterRequest(BaseModel):
    username: str
    phone: str
    verification_code: str
    password: str

# 注册响应模式
class RegisterResponse(BaseModel):
    user_id: int
    username: str
    phone: Optional[str] = None
    email: Optional[str] = None
    full_name: Optional[str] = None
    message: str = "注册成功"
    
    class Config:
        from_attributes = True

# 分页响应模式
class PaginatedResponse(BaseModel):
    items: List[dict]
    total: int
    page: int
    size: int
    pages: int

# 医院相关模式
class HospitalBase(BaseModel):
    name: str
    address: Optional[str] = None
    phone: Optional[str] = None
    level: Optional[str] = None
    description: Optional[str] = None
    departments: Optional[str] = None
    official_account_id: Optional[str] = None
    wechat_id: Optional[str] = None

class HospitalCreate(HospitalBase):
    pass

class HospitalUpdate(BaseModel):
    name: Optional[str] = None
    address: Optional[str] = None
    phone: Optional[str] = None
    level: Optional[str] = None
    description: Optional[str] = None
    departments: Optional[str] = None
    official_account_id: Optional[str] = None
    wechat_id: Optional[str] = None

class Hospital(HospitalBase):
    id: int
    created_time: datetime
    updated_time: datetime
    
    class Config:
        from_attributes = True

# 医院列表响应
class HospitalListResponse(BaseModel):
    success: bool = True
    message: str = "获取成功"
    data: List[Hospital]
    total: int
    page: int
    size: int
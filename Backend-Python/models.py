from sqlalchemy import Column, Integer, String, Float, DateTime, Text, Boolean, ForeignKey
from sqlalchemy import Column, Integer, String, Float, Boolean, Text, ForeignKey, DateTime
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
    pharmacy_name = Column(String(200))
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
    pdf_file_path = Column(String(500))  # PDF文件路径
    file_size = Column(Integer)  # 文件大小（字节）
    publish_date = Column(DateTime)
    created_time = Column(DateTime, default=datetime.utcnow)
    updated_time = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # 关联关系
    pages = relationship("BookPage", back_populates="book")

# 图书页面模型
class BookPage(Base):
    __tablename__ = "book_pages"
    
    id = Column(Integer, primary_key=True, index=True)
    book_id = Column(Integer, ForeignKey("books.id"), nullable=False, index=True)
    page_number = Column(Integer, nullable=False, index=True)
    title = Column(String(255))
    content = Column(Text)
    image_url = Column(String(500))
    created_time = Column(DateTime, default=datetime.utcnow)
    updated_time = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # 关联关系
    book = relationship("Book", back_populates="pages")

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
    name = Column(String(100))  # 姓名
    gender = Column(String(10))  # 性别
    birthdate = Column(String(100))  # 出生日期，使用字符串类型以匹配Android模型
    height = Column(Float)  # 身高
    weight = Column(Float)  # 体重
    blood_type = Column(String(10))  # 血型
    allergies = Column(Text)  # 过敏史
    chronic_diseases = Column(Text)  # 慢性疾病
    medications = Column(Text)  # 用药情况
    family_history = Column(Text)  # 家族病史
    emergency_contact_name = Column(String(100))  # 紧急联系人姓名
    emergency_contact_phone = Column(String(20))  # 紧急联系人电话
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # 关联关系
    physical_exam_reports = relationship("PhysicalExamReport", back_populates="health_record")

# 体检报告模型
class PhysicalExamReport(Base):
    __tablename__ = "physical_exam_reports"
    
    id = Column(Integer, primary_key=True, index=True)
    health_record_id = Column(Integer, ForeignKey("health_records.id"), nullable=False)
    report_name = Column(String(200))  # 报告名称
    exam_date = Column(DateTime)  # 体检日期
    hospital_name = Column(String(200))  # 医院名称
    doctor_comments = Column(String(100))  # 医生评论
    summary = Column(Text) #总结
    key_findings = Column(Text)  # 主要发现（JSON字符串）
    normal_items = Column(Text)  # 正常项目（JSON字符串）
    abnormal_items = Column(Text)  # 异常项目（JSON字符串）
    recommendations = Column(Text) #建议
    report_url = Column(String(500)) #报告url
    created_time = Column(DateTime, default=datetime.utcnow)
    updated_time = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # 关联关系
    health_record = relationship("HealthRecord", back_populates="physical_exam_reports")

# 医院模型 - 与ai_medical.db hospitals表结构保持一致
class Hospital(Base):
    __tablename__ = "hospitals"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(200), nullable=False, index=True)  # 医院名称
    address = Column(String(500))  # 医院地址
    phone = Column(String(50))  # 联系电话
    level = Column(String(50))  # 医院等级（如：三甲）
    description = Column(Text)  # 医院描述
    departments = Column(Text)  # 可用科室ID列表，以逗号分隔存储
    official_account_id = Column(String(100))  # 公众号原始ID
    wechat_id = Column(String(100))  # 微信号
    created_time = Column(DateTime, default=datetime.utcnow)
    updated_time = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    slug = Column(String(200))  # 医院唯一标识符
    short_description = Column(Text)  # 医院简短描述
    category_id = Column(Integer)  # 医院类别ID
    department_id = Column(Integer)  # 医院科室ID
    email = Column(String(255))  # 医院邮箱
    website = Column(String(500))  # 医院网站
    rating = Column(Float, default=0)  # 医院评分
    featured_image_url = Column(String(500))  # 特色图片URL
    services_offered = Column(Text)  # 提供的服务
    tags = Column(String(500))  # 标签
    status = Column(String(50))  # 状态
    is_featured = Column(Integer, default=0)  # 是否推荐，0否1是
    is_affiliated = Column(Integer, default=0)  # 是否附属，0否1是

# 科室模型 - 与ai_medical.db departments表结构保持一致
class Department(Base):
    __tablename__ = "departments"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(200), nullable=False, index=True)  # 科室名称
    description = Column(Text)  # 科室描述
    
    # 关联关系
    doctors = relationship("Doctor", back_populates="department")

# 医生模型 - 与ai_medical.db doctors表结构保持一致
class Doctor(Base):
    __tablename__ = "doctors"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(200), nullable=False, index=True)  # 医生姓名
    title = Column(String(100))  # 职称
    department_id = Column(Integer, ForeignKey("departments.id"))  # 科室ID
    department_name = Column(String(200))  # 科室名称
    hospital_id = Column(Integer, ForeignKey("hospitals.id"))  # 医院ID
    hospital_name = Column(String(200))  # 医院名称
    specialties = Column(Text)  # 专长
    experience_years = Column(Integer)  # 工作年限
    education = Column(Text)  # 教育背景
    introduction = Column(Text)  # 医生介绍
    available_times = Column(Text)  # 出诊时间（JSON字符串）
    
    # 关联关系
    department = relationship("Department", back_populates="doctors")
    hospital = relationship("Hospital")

# 收货地址模型 - 与Address.java保持一致
class Address(Base):
    __tablename__ = "addresses"
    
    id = Column(Integer, primary_key=True, index=True)  # 地址ID
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False, index=True)  # 用户ID
    name = Column(String(100), nullable=False)  # 收件人姓名
    phone = Column(String(20), nullable=False)  # 联系电话
    province = Column(String(50), nullable=False)  # 省份
    city = Column(String(50), nullable=False)  # 城市
    district = Column(String(50), nullable=False)  # 区县
    detail_address = Column(String(500), nullable=False)  # 详细地址
    is_default = Column(Boolean, default=False)  # 是否默认地址
    latitude = Column(String(20))  # 纬度
    longitude = Column(String(20))  # 经度
    created_time = Column(DateTime, default=datetime.utcnow)
    updated_time = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # 关联关系
    user = relationship("User", foreign_keys=[user_id])

# 订单模型 - 与ai_medical.db orders表结构保持一致
class Order(Base):
    __tablename__ = "orders"
    
    id = Column(Integer, primary_key=True, index=True)
    order_id = Column(String(100), nullable=False, unique=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False, index=True)
    product_name = Column(String(255), nullable=False)
    status = Column(String(20), nullable=False, index=True)
    price = Column(String(50), nullable=False)
    create_time = Column(String(50))
    pay_time = Column(String(50))
    shipping_address = Column(Text)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # 关联关系
    user = relationship("User")

# 实名认证模型
class IdentityVerification(Base):
    __tablename__ = "identity_verifications"
    
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False, unique=True, index=True)  # 用户ID，唯一
    real_name = Column(String(100), nullable=False)  # 真实姓名
    id_card_number = Column(String(50), nullable=False)  # 身份证号（加密存储）
    status = Column(String(20), default="pending", nullable=False)  # 认证状态：pending, verified, rejected
    verification_time = Column(DateTime)  # 认证时间
    created_at = Column(DateTime, default=datetime.utcnow)  # 创建时间
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)  # 更新时间
    
    # 关联关系
    user = relationship("User")
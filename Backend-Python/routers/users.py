from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.responses import JSONResponse
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from sqlalchemy.orm import Session
from passlib.context import CryptContext
from jose import JWTError, jwt
from datetime import datetime, timedelta
from typing import Optional
from database import get_db
from models import User as UserModel, HealthRecord as HealthRecordModel
from schemas import User, UserCreate, UserUpdate, HealthRecord, HealthRecordCreate, SmsCodeResponse, SmsRegisterRequest, RegisterResponse, SmsCodeRequest, LoginRequest
import os
import base64

router = APIRouter()

# 密码加密配置
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

def generate_jwt_secret():
    # 生成 32 字节的随机数据
    random_bytes = os.urandom(32)
    # 转换为 base64 字符串
    secret_key = base64.b64encode(random_bytes).decode('utf-8')
    return secret_key

# JWT配置
SECRET_KEY = "a5gV+Zsx0P27rOou8YMRJqDWxwq/51b5hsgIntCXZueZa66B/Qx5u3pSmlpC5BLUCT6gxx5Pf3nhOkVxdo7OMA=="  # 在生产环境中应该使用环境变量
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60*24

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/api/v1/users/token")

# 密码工具函数
def verify_password(plain_password, hashed_password):
    return pwd_context.verify(plain_password, hashed_password)

def get_password_hash(password):
    return pwd_context.hash(password)

# JWT工具函数
def create_access_token(data: dict, expires_delta: Optional[timedelta] = None):
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=15)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

# 获取当前用户
async def get_current_user(token: str = Depends(oauth2_scheme), db: Session = Depends(get_db)):
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        username: str = payload.get("sub")
        if username is None:
            raise credentials_exception
    except JWTError:
        raise credentials_exception
    
    user = db.query(UserModel).filter(UserModel.username == username).first()
    if user is None:
        raise credentials_exception
    return user

# 存储验证码的临时字典（生产环境应使用Redis）
verification_codes = {}

# 发送短信验证码
@router.post("/send-sms-code")
async def send_sms_code(request: SmsCodeRequest, db: Session = Depends(get_db)):
    """发送短信验证码"""
    import random
    import time
    
    phone = request.phone
    # 检查手机号格式
    if not phone or len(phone) != 11 or not phone.startswith('1'):
        raise HTTPException(status_code=400, detail="手机号格式不正确")
    
    # 生成6位验证码
    verification_code = str(random.randint(100000, 999999))
    
    # 存储验证码（5分钟过期）
    verification_codes[phone] = {
        "code": verification_code,
        "expires_at": time.time() + 300  # 5分钟后过期
    }
    
    # 模拟发送短信（实际项目中应调用短信服务商API）
    print(f"发送验证码到 {phone}: {verification_code}")
    
    return {
        "success": True,
        "message": "验证码发送成功",
        "data": {
            "phone": phone,
            "message": "验证码发送成功",
            "expires_in": 300
        }
    }

# 短信注册
@router.post("/register-with-sms")
async def register_with_sms(request: SmsRegisterRequest, db: Session = Depends(get_db)):
    """短信验证码注册"""
    import time
    
    # 验证验证码
    if request.phone not in verification_codes:
        raise HTTPException(status_code=400, detail="验证码不存在或已过期")
    
    stored_code_info = verification_codes[request.phone]
    if time.time() > stored_code_info["expires_at"]:
        del verification_codes[request.phone]
        raise HTTPException(status_code=400, detail="验证码已过期")
    
    if stored_code_info["code"] != request.verification_code:
        raise HTTPException(status_code=400, detail="验证码错误")
    
    # 检查用户名是否已存在
    db_user = db.query(UserModel).filter(UserModel.username == request.username).first()
    if db_user:
        raise HTTPException(status_code=400, detail="用户名已存在")
    
    # 检查手机号是否已存在
    db_user = db.query(UserModel).filter(UserModel.phone == request.phone).first()
    if db_user:
        raise HTTPException(status_code=400, detail="手机号已被注册")
    
    # 创建新用户
    hashed_password = get_password_hash(request.password)
    db_user = UserModel(
        username=request.username,
        email=None,  # 短信注册可以不填邮箱
        full_name=None,
        phone=request.phone,
        avatar_url=None,
        hashed_password=hashed_password
    )
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    
    # 删除已使用的验证码
    del verification_codes[request.phone]
    
    # 使用JSONResponse确保返回正确的格式
    from fastapi.responses import JSONResponse
    response_data = {
        "success": True,
        "message": "注册成功",
        "data": {
            "user_id": int(db_user.id),
            "username": str(db_user.username),
            "phone": str(db_user.phone) if db_user.phone else None,
            "email": str(db_user.email) if db_user.email else None,
            "full_name": str(db_user.full_name) if db_user.full_name else None
        }
    }
    return JSONResponse(content=response_data, status_code=200, headers={"Content-Type": "application/json"})

# 用户注册（邮箱注册）
@router.post("/register")
async def register_user(user: UserCreate, db: Session = Depends(get_db)):
    """用户注册"""
    # 检查用户名是否已存在
    db_user = db.query(UserModel).filter(UserModel.username == user.username).first()
    if db_user:
        raise HTTPException(status_code=400, detail="用户名已存在")
    
    # 检查邮箱是否已存在
    if user.email:
        db_user = db.query(UserModel).filter(UserModel.email == user.email).first()
        if db_user:
            raise HTTPException(status_code=400, detail="邮箱已存在")
    
    # 创建新用户
    hashed_password = get_password_hash(user.password)
    db_user = UserModel(
        username=user.username,
        email=user.email,
        full_name=user.full_name,
        phone=user.phone,
        avatar_url=user.avatar_url,
        hashed_password=hashed_password
    )
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    
    # 保存用户信息到变量，避免直接引用数据库对象
    user_id = db_user.id
    username = db_user.username
    phone = db_user.phone
    email = db_user.email
    full_name = db_user.full_name
    
    # 使用JSONResponse确保返回正确的格式
    from fastapi.responses import JSONResponse
    response_data = {
        "success": True,
        "message": "注册成功",
        "data": {
            "user_id": user_id,
            "username": username,
            "phone": phone,
            "email": email,
            "full_name": full_name
        }
    }
    return JSONResponse(content=response_data, status_code=200, headers={"Content-Type": "application/json"})

# 测试端点
@router.get("/debug/test-json-response")
async def test_json_response():
    """测试JSON响应格式"""
    from fastapi.responses import JSONResponse
    response_data = {
        "success": True,
        "message": "测试JSON响应成功",
        "data": {
            "user_id": 999,
            "username": "testuser",
            "email": "test@example.com"
        }
    }
    return JSONResponse(content=response_data, status_code=200)

# 用户登录
@router.post("/token")
async def login_for_access_token(form_data: OAuth2PasswordRequestForm = Depends(), db: Session = Depends(get_db)):
    """用户登录获取访问令牌"""
    user = db.query(UserModel).filter(UserModel.username == form_data.username).first()
    if not user or not verify_password(form_data.password, user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="用户名或密码错误",
            headers={"WWW-Authenticate": "Bearer"},
        )
    access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = create_access_token(
        data={"sub": user.username}, expires_delta=access_token_expires
    )
    return {"access_token": access_token, "token_type": "bearer"}

# 简单用户登录接口（用于Android客户端）
@router.post("/login")
async def login_user(request: LoginRequest, db: Session = Depends(get_db)):
    """用户登录验证"""
    username = request.username
    password = request.password
    
    # 查找用户
    user = db.query(UserModel).filter(UserModel.username == username).first()
    print(user)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="用户名不存在"
        )

    # 验证密码
    if not verify_password(password, user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="密码错误"
        )
    
    # 检查用户是否激活
    if not user.is_active:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="用户账号已被禁用"
        )
    
    # 生成访问令牌
    access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = create_access_token(
        data={"sub": user.username}, expires_delta=access_token_expires
    )
    
    return {
        "success": True,
        "message": "登录成功",
        "data": {
            "user_id": user.id,
            "username": user.username,
            "email": user.email,
            "full_name": user.full_name,
            "phone": user.phone,
            "avatar_url": user.avatar_url,
            "access_token": access_token,
            "token_type": "bearer"
        }
    }

# 获取当前用户信息
@router.get("/me", response_model=User)
async def read_users_me(current_user: UserModel = Depends(get_current_user)):
    """获取当前用户信息"""
    return current_user

# 更新用户信息
@router.put("/me", response_model=User)
async def update_user_me(
    user_update: UserUpdate,
    current_user: UserModel = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """更新当前用户信息"""
    update_data = user_update.dict(exclude_unset=True)
    
    # 如果更新密码，需要加密
    if "password" in update_data:
        update_data["hashed_password"] = get_password_hash(update_data.pop("password"))
    
    # 检查用户名和邮箱唯一性
    if "username" in update_data and update_data["username"] != current_user.username:
        existing_user = db.query(UserModel).filter(
            UserModel.username == update_data["username"],
            UserModel.id != current_user.id
        ).first()
        if existing_user:
            raise HTTPException(status_code=400, detail="用户名已存在")
    
    if "email" in update_data and update_data["email"] != current_user.email:
        existing_user = db.query(UserModel).filter(
            UserModel.email == update_data["email"],
            UserModel.id != current_user.id
        ).first()
        if existing_user:
            raise HTTPException(status_code=400, detail="邮箱已存在")
    
    # 更新用户信息
    for field, value in update_data.items():
        setattr(current_user, field, value)
    
    db.commit()
    db.refresh(current_user)
    return current_user

# 测试注册端点
@router.post("/test-register-new")
async def test_register_new():
    """测试注册响应格式"""
    from fastapi.responses import JSONResponse
    response_data = {
        "success": True,
        "message": "测试注册成功",
        "data": {
            "user_id": 999,
            "username": "test_user",
            "phone": "13800138000",
            "email": "test@test.com",
            "full_name": "测试用户"
        }
    }
    return JSONResponse(content=response_data, status_code=200, headers={"Content-Type": "application/json"})

# 获取用户详情（管理员功能）
@router.get("/{user_id}", response_model=User)
async def get_user(user_id: int, db: Session = Depends(get_db)):
    """获取用户详情"""
    user = db.query(UserModel).filter(UserModel.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="用户不存在")
    return user

# 创建健康档案记录
@router.post("/me/health-records", response_model=HealthRecord)
async def create_health_record(
    health_record: HealthRecordCreate,
    current_user: UserModel = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """创建健康档案记录"""
    db_record = HealthRecordModel(
        user_id=current_user.id,
        **health_record.dict()
    )
    db.add(db_record)
    db.commit()
    db.refresh(db_record)
    return db_record

# 获取用户健康档案
@router.get("/me/health-records")
async def get_my_health_records(
    current_user: UserModel = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """获取当前用户的健康档案"""
    records = db.query(HealthRecordModel).filter(
        HealthRecordModel.user_id == current_user.id
    ).order_by(HealthRecordModel.recorded_date.desc()).all()
    
    return {"health_records": records}

# 获取用户统计信息
@router.get("/me/stats")
async def get_user_stats(
    current_user: UserModel = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """获取用户统计信息"""
    from models import Appointment as AppointmentModel, Prescription as PrescriptionModel
    
    # 统计预约数量
    total_appointments = db.query(AppointmentModel).filter(
        AppointmentModel.user_id == current_user.id
    ).count()
    
    pending_appointments = db.query(AppointmentModel).filter(
        AppointmentModel.user_id == current_user.id,
        AppointmentModel.status == "pending"
    ).count()
    
    # 统计处方数量
    total_prescriptions = db.query(PrescriptionModel).filter(
        PrescriptionModel.user_id == current_user.id
    ).count()
    
    # 统计健康记录数量
    total_health_records = db.query(HealthRecordModel).filter(
        HealthRecordModel.user_id == current_user.id
    ).count()
    
    return {
        "user_id": current_user.id,
        "username": current_user.username,
        "stats": {
            "total_appointments": total_appointments,
            "pending_appointments": pending_appointments,
            "total_prescriptions": total_prescriptions,
            "total_health_records": total_health_records
        }
    }

# 删除健康记录
@router.delete("/me/health-records/{record_id}")
async def delete_health_record(
    record_id: int,
    current_user: UserModel = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """删除健康记录"""
    record = db.query(HealthRecordModel).filter(
        HealthRecordModel.id == record_id,
        HealthRecordModel.user_id == current_user.id
    ).first()
    
    if not record:
        raise HTTPException(status_code=404, detail="健康记录不存在")
    
    db.delete(record)
    db.commit()
    return {"message": "健康记录删除成功"}

# 修改密码
@router.post("/me/change-password")
async def change_password(
    old_password: str,
    new_password: str,
    current_user: UserModel = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """修改密码"""
    # 验证旧密码
    if not verify_password(old_password, current_user.hashed_password):
        raise HTTPException(status_code=400, detail="原密码错误")
    
    # 更新密码
    current_user.hashed_password = get_password_hash(new_password)
    db.commit()
    
    return {"message": "密码修改成功"}
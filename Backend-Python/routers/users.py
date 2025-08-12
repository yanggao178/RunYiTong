from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from sqlalchemy.orm import Session
from passlib.context import CryptContext
from jose import JWTError, jwt
from datetime import datetime, timedelta
from typing import Optional
from database import get_db
from models import User as UserModel, HealthRecord as HealthRecordModel
from schemas import User, UserCreate, UserUpdate, HealthRecord, HealthRecordCreate

router = APIRouter()

# 密码加密配置
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# JWT配置
SECRET_KEY = "your-secret-key-here"  # 在生产环境中应该使用环境变量
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30

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

# 用户注册
@router.post("/register", response_model=User)
async def register_user(user: UserCreate, db: Session = Depends(get_db)):
    """用户注册"""
    # 检查用户名是否已存在
    db_user = db.query(UserModel).filter(UserModel.username == user.username).first()
    if db_user:
        raise HTTPException(status_code=400, detail="用户名已存在")
    
    # 检查邮箱是否已存在
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
    return db_user

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
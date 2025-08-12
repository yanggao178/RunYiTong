from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List, Optional
from datetime import datetime, timedelta
from database import get_db
from models import Appointment as AppointmentModel
from schemas import Appointment, AppointmentCreate, AppointmentUpdate, PaginatedResponse

router = APIRouter()

# 获取预约列表（支持分页和搜索）
@router.get("/", response_model=PaginatedResponse)
async def get_appointments(
    page: int = Query(1, ge=1, description="页码"),
    size: int = Query(10, ge=1, le=100, description="每页数量"),
    user_id: Optional[int] = Query(None, description="用户ID"),
    status: Optional[str] = Query(None, description="预约状态"),
    department: Optional[str] = Query(None, description="科室"),
    db: Session = Depends(get_db)
):
    """获取预约列表"""
    query = db.query(AppointmentModel)
    
    # 用户过滤
    if user_id:
        query = query.filter(AppointmentModel.user_id == user_id)
    
    # 状态过滤
    if status:
        query = query.filter(AppointmentModel.status == status)
    
    # 科室过滤
    if department:
        query = query.filter(AppointmentModel.department == department)
    
    # 按预约时间排序
    query = query.order_by(AppointmentModel.appointment_date.desc())
    
    # 计算总数
    total = query.count()
    
    # 分页
    offset = (page - 1) * size
    appointments = query.offset(offset).limit(size).all()
    
    # 计算总页数
    pages = (total + size - 1) // size
    
    return PaginatedResponse(
        items=[Appointment.from_orm(appointment).dict() for appointment in appointments],
        total=total,
        page=page,
        size=size,
        pages=pages
    )

# 获取单个预约详情
@router.get("/{appointment_id}", response_model=Appointment)
async def get_appointment(appointment_id: int, db: Session = Depends(get_db)):
    """获取预约详情"""
    appointment = db.query(AppointmentModel).filter(
        AppointmentModel.id == appointment_id
    ).first()
    if not appointment:
        raise HTTPException(status_code=404, detail="预约不存在")
    return appointment

# 创建预约
@router.post("/", response_model=Appointment)
async def create_appointment(appointment: AppointmentCreate, db: Session = Depends(get_db)):
    """创建预约"""
    # 检查预约时间是否在未来
    if appointment.appointment_date <= datetime.now():
        raise HTTPException(status_code=400, detail="预约时间必须在未来")
    
    # 检查是否有冲突的预约（同一用户同一时间段）
    existing_appointment = db.query(AppointmentModel).filter(
        AppointmentModel.user_id == appointment.user_id,
        AppointmentModel.appointment_date == appointment.appointment_date,
        AppointmentModel.appointment_time == appointment.appointment_time,
        AppointmentModel.status.in_(["pending", "confirmed"])
    ).first()
    
    if existing_appointment:
        raise HTTPException(status_code=400, detail="该时间段已有预约")
    
    db_appointment = AppointmentModel(**appointment.dict())
    db.add(db_appointment)
    db.commit()
    db.refresh(db_appointment)
    return db_appointment

# 更新预约
@router.put("/{appointment_id}", response_model=Appointment)
async def update_appointment(
    appointment_id: int, 
    appointment_update: AppointmentUpdate, 
    db: Session = Depends(get_db)
):
    """更新预约"""
    db_appointment = db.query(AppointmentModel).filter(
        AppointmentModel.id == appointment_id
    ).first()
    if not db_appointment:
        raise HTTPException(status_code=404, detail="预约不存在")
    
    # 更新字段
    update_data = appointment_update.dict(exclude_unset=True)
    for field, value in update_data.items():
        setattr(db_appointment, field, value)
    
    db.commit()
    db.refresh(db_appointment)
    return db_appointment

# 取消预约
@router.patch("/{appointment_id}/cancel")
async def cancel_appointment(appointment_id: int, db: Session = Depends(get_db)):
    """取消预约"""
    db_appointment = db.query(AppointmentModel).filter(
        AppointmentModel.id == appointment_id
    ).first()
    if not db_appointment:
        raise HTTPException(status_code=404, detail="预约不存在")
    
    if db_appointment.status == "cancelled":
        raise HTTPException(status_code=400, detail="预约已经取消")
    
    if db_appointment.status == "completed":
        raise HTTPException(status_code=400, detail="已完成的预约无法取消")
    
    db_appointment.status = "cancelled"
    db.commit()
    db.refresh(db_appointment)
    
    return {"message": "预约取消成功"}

# 确认预约
@router.patch("/{appointment_id}/confirm")
async def confirm_appointment(appointment_id: int, db: Session = Depends(get_db)):
    """确认预约"""
    db_appointment = db.query(AppointmentModel).filter(
        AppointmentModel.id == appointment_id
    ).first()
    if not db_appointment:
        raise HTTPException(status_code=404, detail="预约不存在")
    
    if db_appointment.status != "pending":
        raise HTTPException(status_code=400, detail="只能确认待处理的预约")
    
    db_appointment.status = "confirmed"
    db.commit()
    db.refresh(db_appointment)
    
    return {"message": "预约确认成功"}

# 完成预约
@router.patch("/{appointment_id}/complete")
async def complete_appointment(appointment_id: int, db: Session = Depends(get_db)):
    """完成预约"""
    db_appointment = db.query(AppointmentModel).filter(
        AppointmentModel.id == appointment_id
    ).first()
    if not db_appointment:
        raise HTTPException(status_code=404, detail="预约不存在")
    
    if db_appointment.status != "confirmed":
        raise HTTPException(status_code=400, detail="只能完成已确认的预约")
    
    db_appointment.status = "completed"
    db.commit()
    db.refresh(db_appointment)
    
    return {"message": "预约完成"}

# 获取可用的预约时间段
@router.get("/available-slots/{department}")
async def get_available_slots(
    department: str,
    date: str = Query(..., description="日期，格式：YYYY-MM-DD"),
    db: Session = Depends(get_db)
):
    """获取指定科室和日期的可用时间段"""
    try:
        appointment_date = datetime.strptime(date, "%Y-%m-%d").date()
    except ValueError:
        raise HTTPException(status_code=400, detail="日期格式错误，应为YYYY-MM-DD")
    
    # 检查日期是否在未来
    if appointment_date <= datetime.now().date():
        raise HTTPException(status_code=400, detail="只能查询未来日期的时间段")
    
    # 定义时间段
    time_slots = [
        "08:00-08:30", "08:30-09:00", "09:00-09:30", "09:30-10:00",
        "10:00-10:30", "10:30-11:00", "11:00-11:30", "11:30-12:00",
        "14:00-14:30", "14:30-15:00", "15:00-15:30", "15:30-16:00",
        "16:00-16:30", "16:30-17:00", "17:00-17:30", "17:30-18:00"
    ]
    
    # 查询已预约的时间段
    booked_slots = db.query(AppointmentModel.appointment_time).filter(
        AppointmentModel.department == department,
        AppointmentModel.appointment_date == appointment_date,
        AppointmentModel.status.in_(["pending", "confirmed"])
    ).all()
    
    booked_times = [slot[0] for slot in booked_slots if slot[0]]
    
    # 返回可用时间段
    available_slots = [slot for slot in time_slots if slot not in booked_times]
    
    return {
        "date": date,
        "department": department,
        "available_slots": available_slots,
        "total_slots": len(time_slots),
        "available_count": len(available_slots)
    }

# 获取科室列表
@router.get("/departments/list")
async def get_departments():
    """获取所有科室列表"""
    departments = [
        "内科", "外科", "儿科", "妇产科", "眼科", "耳鼻喉科",
        "皮肤科", "神经科", "心理科", "中医科", "康复科", "急诊科"
    ]
    return {"departments": departments}

# 获取用户的预约历史
@router.get("/user/{user_id}/history", response_model=List[Appointment])
async def get_user_appointment_history(
    user_id: int, 
    limit: int = Query(10, ge=1, le=50, description="返回数量限制"),
    db: Session = Depends(get_db)
):
    """获取用户的预约历史"""
    appointments = db.query(AppointmentModel).filter(
        AppointmentModel.user_id == user_id
    ).order_by(
        AppointmentModel.appointment_date.desc()
    ).limit(limit).all()
    
    return appointments

# 获取今日预约
@router.get("/today/list", response_model=List[Appointment])
async def get_today_appointments(
    department: Optional[str] = Query(None, description="科室过滤"),
    db: Session = Depends(get_db)
):
    """获取今日预约列表"""
    today = datetime.now().date()
    query = db.query(AppointmentModel).filter(
        AppointmentModel.appointment_date == today,
        AppointmentModel.status.in_(["confirmed", "pending"])
    )
    
    if department:
        query = query.filter(AppointmentModel.department == department)
    
    appointments = query.order_by(AppointmentModel.appointment_time).all()
    return appointments
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List, Optional
from datetime import datetime, timedelta
from database import get_db
from models import Appointment as AppointmentModel, Hospital, Department, Doctor
from schemas import Appointment, AppointmentCreate, AppointmentUpdate, PaginatedResponse, Hospital as HospitalSchema
from fastapi import APIRouter, Depends, HTTPException, Query

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

# 获取医院列表
@router.get("/hospitals")
async def get_hospitals(
    db: Session = Depends(get_db),
    name: Optional[str] = Query(None, description="医院名称搜索")
):
    """从数据库获取医院列表"""
    query = db.query(Hospital)
    
    # 如果提供了名称参数，进行模糊搜索
    if name:
        query = query.filter(Hospital.name.contains(name))
    
    # 按创建时间降序排序
    query = query.order_by(Hospital.created_time.desc())
    
    # 获取所有医院
    hospitals = query.all()
    
    # 序列化医院数据，不包含科室信息
    serialized_hospitals = []
    for hospital in hospitals:
        # 转换为字典并移除departments字段
        hospital_dict = HospitalSchema.from_orm(hospital).dict()
        # 删除departments字段
        if 'departments' in hospital_dict:
            del hospital_dict['departments']
        
        serialized_hospitals.append(hospital_dict)
    
    # 返回符合客户端期望格式的响应
    return {
        "success": True,
        "message": "获取医院列表成功",
        "data": {"hospitals": serialized_hospitals}
    }

# 获取指定医院的科室列表
@router.get("/hospitals/{hospital_id}/departments")
async def get_hospital_departments(
    hospital_id: int, 
    db: Session = Depends(get_db)
):
    """获取指定医院的科室列表"""
    # 1. 查询指定的医院
    hospital = db.query(Hospital).filter(Hospital.id == hospital_id).first()
    
    if not hospital:
        raise HTTPException(status_code=404, detail="医院不存在")
    
    # 2. 获取医院关联的科室ID列表
    department_ids = []
    if hospital.departments:
        # 同时支持全角逗号和半角逗号的分割
        # 先将所有全角逗号替换为半角逗号
        departments_str = hospital.departments.replace('，', ',')
        # 分割半角逗号并转换为整数
        department_ids = [int(dept_id.strip()) for dept_id in departments_str.split(',') if dept_id.strip().isdigit()]
    
    # 3. 查询departments表获取科室详情
    departments = []
    if department_ids:
        departments_query = db.query(Department).filter(Department.id.in_(department_ids)).order_by(Department.id)
        departments = [
            {
                "id": dept.id,
                "name": dept.name,
                "description": dept.description
            }
            for dept in departments_query.all()
        ]
    
    # 4. 返回符合客户端期望格式的响应
    return {
        "success": True,
        "message": "获取科室列表成功",
        "data": {
            "departments": departments,
            "hospital_id": hospital_id,
            "hospital_name": hospital.name
        }
    }

# 获取医生列表
@router.get("/doctors")
async def get_doctors(
    department_id: Optional[int] = Query(None, description="科室ID"),
    hospital_id: Optional[int] = Query(None, description="医院ID"),
    db: Session = Depends(get_db)
):
    """获取医生列表"""
    # 从数据库中查询医生
    query = db.query(Doctor)
    
    # 根据参数过滤
    if hospital_id is not None:
        query = query.filter(Doctor.hospital_id == hospital_id)
    if department_id is not None:
        query = query.filter(Doctor.department_id == department_id)
    
    # 执行查询
    doctors = query.all()
    
    # 处理查询结果，将数据库模型转换为前端需要的格式
    filtered_doctors = []
    for doctor in doctors:
        # 处理specialties字段，从文本转换为列表
        specialties = []
        if doctor.specialties:
            # 尝试按逗号分隔
            specialties = [s.strip() for s in doctor.specialties.split(',') if s.strip()]
        
        # 处理available_times字段，从文本转换为列表
        available_times = []
        if doctor.available_times:
            # 尝试按逗号分隔
            available_times = [t.strip() for t in doctor.available_times.split(',') if t.strip()]
        
        # 添加到结果列表
        filtered_doctors.append({
            "id": doctor.id,
            "name": doctor.name,
            "title": doctor.title,
            "department_id": doctor.department_id,
            "department_name": doctor.department_name,
            "hospital_id": doctor.hospital_id,
            "hospital_name": doctor.hospital_name,
            "specialties": specialties,
            "experience_years": doctor.experience_years,
            "education": doctor.education,
            "introduction": doctor.introduction,
            "available_times": available_times
        })
    
    return {
        "success": True,
        "message": "获取医生列表成功",
        "data": {"doctors": filtered_doctors}
    }

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

# 获取指定医院的科室列表
@router.get("/hospitals/{hospital_id}/departments")
async def get_hospital_departments(hospital_id: int):
    """获取指定医院的科室列表"""
    # 医院科室映射
    hospital_departments = {
        1: [1, 2, 3, 4],  # 北京协和医院
        2: [1, 2, 4],     # 北京大学第一医院
        3: [1, 3, 4],     # 广安门医院
        4: [4]            # 儿童医院
    }
    
    all_departments = [
        {"id": 1, "name": "内科", "description": "内科疾病诊治"},
        {"id": 2, "name": "外科", "description": "外科手术治疗"},
        {"id": 3, "name": "中医科", "description": "中医诊疗"},
        {"id": 4, "name": "儿科", "description": "儿童疾病诊治"},
        {"id": 5, "name": "眼科", "description": "眼科疾病诊治"},
        {"id": 6, "name": "耳鼻喉科", "description": "耳鼻喉疾病诊治"},
        {"id": 7, "name": "皮肤科", "description": "皮肤疾病诊治"},
        {"id": 8, "name": "神经科", "description": "神经系统疾病诊治"}
    ]
    
    if hospital_id not in hospital_departments:
        raise HTTPException(status_code=404, detail="医院不存在")
    
    available_dept_ids = hospital_departments[hospital_id]
    available_departments = [d for d in all_departments if d["id"] in available_dept_ids]
    
    return {
        "success": True,
        "message": "获取医院科室列表成功",
        "data": {"items": available_departments}
    }

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
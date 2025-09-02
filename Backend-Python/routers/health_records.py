from datetime import datetime

from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from typing import List, Optional
from database import get_db
from models import HealthRecord as HealthRecordModel, PhysicalExamReport as PhysicalExamReportModel, User
from schemas import HealthRecord, HealthRecordCreate, HealthRecordUpdate, PhysicalExamReport, PhysicalExamReportCreate, PhysicalExamReportUpdate, BaseResponse
from pydantic import ValidationError
from routers.users import get_current_user

router = APIRouter()

# 根据用户ID获取健康档案（兼容移动端API）
@router.get("/{user_id}", response_model=BaseResponse)
async def get_health_record_by_user_id(
    user_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """根据用户ID获取健康档案（兼容移动端API）"""
    # 确保当前用户只能访问自己的健康档案
    if user_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="无权访问其他用户的健康档案"
        )
    
    health_record = db.query(HealthRecordModel).filter(
        HealthRecordModel.user_id == user_id
    ).first()
    
    if not health_record:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="健康档案不存在"
        )
    
    # 转换为字典格式以便返回
    health_record_dict = health_record.__dict__
    health_record_dict.pop('_sa_instance_state', None)
    
    # 获取关联的体检报告
    physical_exams = db.query(PhysicalExamReportModel).filter(
        PhysicalExamReportModel.health_record_id == health_record.id
    ).all()
    
    physical_exams_data = []
    for exam in physical_exams:
        exam_dict = exam.__dict__
        exam_dict.pop('_sa_instance_state', None)
        # 将JSON字段从字符串转换为字典
        exam_dict['key_findings'] = eval(exam_dict['key_findings']) if exam_dict['key_findings'] else {}
        exam_dict['normal_items'] = eval(exam_dict['normal_items']) if exam_dict['normal_items'] else {}
        exam_dict['abnormal_items'] = eval(exam_dict['abnormal_items']) if exam_dict['abnormal_items'] else {}
        physical_exams_data.append(exam_dict)
    
    health_record_dict['physical_exam_reports'] = physical_exams_data
    
    return {
        "success": True,
        "message": "获取健康档案成功",
        "data": health_record_dict
    }

# 获取当前用户的健康档案
@router.get("/", response_model=BaseResponse)
async def get_health_records(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """获取当前用户的健康档案"""
    health_records = db.query(HealthRecordModel).filter(HealthRecordModel.user_id == current_user.id).all()
    
    # 转换为字典格式以便返回
    health_records_data = []
    for record in health_records:
        health_record_dict = record.__dict__
        # 删除SQLAlchemy相关的属性
        health_record_dict.pop('_sa_instance_state', None)
        
        # 获取关联的体检报告
        physical_exams = db.query(PhysicalExamReportModel).filter(
            PhysicalExamReportModel.health_record_id == record.id
        ).all()
        
        physical_exams_data = []
        for exam in physical_exams:
            exam_dict = exam.__dict__
            exam_dict.pop('_sa_instance_state', None)
            # 将JSON字段从字符串转换为字典
            exam_dict['key_findings'] = eval(exam_dict['key_findings']) if exam_dict['key_findings'] else {}
            exam_dict['normal_items'] = eval(exam_dict['normal_items']) if exam_dict['normal_items'] else {}
            exam_dict['abnormal_items'] = eval(exam_dict['abnormal_items']) if exam_dict['abnormal_items'] else {}
            physical_exams_data.append(exam_dict)
        
        health_record_dict['physical_exam_reports'] = physical_exams_data
        health_records_data.append(health_record_dict)
    
    return {
        "success": True,
        "message": "获取健康档案成功",
        "data": {"health_records": health_records_data}
    }

# 获取单个健康档案
@router.get("/{record_id}", response_model=BaseResponse)
async def get_health_record(
    record_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """获取单个健康档案"""
    health_record = db.query(HealthRecordModel).filter(
        HealthRecordModel.id == record_id, 
        HealthRecordModel.user_id == current_user.id
    ).first()
    
    if not health_record:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="健康档案不存在"
        )
    
    # 转换为字典格式以便返回
    health_record_dict = health_record.__dict__
    health_record_dict.pop('_sa_instance_state', None)
    
    # 获取关联的体检报告
    physical_exams = db.query(PhysicalExamReportModel).filter(
        PhysicalExamReportModel.health_record_id == record_id
    ).all()
    
    physical_exams_data = []
    for exam in physical_exams:
        exam_dict = exam.__dict__
        exam_dict.pop('_sa_instance_state', None)
        # 将JSON字段从字符串转换为字典
        exam_dict['key_findings'] = eval(exam_dict['key_findings']) if exam_dict['key_findings'] else {}
        exam_dict['normal_items'] = eval(exam_dict['normal_items']) if exam_dict['normal_items'] else {}
        exam_dict['abnormal_items'] = eval(exam_dict['abnormal_items']) if exam_dict['abnormal_items'] else {}
        physical_exams_data.append(exam_dict)
    
    health_record_dict['physical_exam_reports'] = physical_exams_data
    
    return {
        "success": True,
        "message": "获取健康档案成功",
        "data": {"health_record": health_record_dict}
    }

# 创建健康档案
@router.post("/", response_model=BaseResponse)
async def create_health_record(
    health_record: HealthRecordCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """创建健康档案"""
    # 检查用户是否已经有健康档案
    existing_record = db.query(HealthRecordModel).filter(
        HealthRecordModel.user_id == current_user.id
    ).first()
    
    if existing_record:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="您已经有健康档案，请更新现有档案"
        )
    
    db_health_record = HealthRecordModel(
        user_id=current_user.id,
        name=health_record.name,
        gender=health_record.gender,
        birthdate=health_record.birthdate,
        height=health_record.height,
        weight=health_record.weight,
        blood_type=health_record.blood_type,
        allergy_history=health_record.allergy_history,
        medication_history=health_record.medication_history,
        family_medical_history=health_record.family_medical_history,
        created_time=health_record.created_time,
        updated_time=health_record.updated_time
    )
    
    db.add(db_health_record)
    db.commit()
    db.refresh(db_health_record)
    
    # 转换为字典格式以便返回
    health_record_dict = db_health_record.__dict__
    health_record_dict.pop('_sa_instance_state', None)
    health_record_dict['physical_exam_reports'] = []
    
    return {
        "success": True,
        "message": "创建健康档案成功",
        "data": {"health_record": health_record_dict}
    }

# 更新健康档案 - 兼容客户端ApiService接口
@router.put("/{user_id}", response_model=BaseResponse)
async def update_health_record(
    user_id: int,
    health_record: HealthRecordUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """更新健康档案 - 兼容客户端ApiService接口"""
    # 验证用户身份
    if current_user.id != user_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="无权访问此用户的健康档案"
        )
    
    # 查询用户的健康档案
    db_health_record = db.query(HealthRecordModel).filter(
        HealthRecordModel.user_id == user_id
    ).first()
    
    if not db_health_record:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="健康档案不存在"
        )
    
    # 更新字段
    for field, value in health_record.dict(exclude_unset=True).items():
        setattr(db_health_record, field, value)
    
    # 更新时间
    from datetime import datetime
    db_health_record.updated_time = datetime.utcnow()
    
    # 将健康档案参数更新后保存到数据库
    db.commit()
    db.refresh(db_health_record)  # 刷新数据库对象，确保获取最新状态
    
    # 转换为字典格式以便返回
    health_record_dict = db_health_record.__dict__
    health_record_dict.pop('_sa_instance_state', None)
    
    # 获取关联的体检报告
    physical_exams = db.query(PhysicalExamReportModel).filter(
        PhysicalExamReportModel.health_record_id == db_health_record.id
    ).all()
    
    physical_exams_data = []
    for exam in physical_exams:
        exam_dict = exam.__dict__
        exam_dict.pop('_sa_instance_state', None)
        # 将JSON字段从字符串转换为字典
        exam_dict['key_findings'] = eval(exam_dict['key_findings']) if exam_dict['key_findings'] else {}
        exam_dict['normal_items'] = eval(exam_dict['normal_items']) if exam_dict['normal_items'] else {}
        exam_dict['abnormal_items'] = eval(exam_dict['abnormal_items']) if exam_dict['abnormal_items'] else {}
        physical_exams_data.append(exam_dict)
    
    health_record_dict['physical_exam_reports'] = physical_exams_data
    
    return {
        "success": True,
        "message": "更新健康档案成功",
        "data": {"health_record": health_record_dict}
    }

# 更新健康档案（原接口 - 保留用于向后兼容）
@router.put("/{record_id}", response_model=BaseResponse)
async def update_health_record_legacy(
    record_id: int,
    health_record: HealthRecordUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """更新健康档案（原接口 - 保留用于向后兼容）"""
    db_health_record = db.query(HealthRecordModel).filter(
        HealthRecordModel.id == record_id, 
        HealthRecordModel.user_id == current_user.id
    ).first()
    
    if not db_health_record:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="健康档案不存在"
        )
    
    # 更新字段
    for field, value in health_record.dict(exclude_unset=True).items():
        setattr(db_health_record, field, value)
    
    # 更新时间
    from datetime import datetime
    db_health_record.updated_time = datetime.utcnow()
    
    db.commit()
    db.refresh(db_health_record)
    
    # 转换为字典格式以便返回
    health_record_dict = db_health_record.__dict__
    health_record_dict.pop('_sa_instance_state', None)
    
    # 获取关联的体检报告
    physical_exams = db.query(PhysicalExamReportModel).filter(
        PhysicalExamReportModel.health_record_id == record_id
    ).all()
    
    physical_exams_data = []
    for exam in physical_exams:
        exam_dict = exam.__dict__
        exam_dict.pop('_sa_instance_state', None)
        # 将JSON字段从字符串转换为字典
        exam_dict['key_findings'] = eval(exam_dict['key_findings']) if exam_dict['key_findings'] else {}
        exam_dict['normal_items'] = eval(exam_dict['normal_items']) if exam_dict['normal_items'] else {}
        exam_dict['abnormal_items'] = eval(exam_dict['abnormal_items']) if exam_dict['abnormal_items'] else {}
        physical_exams_data.append(exam_dict)
    
    health_record_dict['physical_exam_reports'] = physical_exams_data
    
    return {
        "success": True,
        "message": "更新健康档案成功",
        "data": {"health_record": health_record_dict}
    }

# 创建体检报告
@router.post("/{record_id}/physical-exams", response_model=BaseResponse)
async def create_physical_exam(
    record_id: int,
    physical_exam: PhysicalExamReportCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """创建体检报告"""
    # 检查健康档案是否属于当前用户
    health_record = db.query(HealthRecordModel).filter(
        HealthRecordModel.id == record_id, 
        HealthRecordModel.user_id == current_user.id
    ).first()
    
    if not health_record:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="健康档案不存在"
        )
    
    # 将字典转换为字符串存储
    key_findings_str = str(physical_exam.key_findings) if physical_exam.key_findings else "{}"
    normal_items_str = str(physical_exam.normal_items) if physical_exam.normal_items else "{}"
    abnormal_items_str = str(physical_exam.abnormal_items) if physical_exam.abnormal_items else "{}"
    
    db_physical_exam = PhysicalExamReportModel(
        health_record_id=record_id,
        report_name=physical_exam.report_name,
        exam_date=physical_exam.exam_date,
        hospital_name=physical_exam.hospital_name,
        doctor_name=physical_exam.doctor_name,
        key_findings=key_findings_str,
        normal_items=normal_items_str,
        abnormal_items=abnormal_items_str,
        created_time=physical_exam.created_time,
        updated_time=physical_exam.updated_time
    )
    
    db.add(db_physical_exam)
    db.commit()
    db.refresh(db_physical_exam)
    
    # 转换为字典格式以便返回
    exam_dict = db_physical_exam.__dict__
    exam_dict.pop('_sa_instance_state', None)
    # 将字符串转换回字典
    exam_dict['key_findings'] = eval(exam_dict['key_findings']) if exam_dict['key_findings'] else {}
    exam_dict['normal_items'] = eval(exam_dict['normal_items']) if exam_dict['normal_items'] else {}
    exam_dict['abnormal_items'] = eval(exam_dict['abnormal_items']) if exam_dict['abnormal_items'] else {}
    
    return {
        "success": True,
        "message": "创建体检报告成功",
        "data": {"physical_exam": exam_dict}
    }


# 获取体检报告列表
@router.get("/{user_id}/physical-exams", response_model=BaseResponse)
async def get_physical_exam_reports(
    user_id: int,
    skip: int = Query(0, ge=0, description="跳过的记录数"),
    limit: int = Query(10, ge=1, le=100, description="每页记录数"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """获取用户体检报告列表（兼容移动端API）"""
    # 确保当前用户只能访问自己的体检报告
    if user_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="无权访问其他用户的体检报告"
        )
    
    # 获取用户的健康档案
    health_record = db.query(HealthRecordModel).filter(
        HealthRecordModel.user_id == user_id
    ).first()
    
    if not health_record:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="健康档案不存在"
        )
    
    # 获取关联的体检报告并分页
    physical_exams = db.query(PhysicalExamReportModel).filter(
        PhysicalExamReportModel.health_record_id == health_record.id
    ).offset(skip).limit(limit).all()
    
    # 转换为字典格式以便返回
    physical_exams_data = []
    for exam in physical_exams:
        exam_dict = exam.__dict__
        exam_dict.pop('_sa_instance_state', None)
        # 将JSON字段从字符串转换为字典
        exam_dict['key_findings'] = eval(exam_dict['key_findings']) if exam_dict['key_findings'] else {}
        exam_dict['normal_items'] = eval(exam_dict['normal_items']) if exam_dict['normal_items'] else {}
        exam_dict['abnormal_items'] = eval(exam_dict['abnormal_items']) if exam_dict['abnormal_items'] else {}
        physical_exams_data.append(exam_dict)
    
    return {
        "success": True,
        "message": "获取体检报告列表成功",
        "data": physical_exams_data
    }

# 获取单个体检报告
@router.get("/physical-exams/{exam_id}", response_model=BaseResponse)
async def get_physical_exam(
    exam_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """获取单个体检报告"""
    physical_exam = db.query(PhysicalExamReportModel).filter(
        PhysicalExamReportModel.id == exam_id
    ).first()
    
    if not physical_exam:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="体检报告不存在"
        )
    
    # 检查该体检报告所属的健康档案是否属于当前用户
    health_record = db.query(HealthRecordModel).filter(
        HealthRecordModel.id == physical_exam.health_record_id,
        HealthRecordModel.user_id == current_user.id
    ).first()
    
    if not health_record:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="您没有权限查看该体检报告"
        )
    
    # 转换为字典格式以便返回
    exam_dict = physical_exam.__dict__
    exam_dict.pop('_sa_instance_state', None)
    # 将字符串转换回字典
    exam_dict['key_findings'] = eval(exam_dict['key_findings']) if exam_dict['key_findings'] else {}
    exam_dict['normal_items'] = eval(exam_dict['normal_items']) if exam_dict['normal_items'] else {}
    exam_dict['abnormal_items'] = eval(exam_dict['abnormal_items']) if exam_dict['abnormal_items'] else {}
    
    return {
        "success": True,
        "message": "获取体检报告成功",
        "data": {"physical_exam": exam_dict}
    }

# 更新体检报告
@router.put("/physical-exams/{exam_id}", response_model=BaseResponse)
async def update_physical_exam(
    exam_id: int,
    physical_exam: PhysicalExamReportUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """更新体检报告"""
    db_physical_exam = db.query(PhysicalExamReportModel).filter(
        PhysicalExamReportModel.id == exam_id
    ).first()
    
    if not db_physical_exam:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="体检报告不存在"
        )
    
    # 检查该体检报告所属的健康档案是否属于当前用户
    health_record = db.query(HealthRecordModel).filter(
        HealthRecordModel.id == db_physical_exam.health_record_id,
        HealthRecordModel.user_id == current_user.id
    ).first()
    
    if not health_record:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="您没有权限更新该体检报告"
        )
    
    # 更新字段
    for field, value in physical_exam.dict(exclude_unset=True).items():
        if field in ['key_findings', 'normal_items', 'abnormal_items'] and value is not None:
            setattr(db_physical_exam, field, str(value))
        else:
            setattr(db_physical_exam, field, value)
    
    # 更新时间
    from datetime import datetime
    db_physical_exam.updated_time = datetime.utcnow()
    
    db.commit()
    db.refresh(db_physical_exam)
    
    # 转换为字典格式以便返回
    exam_dict = db_physical_exam.__dict__
    exam_dict.pop('_sa_instance_state', None)
    # 将字符串转换回字典
    exam_dict['key_findings'] = eval(exam_dict['key_findings']) if exam_dict['key_findings'] else {}
    exam_dict['normal_items'] = eval(exam_dict['normal_items']) if exam_dict['normal_items'] else {}
    exam_dict['abnormal_items'] = eval(exam_dict['abnormal_items']) if exam_dict['abnormal_items'] else {}
    
    return {
        "success": True,
        "message": "更新体检报告成功",
        "data": {"physical_exam": exam_dict}
    }

# 删除体检报告
@router.delete("/physical-exams/{exam_id}", response_model=BaseResponse)
async def delete_physical_exam(
    exam_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """删除体检报告"""
    physical_exam = db.query(PhysicalExamReportModel).filter(
        PhysicalExamReportModel.id == exam_id
    ).first()
    
    if not physical_exam:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="体检报告不存在"
        )
    
    # 检查该体检报告所属的健康档案是否属于当前用户
    health_record = db.query(HealthRecordModel).filter(
        HealthRecordModel.id == physical_exam.health_record_id,
        HealthRecordModel.user_id == current_user.id
    ).first()
    
    if not health_record:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="您没有权限删除该体检报告"
        )
    
    db.delete(physical_exam)
    db.commit()
    
    return {
        "success": True,
        "message": "删除体检报告成功",
        "data": None
    }


# 添加新的体检报告（兼容移动端API - PUT版本）
@router.put("/{user_id}/physical-exams-add", response_model=BaseResponse)
async def add_physical_exam_report_mobile(
        user_id: int,
        physical_exam: PhysicalExamReportCreate,
        db: Session = Depends(get_db),
        current_user: User = Depends(get_current_user)
):
    """添加新的体检报告（兼容移动端API - 使用PUT方法）"""
    try:
        print(f"Debug: 收到体检报告请求 - user_id: {user_id}")
        print(f"Debug: 体检报告数据: {physical_exam}")
        print(f"Debug: 体检报告数据类型: {type(physical_exam).__name__}")
        # 记录每个字段的值和类型
        print(f"Debug: report_name: {getattr(physical_exam, 'report_name', None)} (type: {type(getattr(physical_exam, 'report_name', None)).__name__})")
        print(f"Debug: exam_date: {getattr(physical_exam, 'exam_date', None)} (type: {type(getattr(physical_exam, 'exam_date', None)).__name__})")
        print(f"Debug: hospital_name: {getattr(physical_exam, 'hospital_name', None)} (type: {type(getattr(physical_exam, 'hospital_name', None)).__name__})")
        print(f"Debug: key_findings: {getattr(physical_exam, 'key_findings', None)} (type: {type(getattr(physical_exam, 'key_findings', None)).__name__})")
        print(f"Debug: normal_items: {getattr(physical_exam, 'normal_items', None)} (type: {type(getattr(physical_exam, 'normal_items', None)).__name__})")
        print(f"Debug: abnormal_items: {getattr(physical_exam, 'abnormal_items', None)} (type: {type(getattr(physical_exam, 'abnormal_items', None)).__name__})")
        
        # 确保当前用户只能添加自己的体检报告
        if user_id != current_user.id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="无权为其他用户添加体检报告"
            )

        # 获取用户的健康档案
        health_record = db.query(HealthRecordModel).filter(
            HealthRecordModel.user_id == user_id
        ).first()

        if not health_record:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="健康档案不存在"
            )
        
        print(f"Debug: 找到健康档案 - ID: {health_record.id}")

        # 安全地处理JSON字段，避免使用eval()
        def safe_convert(obj):
            if isinstance(obj, dict):
                return str(obj)
            return "{}"
        
        key_findings_str = safe_convert(getattr(physical_exam, 'key_findings', {}))
        normal_items_str = safe_convert(getattr(physical_exam, 'normal_items', {}))
        abnormal_items_str = safe_convert(getattr(physical_exam, 'abnormal_items', {}))
        
        # 为可选字段提供默认值
        doctor_comments = getattr(physical_exam, 'doctor_comments', None)
        summary = getattr(physical_exam, 'summary', None)
        recommendations = getattr(physical_exam, 'recommendations', None)
        report_url = getattr(physical_exam, 'report_url', None)
        
        # 创建当前时间戳
        now = datetime.now()
        
        # 处理exam_date字段：从字符串转换为datetime对象
        exam_date = now  # 默认使用当前时间
        if hasattr(physical_exam, 'exam_date') and physical_exam.exam_date:
            try:
                # 尝试解析不同的日期格式
                date_str = physical_exam.exam_date
                if isinstance(date_str, str):
                    # 尝试多种日期格式
                    for date_format in ['%Y-%m-%d', '%Y-%m-%d %H:%M:%S', '%Y/%m/%d', '%m/%d/%Y']:
                        try:
                            exam_date = datetime.strptime(date_str, date_format)
                            break
                        except ValueError:
                            continue
                    else:
                        # 如果所有格式都失败，使用当前时间
                        print(f"Warning: Could not parse exam_date '{date_str}', using current time")
                        exam_date = now
                elif isinstance(date_str, datetime):
                    exam_date = date_str
            except Exception as e:
                print(f"Error parsing exam_date: {e}, using current time")
                exam_date = now

        # 确保health_record.id是整数类型
        health_record_id_value = int(health_record.id) if health_record and health_record.id else 0
        
        db_physical_exam = PhysicalExamReportModel(
            health_record_id=health_record_id_value,
            report_name=physical_exam.report_name or "体检报告",
            exam_date=exam_date,  # 使用解析后的exam_date
            hospital_name=getattr(physical_exam, 'hospital_name', None),
            doctor_comments=doctor_comments,
            summary=summary,
            key_findings=key_findings_str,
            normal_items=normal_items_str,
            abnormal_items=abnormal_items_str,
            recommendations=recommendations,
            report_url=report_url,
            created_time=now,
            updated_time=now
        )

        db.add(db_physical_exam)
        db.commit()
        db.refresh(db_physical_exam)

        # 转换为字典格式以便返回
        exam_dict = db_physical_exam.__dict__
        exam_dict.pop('_sa_instance_state', None)
        
        # 安全地将字符串转回字典
        try:
            exam_dict['key_findings'] = eval(exam_dict['key_findings']) if exam_dict['key_findings'] else {}
        except:
            exam_dict['key_findings'] = {}
        try:
            exam_dict['normal_items'] = eval(exam_dict['normal_items']) if exam_dict['normal_items'] else {}
        except:
            exam_dict['normal_items'] = {}
        try:
            exam_dict['abnormal_items'] = eval(exam_dict['abnormal_items']) if exam_dict['abnormal_items'] else {}
        except:
            exam_dict['abnormal_items'] = {}

        return {
            "success": True,
            "message": "添加体检报告成功",
            "data": {"physical_exam": exam_dict}
        }
    except ValidationError as e:
        print(f"Error: 数据验证失败 - {str(e)}")
        print(f"ValidationError详情: {e.errors()}")
        # 提取字段级别的错误信息
        field_errors = {}
        for error in e.errors():
            field = ' -> '.join([str(loc) for loc in error['loc']]) if 'loc' in error else 'unknown'
            field_errors[field] = error['msg'] if 'msg' in error else str(error)
        print(f"字段验证错误: {field_errors}")
        
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=f"数据验证错误: {field_errors}"
        )
    except Exception as e:
        print(f"Error: 添加体检报告失败 - {str(e)}")
        print(f"Error type: {type(e).__name__}")
        # 捕获任何可能的键错误
        if isinstance(e, KeyError):
            print(f"KeyError: 缺少字段 '{e.args[0]}'")
            raise HTTPException(
                status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
                detail=f"请求数据缺少必要字段: {e.args[0]}"
            )
        # 捕获类型错误
        if isinstance(e, TypeError):
            print(f"TypeError: {str(e)}")
            raise HTTPException(
                status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
                detail=f"数据类型错误: {str(e)}"
            )
        # 捕获其他可能的验证相关错误
        if hasattr(e, 'errors'):
            print(f"Validation errors: {e.errors()}")
        
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"添加体检报告失败: {str(e)}"
        )
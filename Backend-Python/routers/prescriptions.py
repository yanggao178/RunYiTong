from fastapi import APIRouter, Depends, HTTPException, Query, UploadFile, File
from sqlalchemy.orm import Session
from typing import List, Optional
import os
import uuid
from database import get_db
from models import Prescription as PrescriptionModel
from schemas import Prescription, PrescriptionCreate, PrescriptionUpdate, PaginatedResponse

router = APIRouter()

# 获取处方列表（支持分页和搜索）
@router.get("/", response_model=PaginatedResponse)
async def get_prescriptions(
    page: int = Query(1, ge=1, description="页码"),
    size: int = Query(10, ge=1, le=100, description="每页数量"),
    user_id: Optional[int] = Query(None, description="用户ID"),
    status: Optional[str] = Query(None, description="处方状态"),
    db: Session = Depends(get_db)
):
    """获取处方列表"""
    query = db.query(PrescriptionModel)
    
    # 用户过滤
    if user_id:
        query = query.filter(PrescriptionModel.user_id == user_id)
    
    # 状态过滤
    if status:
        query = query.filter(PrescriptionModel.status == status)
    
    # 按创建时间倒序排列
    query = query.order_by(PrescriptionModel.created_time.desc())
    
    # 计算总数
    total = query.count()
    
    # 分页
    offset = (page - 1) * size
    prescriptions = query.offset(offset).limit(size).all()
    
    # 计算总页数
    pages = (total + size - 1) // size
    
    return PaginatedResponse(
        items=[Prescription.from_orm(prescription).dict() for prescription in prescriptions],
        total=total,
        page=page,
        size=size,
        pages=pages
    )

# 获取单个处方详情
@router.get("/{prescription_id}", response_model=Prescription)
async def get_prescription(prescription_id: int, db: Session = Depends(get_db)):
    """获取处方详情"""
    prescription = db.query(PrescriptionModel).filter(
        PrescriptionModel.id == prescription_id
    ).first()
    if not prescription:
        raise HTTPException(status_code=404, detail="处方不存在")
    return prescription

# 创建处方
@router.post("/", response_model=Prescription)
async def create_prescription(prescription: PrescriptionCreate, db: Session = Depends(get_db)):
    """创建处方"""
    db_prescription = PrescriptionModel(**prescription.dict())
    db.add(db_prescription)
    db.commit()
    db.refresh(db_prescription)
    return db_prescription

# 更新处方
@router.put("/{prescription_id}", response_model=Prescription)
async def update_prescription(
    prescription_id: int, 
    prescription_update: PrescriptionUpdate, 
    db: Session = Depends(get_db)
):
    """更新处方"""
    db_prescription = db.query(PrescriptionModel).filter(
        PrescriptionModel.id == prescription_id
    ).first()
    if not db_prescription:
        raise HTTPException(status_code=404, detail="处方不存在")
    
    # 更新字段
    update_data = prescription_update.dict(exclude_unset=True)
    for field, value in update_data.items():
        setattr(db_prescription, field, value)
    
    db.commit()
    db.refresh(db_prescription)
    return db_prescription

# 删除处方
@router.delete("/{prescription_id}")
async def delete_prescription(prescription_id: int, db: Session = Depends(get_db)):
    """删除处方"""
    db_prescription = db.query(PrescriptionModel).filter(
        PrescriptionModel.id == prescription_id
    ).first()
    if not db_prescription:
        raise HTTPException(status_code=404, detail="处方不存在")
    
    db.delete(db_prescription)
    db.commit()
    return {"message": "处方删除成功"}

# 上传处方图片
@router.post("/{prescription_id}/upload-image")
async def upload_prescription_image(
    prescription_id: int,
    file: UploadFile = File(...),
    db: Session = Depends(get_db)
):
    """上传处方图片"""
    # 检查处方是否存在
    db_prescription = db.query(PrescriptionModel).filter(
        PrescriptionModel.id == prescription_id
    ).first()
    if not db_prescription:
        raise HTTPException(status_code=404, detail="处方不存在")
    
    # 检查文件类型
    if not file.content_type.startswith('image/'):
        raise HTTPException(status_code=400, detail="只能上传图片文件")
    
    # 创建上传目录
    upload_dir = "static/prescriptions"
    os.makedirs(upload_dir, exist_ok=True)
    
    # 生成唯一文件名
    file_extension = file.filename.split('.')[-1]
    unique_filename = f"{uuid.uuid4()}.{file_extension}"
    file_path = os.path.join(upload_dir, unique_filename)
    
    # 保存文件
    with open(file_path, "wb") as buffer:
        content = await file.read()
        buffer.write(content)
    
    # 更新处方记录
    db_prescription.image_url = f"/static/prescriptions/{unique_filename}"
    db.commit()
    db.refresh(db_prescription)
    
    return {
        "message": "图片上传成功",
        "image_url": db_prescription.image_url
    }

# 症状分析（模拟AI分析）
@router.post("/analyze-symptoms")
async def analyze_symptoms(symptoms: str):
    """症状分析（模拟AI分析功能）"""
    # 这里可以集成真实的AI分析服务
    # 目前返回模拟的分析结果
    
    analysis_result = {
        "symptoms": symptoms,
        "possible_diagnosis": [
            "根据症状描述，可能的诊断包括：",
            "1. 常见感冒 - 概率60%",
            "2. 过敏性鼻炎 - 概率25%",
            "3. 其他呼吸道疾病 - 概率15%"
        ],
        "recommendations": [
            "建议多休息，多喝水",
            "如症状持续或加重，请及时就医",
            "可适当服用对症药物缓解症状"
        ],
        "suggested_prescription": {
            "medicines": [
                {"name": "感冒灵颗粒", "dosage": "每次1袋，每日3次", "duration": "3-5天"},
                {"name": "维生素C片", "dosage": "每次2片，每日2次", "duration": "1周"}
            ],
            "notes": "请在医生指导下用药，如有不适请立即停药并就医"
        }
    }
    
    return analysis_result

# 获取用户的处方历史
@router.get("/user/{user_id}/history", response_model=List[Prescription])
async def get_user_prescription_history(
    user_id: int, 
    limit: int = Query(10, ge=1, le=50, description="返回数量限制"),
    db: Session = Depends(get_db)
):
    """获取用户的处方历史"""
    prescriptions = db.query(PrescriptionModel).filter(
        PrescriptionModel.user_id == user_id
    ).order_by(
        PrescriptionModel.created_time.desc()
    ).limit(limit).all()
    
    return prescriptions

# 更新处方状态
@router.patch("/{prescription_id}/status")
async def update_prescription_status(
    prescription_id: int,
    status: str,
    db: Session = Depends(get_db)
):
    """更新处方状态"""
    valid_statuses = ["draft", "issued", "dispensed"]
    if status not in valid_statuses:
        raise HTTPException(
            status_code=400, 
            detail=f"无效的状态值，有效值为: {', '.join(valid_statuses)}"
        )
    
    db_prescription = db.query(PrescriptionModel).filter(
        PrescriptionModel.id == prescription_id
    ).first()
    if not db_prescription:
        raise HTTPException(status_code=404, detail="处方不存在")
    
    db_prescription.status = status
    db.commit()
    db.refresh(db_prescription)
    
    return {"message": "处方状态更新成功", "status": status}
from fastapi import APIRouter, Depends, HTTPException, Query, UploadFile, File, Form
from sqlalchemy.orm import Session
from typing import List, Optional
import os
import uuid
from database import get_db
from models import Prescription as PrescriptionModel
from schemas import Prescription, PrescriptionCreate, PrescriptionUpdate, PaginatedResponse
from ai.ai_prescription import generate_tcm_prescription

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
async def analyze_symptoms(symptoms: str = Form(...)):
    """症状分析（集成AI中医处方生成）"""
    try:
        # 从环境变量获取API密钥和模型配置
        api_key = os.getenv("OPENAI_API_KEY")
        ai_model = os.getenv("AI_MODEL", "deepseek-chat")
        if not api_key:
            # 如果没有API密钥，返回模拟结果
            analysis_data = {
                "symptoms": symptoms,
                "analysis": "暂未配置AI服务，返回模拟分析结果",
                "syndrome_type": "风寒表证",
                "treatment_method": "辛温解表",
                "main_prescription": "桂枝汤加减",
                "composition": [
                    {"药材": "桂枝", "剂量": "10g", "角色": "君药"},
                    {"药材": "白芍", "剂量": "10g", "角色": "臣药"},
                    {"药材": "生姜", "剂量": "6g", "角色": "佐药"},
                    {"药材": "大枣", "剂量": "3枚", "角色": "使药"},
                    {"药材": "甘草", "剂量": "6g", "角色": "使药"}
                ],
                "usage": "水煎服，每日1剂，分2次温服",
                "contraindications": "孕妇慎用，高血压患者注意监测血压"
            }
            return {
                "success": True,
                "message": "症状分析完成",
                "data": analysis_data
            }
        
        # 调用AI生成处方
        prescription = generate_tcm_prescription(
            symptoms=symptoms,
            api_key=api_key,
            patient_info=None,  # 可以根据需要传入患者信息
            model=ai_model,
            max_tokens=1000
        )
        
        analysis_data = {
            "symptoms": symptoms,
            "analysis": "AI中医辨证分析完成",
            "syndrome_type": prescription.syndrome_type,
            "treatment_method": prescription.treatment_method,
            "main_prescription": prescription.main_prescription,
            "composition": prescription.composition,
            "usage": prescription.usage,
            "contraindications": prescription.contraindications
        }
        return {
            "success": True,
            "message": "AI症状分析完成",
            "data": analysis_data
        }
        
    except Exception as e:
        # 如果AI调用失败，返回错误信息
        analysis_data = {
            "symptoms": symptoms,
            "analysis": f"AI分析失败: {str(e)}",
            "syndrome_type": "分析失败",
            "treatment_method": "请咨询专业中医师",
            "main_prescription": "暂无",
            "composition": [],
            "usage": "请遵医嘱",
            "contraindications": "请咨询医生"
        }
        return {
            "success": False,
            "message": f"分析失败: {str(e)}",
            "data": analysis_data
        }

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
from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel, Field
from typing import Optional
import logging

# 导入自定义模块
from config.alipay_config import alipay_config
from utils.alipay_signature import AlipaySignature
from utils.alipay_order_builder import AlipayOrderBuilder
from database import get_db
from sqlalchemy.orm import Session
from models import Product

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

router = APIRouter()

# 请求模型
class PaymentOrderRequest(BaseModel):
    product_id: int = Field(..., description="商品ID")
    quantity: int = Field(default=1, ge=1, description="购买数量")
    timeout_express: str = Field(default="30m", description="支付超时时间")

# 响应模型
class PaymentOrderResponse(BaseModel):
    success: bool
    message: str
    order_string: Optional[str] = None
    order_info: Optional[dict] = None

@router.post("/alipay/create-order")
async def create_alipay_order(request: PaymentOrderRequest, db: Session = Depends(get_db)):
    """
    创建支付宝支付订单
    """
    try:
        # 1. 验证商品是否存在
        product = db.query(Product).filter(Product.id == request.product_id).first()
        if not product:
            raise HTTPException(
                status_code=404, 
                detail={
                    "success": False,
                    "message": "商品不存在",
                    "data": None
                }
            )
        
        # 2. 计算总金额
        total_amount = float(product.price) * request.quantity
        
        # 3. 构建订单参数
        order_params = AlipayOrderBuilder.build_order_params(
            app_id=alipay_config.get_app_id(),
            product_name=product.name,
            product_description=product.description or product.name,
            total_amount=total_amount,
            notify_url=alipay_config.get_notify_url(),
            quantity=request.quantity,
            timeout_express=request.timeout_express
        )
        
        # 4. 验证参数完整性
        if not AlipayOrderBuilder.validate_order_params(order_params):
            raise HTTPException(
                status_code=400, 
                detail={
                    "success": False,
                    "message": "订单参数验证失败",
                    "data": None
                }
            )
        
        # 5. 生成签名并构建订单字符串
        try:
            private_key = alipay_config.get_private_key()
            order_string = AlipaySignature.generate_order_string(order_params, private_key)
        except ValueError as e:
            logger.error(f"签名生成失败: {str(e)}")
            raise HTTPException(
                status_code=500, 
                detail={
                    "success": False,
                    "message": f"签名生成失败: {str(e)}",
                    "data": None
                }
            )
        
        # 6. 提取订单信息用于返回
        order_info = AlipayOrderBuilder.extract_order_info(order_params["biz_content"])
        order_info.update({
            "product_name": product.name,
            "quantity": request.quantity,
            "unit_price": float(product.price),
            "total_amount": total_amount
        })
        
        logger.info(f"支付宝订单创建成功，商品ID: {request.product_id}, 订单号: {order_info.get('out_trade_no')}")
        
        # 构建PaymentOrderResponse对象
        payment_response = PaymentOrderResponse(
            success=True,
            message="订单创建成功",
            order_string=order_string,
            order_info=order_info
        )
        
        # 返回符合Android前端期望的ApiResponse格式
        return {
            "success": True,
            "message": "订单创建成功",
            "data": payment_response
        }
        
    except HTTPException as e:
        # HTTPException已经包含了状态码和详细信息，直接抛出
        raise
    except Exception as e:
        logger.error(f"创建支付宝订单失败: {str(e)}")
        # 返回符合ApiResponse格式的错误响应
        raise HTTPException(
            status_code=500, 
            detail={
                "success": False,
                "message": f"创建订单失败: {str(e)}",
                "data": None
            }
        )

@router.get("/alipay/config")
async def get_alipay_config():
    """
    获取支付宝配置信息（仅返回非敏感信息）
    """
    return {
        "app_id": alipay_config.get_app_id(),
        "sign_type": alipay_config.get_sign_type(),
        "charset": alipay_config.get_charset(),
        "is_sandbox": alipay_config.is_sandbox()
    }

@router.post("/alipay/verify-payment")
async def verify_alipay_payment(payment_result: dict):
    """
    验证支付宝支付结果
    注意：这里应该实现支付结果的验证逻辑
    """
    try:
        # TODO: 实现支付结果验证逻辑
        # 1. 验证签名
        # 2. 验证订单状态
        # 3. 更新数据库订单状态
        
        result_status = payment_result.get("resultStatus", "")
        
        if result_status == "9000":
            return {
                "success": True,
                "message": "支付成功",
                "verified": True
            }
        elif result_status == "8000":
            return {
                "success": False,
                "message": "支付结果确认中",
                "verified": False
            }
        elif result_status == "4000":
            return {
                "success": False,
                "message": "支付失败",
                "verified": False
            }
        elif result_status == "6001":
            return {
                "success": False,
                "message": "用户取消支付",
                "verified": False
            }
        else:
            return {
                "success": False,
                "message": "未知支付状态",
                "verified": False
            }
            
    except Exception as e:
        logger.error(f"验证支付结果失败: {str(e)}")
        raise HTTPException(status_code=500, detail=f"验证支付结果失败: {str(e)}")
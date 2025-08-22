from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel, Field
from typing import Optional
import logging

# 导入自定义模块
from config.alipay_config import alipay_config
from utils.alipay_signature import AlipaySignature
from utils.alipay_order_builder import AlipayOrderBuilder
from config.wechat_config import wechat_config
from utils.wechat_signature import WechatSignature
from utils.wechat_order_builder import WechatOrderBuilder
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

# ==================== 微信支付相关接口 ====================

@router.post("/wechat/create-order")
async def create_wechat_order(request: PaymentOrderRequest, db: Session = Depends(get_db)):
    """
    创建微信支付订单
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
        
        # 2. 验证微信支付配置
        if not wechat_config.validate_config():
            raise HTTPException(
                status_code=500,
                detail={
                    "success": False,
                    "message": "微信支付配置不完整，请检查配置信息",
                    "data": None
                }
            )
        
        # 3. 创建微信支付订单
        order_builder = WechatOrderBuilder()
        success, result = order_builder.create_unified_order(
            product_id=request.product_id,
            product_name=product.name,
            product_price=float(product.price),
            quantity=request.quantity,
            user_ip='127.0.0.1'  # 可以从请求中获取真实IP
        )
        
        if not success:
            error_msg = result.get('error', '创建订单失败')
            logger.error(f"微信订单创建失败: {error_msg}")
            raise HTTPException(
                status_code=500,
                detail={
                    "success": False,
                    "message": error_msg,
                    "data": None
                }
            )
        
        # 4. 构建订单信息字符串（用于Android客户端）
        app_pay_params = result['app_pay_params']
        order_string = order_builder.build_order_info_string(app_pay_params)
        
        # 5. 构建返回的订单信息
        order_info = {
            "out_trade_no": result['out_trade_no'],
            "prepay_id": result['prepay_id'],
            "total_fee": result['total_fee'],
            "product_name": product.name,
            "quantity": request.quantity,
            "unit_price": float(product.price),
            "total_amount": float(product.price) * request.quantity,
            "app_pay_params": app_pay_params
        }
        
        logger.info(f"微信订单创建成功，商品ID: {request.product_id}, 订单号: {result['out_trade_no']}")
        
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
        logger.error(f"创建微信订单失败: {str(e)}")
        # 返回符合ApiResponse格式的错误响应
        raise HTTPException(
            status_code=500, 
            detail={
                "success": False,
                "message": f"创建订单失败: {str(e)}",
                "data": None
            }
        )

@router.get("/wechat/config")
async def get_wechat_config():
    """
    获取微信支付配置信息（仅返回非敏感信息）
    """
    return {
        "app_id": wechat_config.get_app_id(),
        "mch_id": wechat_config.get_mch_id(),
        "trade_type": wechat_config.get_trade_type(),
        "sign_type": wechat_config.get_sign_type(),
        "is_sandbox": wechat_config.is_sandbox()
    }

@router.post("/wechat/verify-payment")
async def verify_wechat_payment(payment_result: dict):
    """
    验证微信支付结果
    """
    try:
        # TODO: 实现微信支付结果验证逻辑
        # 1. 验证签名
        # 2. 验证订单状态
        # 3. 更新数据库订单状态
        
        result_code = payment_result.get("result_code", "")
        return_code = payment_result.get("return_code", "")
        
        if return_code == "SUCCESS" and result_code == "SUCCESS":
            return {
                "success": True,
                "message": "支付成功",
                "verified": True
            }
        elif return_code == "SUCCESS" and result_code == "FAIL":
            err_code = payment_result.get("err_code", "")
            err_code_des = payment_result.get("err_code_des", "支付失败")
            return {
                "success": False,
                "message": f"支付失败: {err_code_des}",
                "verified": False,
                "err_code": err_code
            }
        else:
            return {
                "success": False,
                "message": "支付状态未知",
                "verified": False
            }
            
    except Exception as e:
        logger.error(f"验证微信支付结果失败: {str(e)}")
        raise HTTPException(status_code=500, detail=f"验证支付结果失败: {str(e)}")

@router.post("/wechat/query-order")
async def query_wechat_order(out_trade_no: str):
    """
    查询微信支付订单状态
    """
    try:
        order_builder = WechatOrderBuilder()
        success, result = order_builder.query_order(out_trade_no)
        
        if not success:
            error_msg = result.get('error', '查询订单失败')
            return {
                "success": False,
                "message": error_msg,
                "data": None
            }
        
        return {
            "success": True,
            "message": "查询成功",
            "data": result
        }
        
    except Exception as e:
        logger.error(f"查询微信订单失败: {str(e)}")
        raise HTTPException(status_code=500, detail=f"查询订单失败: {str(e)}")
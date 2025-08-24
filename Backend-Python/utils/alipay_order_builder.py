import json
import time
import uuid
from datetime import datetime, timedelta
from typing import Dict, Any, Optional
from decimal import Decimal, ROUND_HALF_UP

class AlipayOrderBuilder:
    """
    支付宝订单信息构建器
    """
    
    @staticmethod
    def generate_out_trade_no() -> str:
        """
        生成商户订单号
        格式: ORDER_时间戳_随机数
        """
        timestamp = str(int(time.time() * 1000))
        random_str = str(uuid.uuid4()).replace('-', '')[:8]
        return f"ORDER_{timestamp}_{random_str}"
    
    @staticmethod
    def format_amount(amount: float) -> str:
        """
        格式化金额，保留两位小数
        """
        decimal_amount = Decimal(str(amount))
        formatted_amount = decimal_amount.quantize(Decimal('0.01'), rounding=ROUND_HALF_UP)
        return str(formatted_amount)
    
    @staticmethod
    def build_biz_content(product_name: str, product_description: str, 
                         total_amount: float, quantity: int = 1,
                         timeout_express: str = "30m") -> str:
        """
        构建业务参数biz_content
        """
        out_trade_no = AlipayOrderBuilder.generate_out_trade_no()
        formatted_amount = AlipayOrderBuilder.format_amount(total_amount)
        
        # 构建商品标题on（包含数量信息）
        subject = f"{product_name}"
        if quantity > 1:
            subject += f"({quantity}件)"
        
        biz_content = {
            "timeout_express": timeout_express,
            "product_code": "QUICK_MSECURITY_PAY",
            "total_amount": formatted_amount,
            "subject": subject,
            "body": product_description,
            "out_trade_no": out_trade_no
        }
        
        # 支付宝要求严格的UTF-8编码，保持中文字符原样
        return json.dumps(biz_content, ensure_ascii=False, separators=(',', ':'))
    
    @staticmethod
    def build_order_params(app_id: str, product_name: str, product_description: str,
                          total_amount: float, notify_url: str, quantity: int = 1,
                          timeout_express: str = "30m") -> Dict[str, Any]:
        """
        构建完整的订单参数
        """
        biz_content = AlipayOrderBuilder.build_biz_content(
            product_name, product_description, total_amount, quantity, timeout_express
        )
        
        params = {
            "app_id": app_id,
            "method": "alipay.trade.app.pay",
            "charset": "utf-8",
            "sign_type": "RSA2",
            "timestamp": datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
            "version": "1.0",
            "notify_url": notify_url,
            "biz_content": biz_content
        }
        
        return params
    
    @staticmethod
    def extract_order_info(biz_content_str: str) -> Dict[str, Any]:
        """
        从biz_content中提取订单信息
        """
        try:
            biz_content = json.loads(biz_content_str)
            return {
                "out_trade_no": biz_content.get("out_trade_no"),
                "total_amount": biz_content.get("total_amount"),
                "subject": biz_content.get("subject"),
                "body": biz_content.get("body"),
                "timeout_express": biz_content.get("timeout_express")
            }
        except json.JSONDecodeError:
            return {}
    
    @staticmethod
    def validate_order_params(params: Dict[str, Any]) -> bool:
        """
        验证订单参数的完整性
        """
        required_fields = [
            "app_id", "method", "charset", "sign_type", 
            "timestamp", "version", "biz_content"
        ]
        
        for field in required_fields:
            if field not in params or not params[field]:
                return False
        
        # 验证biz_content格式
        try:
            biz_content = json.loads(params["biz_content"])
            required_biz_fields = ["total_amount", "subject", "out_trade_no"]
            for field in required_biz_fields:
                if field not in biz_content or not biz_content[field]:
                    return False
        except json.JSONDecodeError:
            return False
        
        return True
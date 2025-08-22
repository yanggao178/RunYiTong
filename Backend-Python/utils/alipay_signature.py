import base64
import hashlib
import json
from datetime import datetime
from typing import Dict, Any
from urllib.parse import quote_plus, urlencode
from Crypto.PublicKey import RSA
from Crypto.Signature import pkcs1_15
from Crypto.Hash import SHA256

class AlipaySignature:
    """
    支付宝RSA2签名工具类
    """
    
    @staticmethod
    def format_private_key(private_key: str) -> str:
        """
        格式化私钥，支付宝要求私钥写在一行（不包含头尾标识和换行符）
        支持PKCS#1和PKCS#8格式的私钥
        """
        if not private_key:
            raise ValueError("私钥不能为空")
            
        # 移除可能存在的头尾标识和换行符，只保留密钥内容
        clean_key = private_key.replace('-----BEGIN PRIVATE KEY-----', '')
        clean_key = clean_key.replace('-----END PRIVATE KEY-----', '')
        clean_key = clean_key.replace('-----BEGIN RSA PRIVATE KEY-----', '')
        clean_key = clean_key.replace('-----END RSA PRIVATE KEY-----', '')
        clean_key = clean_key.replace('\n', '').replace('\r', '').replace(' ', '')
        
        # 支付宝要求私钥写在一行，不需要头尾标识
        return clean_key
    
    @staticmethod
    def format_public_key(public_key: str) -> str:
        """
        格式化支付宝公钥，支付宝要求公钥写在一行（不包含头尾标识和换行符）
        """
        if not public_key:
            raise ValueError("公钥不能为空")
            
        # 移除可能存在的头尾标识和换行符，只保留公钥内容
        clean_key = public_key.replace('-----BEGIN PUBLIC KEY-----', '')
        clean_key = clean_key.replace('-----END PUBLIC KEY-----', '')
        clean_key = clean_key.replace('\n', '').replace('\r', '').replace(' ', '')
        
        # 支付宝要求公钥写在一行，不需要头尾标识
        return clean_key
    
    @staticmethod
    def build_sign_string(params: Dict[str, Any]) -> str:
        """
        构建待签名字符串
        """
        # 过滤空值和sign参数
        filtered_params = {k: v for k, v in params.items() 
                          if v is not None and v != '' and k != 'sign'}
        
        # 按key排序
        sorted_params = sorted(filtered_params.items())
        
        # 构建查询字符串
        sign_string = '&'.join([f"{k}={v}" for k, v in sorted_params])
        
        return sign_string
    
    @staticmethod
    def rsa2_sign(content: str, private_key: str) -> str:
        """
        RSA2签名
        """
        try:
            # 格式化私钥（移除头尾标识）
            clean_private_key = AlipaySignature.format_private_key(private_key)
            
            # 为RSA.import_key添加PKCS#8格式的头尾标识
            formatted_private_key = f"-----BEGIN PRIVATE KEY-----\n{clean_private_key}\n-----END PRIVATE KEY-----"
            
            # 导入私钥
            key = RSA.import_key(formatted_private_key)
            
            # 创建签名对象
            h = SHA256.new(content.encode('utf-8'))
            signature = pkcs1_15.new(key).sign(h)
            
            # Base64编码
            sign = base64.b64encode(signature).decode('utf-8')
            
            return sign
            
        except Exception as e:
            raise ValueError(f"RSA2签名失败: {str(e)}")
    
    @staticmethod
    def generate_timestamp() -> str:
        """
        生成时间戳
        """
        return datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    
    @staticmethod
    def generate_order_string(params: Dict[str, Any], private_key: str) -> str:
        """
        生成完整的订单字符串（包含签名）
        严格按照支付宝规范构建订单字符串
        """
        # 添加时间戳
        if 'timestamp' not in params:
            params['timestamp'] = AlipaySignature.generate_timestamp()
        
        # 构建待签名字符串（不包含sign参数）
        sign_string = AlipaySignature.build_sign_string(params)
        
        # 生成签名
        sign = AlipaySignature.rsa2_sign(sign_string, private_key)
        
        # 构建最终的订单字符串
        # 按照支付宝要求的顺序和格式
        order_parts = []
        
        # 按字母顺序排列参数（除了sign）
        sorted_params = sorted(params.items())
        
        for k, v in sorted_params:
            # 对参数值进行URL编码
            if isinstance(v, dict) or isinstance(v, list):
                # JSON对象需要序列化后编码
                encoded_value = quote_plus(json.dumps(v, ensure_ascii=False, separators=(',', ':')), safe='')
            else:
                # 普通字符串值进行URL编码，biz_content已经是JSON字符串
                encoded_value = quote_plus(str(v), safe='')
            order_parts.append(f"{k}={encoded_value}")
        
        # 添加签名（签名不进行URL编码）
        order_parts.append(f"sign={sign}")
        
        order_string = '&'.join(order_parts)
        return order_string
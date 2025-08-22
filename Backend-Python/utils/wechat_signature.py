import hashlib
import xml.etree.ElementTree as ET
from typing import Dict, Any, Optional
import random
import string
import time

class WechatSignature:
    """
    微信支付签名工具类
    实现微信支付的MD5签名算法
    """
    
    @staticmethod
    def generate_nonce_str(length: int = 32) -> str:
        """
        生成随机字符串
        
        Args:
            length: 随机字符串长度，默认32位
            
        Returns:
            随机字符串
        """
        chars = string.ascii_letters + string.digits
        return ''.join(random.choice(chars) for _ in range(length))
    
    @staticmethod
    def generate_timestamp() -> str:
        """
        生成时间戳
        
        Returns:
            当前时间戳字符串
        """
        return str(int(time.time()))
    
    @staticmethod
    def create_sign(params: Dict[str, Any], api_key: str) -> str:
        """
        创建微信支付签名
        
        Args:
            params: 参数字典
            api_key: API密钥
            
        Returns:
            MD5签名字符串
        """
        # 过滤空值和sign字段
        filtered_params = {}
        for key, value in params.items():
            if value is not None and value != '' and key != 'sign':
                filtered_params[key] = str(value)
        
        # 按key排序
        sorted_params = sorted(filtered_params.items())
        
        # 构建签名字符串
        sign_str = '&'.join([f'{key}={value}' for key, value in sorted_params])
        sign_str += f'&key={api_key}'
        
        # MD5加密并转大写
        md5_hash = hashlib.md5(sign_str.encode('utf-8')).hexdigest()
        return md5_hash.upper()
    
    @staticmethod
    def verify_sign(params: Dict[str, Any], api_key: str) -> bool:
        """
        验证微信支付签名
        
        Args:
            params: 包含签名的参数字典
            api_key: API密钥
            
        Returns:
            签名是否正确
        """
        if 'sign' not in params:
            return False
        
        received_sign = params['sign']
        calculated_sign = WechatSignature.create_sign(params, api_key)
        
        return received_sign == calculated_sign
    
    @staticmethod
    def dict_to_xml(params: Dict[str, Any]) -> str:
        """
        将字典转换为XML格式
        
        Args:
            params: 参数字典
            
        Returns:
            XML字符串
        """
        xml_str = '<xml>'
        for key, value in params.items():
            if value is not None:
                xml_str += f'<{key}><![CDATA[{value}]]></{key}>'
        xml_str += '</xml>'
        return xml_str
    
    @staticmethod
    def xml_to_dict(xml_str: str) -> Dict[str, str]:
        """
        将XML字符串转换为字典
        
        Args:
            xml_str: XML字符串
            
        Returns:
            参数字典
        """
        try:
            root = ET.fromstring(xml_str)
            result = {}
            for child in root:
                result[child.tag] = child.text
            return result
        except ET.ParseError as e:
            raise ValueError(f"XML解析失败: {str(e)}")
    
    @staticmethod
    def create_prepay_params(app_id: str, mch_id: str, api_key: str, 
                           out_trade_no: str, total_fee: int, body: str, 
                           notify_url: str, trade_type: str = 'APP',
                           spbill_create_ip: str = '127.0.0.1') -> Dict[str, str]:
        """
        创建统一下单参数
        
        Args:
            app_id: 应用ID
            mch_id: 商户号
            api_key: API密钥
            out_trade_no: 商户订单号
            total_fee: 总金额（分）
            body: 商品描述
            notify_url: 通知地址
            trade_type: 交易类型
            spbill_create_ip: 终端IP
            
        Returns:
            包含签名的参数字典
        """
        params = {
            'appid': app_id,
            'mch_id': mch_id,
            'nonce_str': WechatSignature.generate_nonce_str(),
            'body': body,
            'out_trade_no': out_trade_no,
            'total_fee': str(total_fee),
            'spbill_create_ip': spbill_create_ip,
            'notify_url': notify_url,
            'trade_type': trade_type
        }
        
        # 生成签名
        params['sign'] = WechatSignature.create_sign(params, api_key)
        
        return params
    
    @staticmethod
    def create_app_pay_params(app_id: str, mch_id: str, prepay_id: str, api_key: str) -> Dict[str, str]:
        """
        创建APP支付参数
        
        Args:
            app_id: 应用ID
            mch_id: 商户号
            prepay_id: 预支付交易会话ID
            api_key: API密钥
            
        Returns:
            APP支付参数字典
        """
        timestamp = WechatSignature.generate_timestamp()
        nonce_str = WechatSignature.generate_nonce_str()
        
        params = {
            'appid': app_id,
            'partnerid': mch_id,
            'prepayid': prepay_id,
            'package': 'Sign=WXPay',
            'noncestr': nonce_str,
            'timestamp': timestamp
        }
        
        # 生成签名
        params['sign'] = WechatSignature.create_sign(params, api_key)
        
        return params
import os
from typing import Optional

class WechatConfig:
    """
    微信支付配置管理类
    用于管理微信支付相关的配置信息
    """
    
    def __init__(self):
        # 从环境变量或配置文件读取配置
        self.app_id = os.getenv('WECHAT_APP_ID', 'wx1234567890abcdef')  # 默认测试应用ID
        self.mch_id = os.getenv('WECHAT_MCH_ID', '1234567890')  # 商户号
        self.api_key = os.getenv('WECHAT_API_KEY', 'your_api_key_32_characters_long')  # API密钥
        self.notify_url = os.getenv('WECHAT_NOTIFY_URL', 'http://8.141.2.166:8000/api/v1/payments/wechat/notify')  # 微信异步通知地址
        self.trade_type = 'APP'  # 交易类型，APP支付
        self.sign_type = 'MD5'  # 签名类型
        
        # 微信支付API地址
        self.gateway_url = 'https://api.mch.weixin.qq.com/pay/unifiedorder'  # 统一下单接口
        self.query_url = 'https://api.mch.weixin.qq.com/pay/orderquery'  # 查询订单接口
        
    def get_app_id(self) -> str:
        """获取应用ID"""
        return self.app_id
    
    def get_mch_id(self) -> str:
        """获取商户号"""
        return self.mch_id
    
    def get_api_key(self) -> str:
        """获取API密钥"""
        if not self.api_key:
            raise ValueError("微信支付API密钥未配置")
        return self.api_key
    
    def get_notify_url(self) -> str:
        """获取异步通知地址"""
        return self.notify_url
    
    def get_trade_type(self) -> str:
        """获取交易类型"""
        return self.trade_type
    
    def get_sign_type(self) -> str:
        """获取签名类型"""
        return self.sign_type
    
    def get_gateway_url(self) -> str:
        """获取统一下单接口地址"""
        return self.gateway_url
    
    def get_query_url(self) -> str:
        """获取查询订单接口地址"""
        return self.query_url
    
    def is_sandbox(self) -> bool:
        """判断是否为沙箱环境"""
        return 'sandbox' in self.gateway_url.lower() or self.app_id.startswith('wx1234')
    
    def validate_config(self) -> bool:
        """验证配置是否完整"""
        required_fields = [
            self.app_id,
            self.mch_id,
            self.api_key,
            self.notify_url
        ]
        
        for field in required_fields:
            if not field or field.strip() == '':
                return False
        
        # 检查API密钥长度（微信要求32位）
        if len(self.api_key) != 32:
            return False
            
        return True

# 创建全局配置实例
wechat_config = WechatConfig()
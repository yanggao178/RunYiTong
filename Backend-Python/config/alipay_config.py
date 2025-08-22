import os
from typing import Optional

class AlipayConfig:
    """
    支付宝配置管理类
    用于管理支付宝支付相关的配置信息
    """
    
    def __init__(self):
        # 从环境变量或配置文件读取配置
        self.app_id = os.getenv('ALIPAY_APP_ID', '2021000000000000')  # 默认为沙箱环境
        self.private_key = os.getenv('ALIPAY_PRIVATE_KEY', 'MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCEvGFCAjFZPBgyiqOegEPniKj0tKpUJQeyJkPZJPcAf30poVEQo9xJzPHHHrr/DuISWb8jfodHXqdJDuy0Tl3dvLxQCThq8I1eVsalY4TKGpwcmYQTJVW+hS4gorYlYjPouFO4FBpz5RPPzzccHfCOp+Yt5tlYjtLYgBfg3dGhwksi7OEsF8LPGmEtEOUQH3W/dryhO1LlE5BB6VUSdPCGxeTTyOaAkzeJ8g7n6mCeZrgKWLnEVQhrVuH5AESDPzJXFbXrzOEly+xbP+PZVUUVtCXBrDmXjw/XSAqO8KAileZqyDFq5WfttCPkUxVfsOXdryJOnqv+8Q0YBA1N8uElAgMBAAECggEAS45TqlhHMO1VNMfYFSwb+xq+WfTRE+60L8M6UcuJ0j2/yEesNlWf2l6PrfGfLfqR0zJE6/ZlGMoXXS3irUwCgGqjosds1uqw/edVUeWRO93jyR/Vn3RcF4QF2svj9OmudKUf+qNu31OhcGQakaW1Pe7yxmQmihNaj6Rycyyw3Gs8wySYOpszNalYLn3+zhNA+EDkhKD8LKVdHcrasO8CtoFdkWJ/CPWZhvSjhuL/J8YhenZg8P492kvwa5Mwdghkrak/c/Du+ur/ZLwhWHzQcGTGRtlJDoiav616gSJiTbI4RJwqC+/lKtu9ICFRMrzgy2nbNiB+wYIHmF7elNaQQQKBgQDuKFh1ozo5u7gM9PTxEMOZPqRubWqYC/LmDrQuljch1vfThD0bztFjCovYuBvrUwsmVrthrdVHAc36y5GoS4t4EgkOgIHQeT3T5pPwjeENPLCVlhvE+IdiJgDUaNINW3Gt4rm01l7VIjD2L9JBhYcqQN8wV9PvPrY/Ckw7RxbZUQKBgQCOriM5GFAhkKHFwgVI5CKvFa45jM2IivRyC1nt4RXyRzjhLg6ubpyOq18/+4oKThZB9sHAwbvpdQ0dcsIarOs7d4JA2KRi0wMHIVFmrlGa3rjQV7S3hEMlS2JpogbcZYqiggdKVxgkzM9fD11VTZ8d2Fh3pIq0nLRs9lk65/bVlQKBgQCoAC/+tCmDxodcJISRdWj1tnnaKDdUkMFFRsQWRQMZTQK/4/4Tmr6cZjkmpSGUzJ0F65L9odnc4EoEjAvxM3FsPxTCdSaeAJj73SEpRXynkNNkgmKOfV9LflGBhXv/zi0QI+sqxTpakWcGk893RxGFXgPTn3EyyQNYzxPdkRVuMQKBgAlLRJYahFW7Yx5LpiZ6XdwD0IaFKnpDeurW3HPHXrOaehUI+AHayK3ucdTsILDNJ1wCo9pXhFMPwdvyNrygGO6VuVFaC1M4pZDYVcIXhs9MxMMlg2sU3X7Xa0CwQjwnDSHK0IjCJZt4D+YETTMaTEwYZKLef/6F2qbiYSuaIpUFAoGBAIQjwVst1XnOyjrjAYseeb3NGth0hqnyxPkUGr4h0hLGZuEP054GhzA2t9tt81ppmhGFsN8this0H+3DY7R5Vqww15Ksuhrl3D8wrlj2sjfZKUrzHFJoAA9CSLJLfVV18bCuI5E4sxJvqrdhBhwTve/JKLtsxLVlR+hrNyf9m7dg')
        self.alipay_public_key = os.getenv('ALIPAY_PUBLIC_KEY', 'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAj0ZlHfjNYf18XbrnfHeUXNZEM56lVFAuryVUkNDfiyRfJjqO4Lc+mzc5eomi5Dha8wJmWNYFdGX3zptwHt2HTIQh1X7wW7uwclk8NWtzs4xA04VafMDN/fMMmwGSqLr9Ldw+vUnoYRmC1vOjGo8yQtISDS+v48HQN80H+oleNRCzVQd9rgoriqptmlIYB5ykR88eka3ItsOq11IxjGNMlHbx51hPSQB2iqBAdBNtcQa8t38XjdY84Pl/F1dsREtM28JHmFEm6jmMG6cyKkTeeOv6pW4zVbt/HP03yT6UgCqylBrpl2n1FIGkFOkO7gY3dA4jBNyrw0Z/C1icEkn9dwIDAQAB')
        self.sign_type = 'RSA2'
        self.charset = 'utf-8'
        self.gateway_url = os.getenv('ALIPAY_GATEWAY_URL', 'https://openapi.alipaydev.com/gateway.do')  # 沙箱环境
        self.notify_url = os.getenv('ALIPAY_NOTIFY_URL', 'http://8.141.2.166:8000/api/v1/payments/alipay/notify')  # 支付宝异步通知地址
        
    def get_app_id(self) -> str:
        """获取应用ID"""
        return self.app_id
    
    def get_private_key(self) -> str:
        """获取私钥"""
        if not self.private_key:
            raise ValueError("支付宝私钥未配置，请设置环境变量 ALIPAY_PRIVATE_KEY")
        return self.private_key
    
    def get_alipay_public_key(self) -> str:
        """获取支付宝公钥"""
        if not self.alipay_public_key:
            raise ValueError("支付宝公钥未配置，请设置环境变量 ALIPAY_PUBLIC_KEY")
        
        # 导入签名工具类来格式化公钥
        from ..utils.alipay_signature import AlipaySignature
        return AlipaySignature.format_public_key(self.alipay_public_key)
    
    def get_sign_type(self) -> str:
        """获取签名类型"""
        return self.sign_type
    
    def get_charset(self) -> str:
        """获取字符集"""
        return self.charset
    
    def get_gateway_url(self) -> str:
        """获取网关地址"""
        return self.gateway_url
    
    def get_notify_url(self) -> str:
        """获取异步通知地址"""
        return self.notify_url
    
    def is_sandbox(self) -> bool:
        """判断是否为沙箱环境"""
        return 'alipaydev.com' in self.gateway_url

# 全局配置实例
alipay_config = AlipayConfig()
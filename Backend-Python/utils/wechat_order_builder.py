import requests
import time
from typing import Dict, Any, Optional, Tuple
import logging
from decimal import Decimal

from config.wechat_config import wechat_config
from utils.wechat_signature import WechatSignature

# 配置日志
logger = logging.getLogger(__name__)

class WechatOrderBuilder:
    """
    微信支付订单构建器
    负责构建微信支付订单信息和处理支付请求
    """
    
    def __init__(self):
        self.config = wechat_config
        self.signature = WechatSignature()
    
    def generate_out_trade_no(self, product_id: int) -> str:
        """
        生成商户订单号
        
        Args:
            product_id: 商品ID
            
        Returns:
            商户订单号
        """
        timestamp = str(int(time.time()))
        return f"WX{product_id}_{timestamp}"
    
    def yuan_to_fen(self, yuan_amount: float) -> int:
        """
        将元转换为分
        
        Args:
            yuan_amount: 金额（元）
            
        Returns:
            金额（分）
        """
        return int(Decimal(str(yuan_amount)) * 100)
    
    def create_unified_order(self, product_id: int, product_name: str, 
                           product_price: float, quantity: int = 1,
                           user_ip: str = '127.0.0.1') -> Tuple[bool, Dict[str, Any]]:
        """
        创建微信统一下单
        
        Args:
            product_id: 商品ID
            product_name: 商品名称
            product_price: 商品价格（元）
            quantity: 购买数量
            user_ip: 用户IP地址
            
        Returns:
            (是否成功, 响应数据)
        """
        try:
            # 生成订单号
            out_trade_no = self.generate_out_trade_no(product_id)
            
            # 计算总金额（分）
            total_fee = self.yuan_to_fen(product_price * quantity)
            
            # 构建商品描述
            body = f"{product_name}" if len(product_name) <= 32 else f"{product_name[:29]}..."
            
            # 创建统一下单参数
            params = self.signature.create_prepay_params(
                app_id=self.config.get_app_id(),
                mch_id=self.config.get_mch_id(),
                api_key=self.config.get_api_key(),
                out_trade_no=out_trade_no,
                total_fee=total_fee,
                body=body,
                notify_url=self.config.get_notify_url(),
                trade_type=self.config.get_trade_type(),
                spbill_create_ip=user_ip
            )
            
            # 转换为XML
            xml_data = self.signature.dict_to_xml(params)
            
            logger.info(f"微信统一下单请求: {out_trade_no}")
            logger.debug(f"请求参数: {params}")
            
            # 发送请求
            response = requests.post(
                self.config.get_gateway_url(),
                data=xml_data.encode('utf-8'),
                headers={'Content-Type': 'application/xml; charset=utf-8'},
                timeout=30
            )
            
            if response.status_code != 200:
                logger.error(f"微信统一下单HTTP错误: {response.status_code}")
                return False, {'error': f'HTTP请求失败: {response.status_code}'}
            
            # 解析响应
            response_dict = self.signature.xml_to_dict(response.text)
            logger.debug(f"微信统一下单响应: {response_dict}")
            
            # 检查返回状态
            if response_dict.get('return_code') != 'SUCCESS':
                error_msg = response_dict.get('return_msg', '未知错误')
                logger.error(f"微信统一下单失败: {error_msg}")
                return False, {'error': f'统一下单失败: {error_msg}'}
            
            if response_dict.get('result_code') != 'SUCCESS':
                error_code = response_dict.get('err_code', '')
                error_msg = response_dict.get('err_code_des', '未知错误')
                logger.error(f"微信统一下单业务失败: {error_code} - {error_msg}")
                return False, {'error': f'业务处理失败: {error_msg}'}
            
            # 验证签名
            if not self.signature.verify_sign(response_dict, self.config.get_api_key()):
                logger.error("微信统一下单响应签名验证失败")
                return False, {'error': '响应签名验证失败'}
            
            # 获取预支付交易会话ID
            prepay_id = response_dict.get('prepay_id')
            if not prepay_id:
                logger.error("微信统一下单未返回prepay_id")
                return False, {'error': '未获取到预支付ID'}
            
            # 生成APP支付参数
            app_pay_params = self.signature.create_app_pay_params(
                app_id=self.config.get_app_id(),
                mch_id=self.config.get_mch_id(),
                prepay_id=prepay_id,
                api_key=self.config.get_api_key()
            )
            
            logger.info(f"微信订单创建成功: {out_trade_no}")
            
            return True, {
                'out_trade_no': out_trade_no,
                'prepay_id': prepay_id,
                'app_pay_params': app_pay_params,
                'total_fee': total_fee,
                'product_info': {
                    'id': product_id,
                    'name': product_name,
                    'price': product_price,
                    'quantity': quantity
                }
            }
            
        except requests.RequestException as e:
            logger.error(f"微信统一下单网络错误: {str(e)}")
            return False, {'error': f'网络请求失败: {str(e)}'}
        except Exception as e:
            logger.error(f"微信统一下单异常: {str(e)}")
            return False, {'error': f'订单创建失败: {str(e)}'}
    
    def query_order(self, out_trade_no: str) -> Tuple[bool, Dict[str, Any]]:
        """
        查询微信支付订单状态
        
        Args:
            out_trade_no: 商户订单号
            
        Returns:
            (是否成功, 订单信息)
        """
        try:
            # 构建查询参数
            params = {
                'appid': self.config.get_app_id(),
                'mch_id': self.config.get_mch_id(),
                'out_trade_no': out_trade_no,
                'nonce_str': self.signature.generate_nonce_str()
            }
            
            # 生成签名
            params['sign'] = self.signature.create_sign(params, self.config.get_api_key())
            
            # 转换为XML
            xml_data = self.signature.dict_to_xml(params)
            
            logger.info(f"查询微信订单: {out_trade_no}")
            
            # 发送请求
            response = requests.post(
                self.config.get_query_url(),
                data=xml_data.encode('utf-8'),
                headers={'Content-Type': 'application/xml; charset=utf-8'},
                timeout=30
            )
            
            if response.status_code != 200:
                logger.error(f"查询微信订单HTTP错误: {response.status_code}")
                return False, {'error': f'HTTP请求失败: {response.status_code}'}
            
            # 解析响应
            response_dict = self.signature.xml_to_dict(response.text)
            logger.debug(f"查询微信订单响应: {response_dict}")
            
            # 检查返回状态
            if response_dict.get('return_code') != 'SUCCESS':
                error_msg = response_dict.get('return_msg', '未知错误')
                logger.error(f"查询微信订单失败: {error_msg}")
                return False, {'error': f'查询失败: {error_msg}'}
            
            if response_dict.get('result_code') != 'SUCCESS':
                error_code = response_dict.get('err_code', '')
                error_msg = response_dict.get('err_code_des', '未知错误')
                logger.warning(f"查询微信订单业务失败: {error_code} - {error_msg}")
                return False, {'error': f'业务查询失败: {error_msg}', 'err_code': error_code}
            
            # 验证签名
            if not self.signature.verify_sign(response_dict, self.config.get_api_key()):
                logger.error("查询微信订单响应签名验证失败")
                return False, {'error': '响应签名验证失败'}
            
            return True, response_dict
            
        except requests.RequestException as e:
            logger.error(f"查询微信订单网络错误: {str(e)}")
            return False, {'error': f'网络请求失败: {str(e)}'}
        except Exception as e:
            logger.error(f"查询微信订单异常: {str(e)}")
            return False, {'error': f'查询订单失败: {str(e)}'}
    
    def build_order_info_string(self, app_pay_params: Dict[str, str]) -> str:
        """
        构建用于Android客户端的订单信息字符串
        
        Args:
            app_pay_params: APP支付参数
            
        Returns:
            订单信息字符串
        """
        # 按照微信支付要求的格式构建
        order_parts = []
        for key, value in app_pay_params.items():
            order_parts.append(f'{key}={value}')
        
        return '&'.join(order_parts)
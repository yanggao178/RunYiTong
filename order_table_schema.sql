-- 根据Order.java模型类生成的orders表结构，与项目规范保持一致
CREATE TABLE IF NOT EXISTS orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id TEXT NOT NULL,
    user_id INTEGER NOT NULL,
    product_name TEXT NOT NULL,
    status TEXT NOT NULL,
    price TEXT NOT NULL,
    create_time TEXT,
    pay_time TEXT,
    shipping_address TEXT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- 外键约束
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 添加常用的索引
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_order_id ON orders(order_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);

-- 注释说明各字段对应Order.java中的属性
-- id: 对应Order.id (int类型)
-- order_id: 对应Order.orderId (String类型)
-- user_id: 对应Order.userId (int类型)
-- product_name: 对应Order.productName (String类型)
-- status: 对应Order.status (String类型)
-- price: 对应Order.price (String类型)
-- create_time: 对应Order.createTime (String类型)
-- pay_time: 对应Order.payTime (String类型)
-- shipping_address: 对应Order.shippingAddress (String类型)
-- created_time: 记录创建时间（项目常用字段，与models.py保持一致）
-- updated_time: 记录更新时间（项目常用字段，与models.py保持一致）
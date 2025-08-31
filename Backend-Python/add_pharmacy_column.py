import sqlite3
import os

# 连接到ai_medical.db数据库
db_path = 'ai_medical.db'

if os.path.exists(db_path):
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    # 检查pharmacy_name字段是否已存在
    cursor.execute('PRAGMA table_info(products)')
    columns = cursor.fetchall()
    column_names = [col[1] for col in columns]
    
    if 'pharmacy_name' not in column_names:
        # 添加pharmacy_name字段
        cursor.execute('ALTER TABLE products ADD COLUMN pharmacy_name VARCHAR(200)')
        print("✓ 成功添加pharmacy_name字段到products表")
        
        # 为现有数据填充pharmacy_name字段（从manufacturer字段复制）
        cursor.execute('UPDATE products SET pharmacy_name = manufacturer')
        conn.commit()
        print("✓ 已从manufacturer字段复制数据到pharmacy_name字段")
    else:
        print("✓ pharmacy_name字段已存在于products表中")
    
    # 查看更新后的表结构
    print("\n更新后的Products表结构:")
    cursor.execute('PRAGMA table_info(products)')
    for row in cursor.fetchall():
        if row[1] == 'pharmacy_name':
            print(f"  {row}")
    
    # 查看一条示例数据，验证pharmacy_name字段
    cursor.execute('SELECT id, name, manufacturer, pharmacy_name FROM products LIMIT 1')
    sample = cursor.fetchone()
    if sample:
        print("\n示例数据（验证pharmacy_name字段）:")
        print(f"  商品ID: {sample[0]}")
        print(f"  商品名称: {sample[1]}")
        print(f"  厂商: {sample[2]}")
        print(f"  药店名: {sample[3]}")
    
    conn.close()
else:
    print(f"❌ 数据库文件 {db_path} 不存在")
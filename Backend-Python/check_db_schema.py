import sqlite3
import os

# 连接到ai_medical.db数据库
db_path = 'ai_medical.db'
if os.path.exists(db_path):
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    # 查看products表结构
    print("Products table schema:")
    cursor.execute('PRAGMA table_info(products)')
    for row in cursor.fetchall():
        print(f"  {row}")
    
    # 查看一条示例数据
    print("\nSample product data:")
    cursor.execute('SELECT * FROM products LIMIT 1')
    sample = cursor.fetchone()
    if sample:
        print(f"  {sample}")
    
    conn.close()
else:
    print(f"Database file {db_path} not found")
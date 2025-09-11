import os
import sqlite3

# 获取AI数据库路径
db_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'ai_medical.db')
print(f"AI数据库路径: {db_path}")

conn = sqlite3.connect(db_path)
cursor = conn.cursor()

# 检查各个表的外键约束
tables = ['appointments', 'prescriptions', 'health_records', 'addresses', 'orders', 'identity_verifications']
for table in tables:
    try:
        cursor.execute(f'PRAGMA foreign_key_list({table})')
        fks = cursor.fetchall()
        if fks:
            print(f'{table}表的外键约束:')
            for fk in fks:
                print(f'  {fk}')
    except Exception as e:
        print(f'检查{table}表时出错: {e}')

conn.close()
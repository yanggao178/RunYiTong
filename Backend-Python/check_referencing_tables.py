import os
import sqlite3

# 获取CMS数据库路径
db_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'cms_medical.db')
print(f"CMS数据库路径: {db_path}")

conn = sqlite3.connect(db_path)
cursor = conn.cursor()

# 查找引用users的表
cursor.execute('SELECT name, sql FROM sqlite_master WHERE type="table" AND sql LIKE "%users%"')
tables = cursor.fetchall()
print('引用users的表:')
for table in tables:
    print(f'{table[0]}: {table[1]}')

conn.close()
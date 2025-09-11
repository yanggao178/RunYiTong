import os
import sqlite3

# 获取CMS数据库路径
db_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'cms_medical.db')
print(f"CMS数据库路径: {db_path}")

conn = sqlite3.connect(db_path)
cursor = conn.cursor()

# 检查users表的外键约束
cursor.execute('PRAGMA foreign_key_list(users)')
fks = cursor.fetchall()
print('CMS users表的外键约束:')
for fk in fks:
    print(fk)

# 检查auth_user表的外键约束
cursor.execute('PRAGMA foreign_key_list(auth_user)')
fks = cursor.fetchall()
print('\nCMS auth_user表的外键约束:')
for fk in fks:
    print(fk)

conn.close()
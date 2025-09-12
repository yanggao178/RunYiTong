import sqlite3

# 连接到数据库
conn = sqlite3.connect('ai_medical.db')
cursor = conn.cursor()

# 查询hospitals表的结构
table_name = 'hospitals'
cursor.execute(f"PRAGMA table_info({table_name})")
columns = cursor.fetchall()

print(f"表 {table_name} 的结构：")
print("字段名称\t字段类型\t是否可为空\t默认值\t是否主键")
print("-" * 60)

for column in columns:
    cid, name, type_, notnull, dflt_value, pk = column
    notnull_text = "NOT NULL" if notnull else "NULL"
    pk_text = "PRIMARY KEY" if pk else ""
    default_text = dflt_value if dflt_value is not None else ""
    print(f"{name}\t{type_}\t{notnull_text}\t{default_text}\t{pk_text}")

# 关闭连接
conn.close()
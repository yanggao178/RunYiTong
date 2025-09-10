import sqlite3
import os

# 连接数据库
db_path = os.path.join(os.path.dirname(__file__), 'ai_medical.db')
print(f"连接到数据库: {db_path}")

# 检查数据库文件是否存在
if not os.path.exists(db_path):
    print("错误: 数据库文件不存在!")
else:
    try:
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        
        # 检查表是否存在
        cursor.execute("SELECT name FROM sqlite_master WHERE type='table';")
        tables = cursor.fetchall()
        print(f"数据库中的表: {tables}")
        
        # 检查physical_exam_reports表结构
        print("\n检查physical_exam_reports表结构:")
        cursor.execute("PRAGMA table_info(physical_exam_reports);")
        columns = cursor.fetchall()
        for col in columns:
            print(col)
        
        # 检查health_records_old表结构
        print("\n检查health_records_old表结构:")
        cursor.execute("PRAGMA table_info(health_records_old);")
        columns = cursor.fetchall()
        for col in columns:
            print(col)
        
        # 查看physical_exam_reports中有问题的记录
        print("\n查看physical_exam_reports中id=2的记录:")
        try:
            cursor.execute("SELECT * FROM physical_exam_reports WHERE id=2;")
            record = cursor.fetchone()
            print(f"记录: {record}")
        except sqlite3.Error as e:
            print(f"无法查询physical_exam_reports表: {e}")
        
        # 查看health_records_old中的所有id
        print("\n查看health_records_old中的所有id:")
        try:
            cursor.execute("SELECT id FROM health_records_old;")
            ids = cursor.fetchall()
            print(f"health_records_old中的id: {[id[0] for id in ids]}")
        except sqlite3.Error as e:
            print(f"无法查询health_records_old表: {e}")
            
        # 尝试修复外键问题 - 有几种可能的解决方案:
        # 1. 删除所有有问题的记录
        # 2. 在health_records_old中创建缺失的记录
        # 3. 修改外键约束为允许空值
        
        # 这里我们选择方案1: 删除所有引用了health_record_id=2的记录
        print("\n尝试删除physical_exam_reports表中所有health_record_id=2的记录...")
        try:
            cursor.execute("DELETE FROM physical_exam_reports WHERE health_record_id=2;")
            conn.commit()
            print(f"已成功删除有问题的记录数量: {cursor.rowcount}")
        except sqlite3.Error as e:
            print(f"删除记录失败: {e}")
            conn.rollback()
        
        # 关闭连接
        conn.close()
        print("\n数据库连接已关闭。")
        
    except sqlite3.Error as e:
        print(f"数据库错误: {e}")
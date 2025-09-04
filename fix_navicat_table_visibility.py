import sqlite3
import os
import shutil
import time
import platform

def check_addresses_table():
    """检查addresses表是否存在并显示其内容"""
    try:
        conn = sqlite3.connect('ai_medical.db')
        cursor = conn.cursor()
        
        # 检查表是否存在
        cursor.execute("SELECT name FROM sqlite_master WHERE type='table' AND name='addresses';")
        table_exists = cursor.fetchone() is not None
        
        if table_exists:
            print("✅ addresses表确实存在于数据库中！")
            
            # 查看表结构
            print("\n表结构：")
            cursor.execute("PRAGMA table_info(addresses);")
            columns = cursor.fetchall()
            for col in columns:
                print(f"- {col[1]} ({col[2]}){'- 主键' if col[5] else ''}")
            
            # 统计记录数
            cursor.execute("SELECT COUNT(*) FROM addresses;")
            count = cursor.fetchone()[0]
            print(f"\naddresses表中的记录数: {count}")
            
            # 如果有记录，显示前几条
            if count > 0:
                print("\naddresses表中的前3条记录：")
                cursor.execute("SELECT id, user_id, name, phone, is_default FROM addresses LIMIT 3;")
                records = cursor.fetchall()
                for record in records:
                    print(f"- ID: {record[0]}, 用户ID: {record[1]}, 姓名: {record[2]}, 电话: {record[3]}, 默认地址: {record[4]}")
        else:
            print("❌ addresses表不存在于数据库中。")
        
    except sqlite3.Error as e:
        print(f"数据库错误: {e}")
    finally:
        if conn:
            conn.close()

def copy_database(new_location=None):
    """复制数据库文件到新位置"""
    source = 'ai_medical.db'
    
    if not os.path.exists(source):
        print(f"错误: 源数据库文件 '{source}' 不存在。")
        return False
    
    # 如果没有指定新位置，使用桌面
    if new_location is None:
        if platform.system() == 'Windows':
            new_location = os.path.join(os.path.expanduser('~'), 'Desktop', f'ai_medical_copy_{int(time.time())}.db')
        else:
            new_location = os.path.join(os.path.expanduser('~'), f'ai_medical_copy_{int(time.time())}.db')
    
    try:
        shutil.copy2(source, new_location)
        print(f"\n✅ 数据库已成功复制到: {new_location}")
        print("请在Navicat中连接这个复制后的数据库文件，查看是否能看到所有表。")
        return True
    except Exception as e:
        print(f"复制数据库时出错: {e}")
        return False

def main():
    print("=" * 60)
    print("     Navicat SQLite表可见性问题解决工具")
    print("=" * 60)
    
    # 检查数据库文件
    if not os.path.exists('ai_medical.db'):
        print("错误: 找不到ai_medical.db数据库文件。")
        return
    
    # 检查addresses表
    check_addresses_table()
    
    # 显示权限信息
    print("\n" + "=" * 60)
    print("数据库文件权限检查")
    print("=" * 60)
    file_stats = os.stat('ai_medical.db')
    print(f"文件路径: {os.path.abspath('ai_medical.db')}")
    print(f"文件大小: {file_stats.st_size} 字节")
    print(f"可读权限: {'✅' if os.access('ai_medical.db', os.R_OK) else '❌'}")
    print(f"可写权限: {'✅' if os.access('ai_medical.db', os.W_OK) else '❌'}")
    
    # 提供复制数据库选项
    print("\n" + "=" * 60)
    print("解决Navicat无法查看表的问题")
    print("=" * 60)
    print("以下是推荐的解决步骤：")
    print("1. 以管理员身份运行Navicat")
    print("2. 在Navicat中刷新数据库连接")
    print("3. 检查数据库文件的Windows安全权限")
    print("4. 尝试连接复制后的数据库文件")
    
    choice = input("\n是否需要复制数据库文件到桌面？(y/n): ")
    if choice.lower() == 'y':
        copy_database()
    
    print("\n" + "=" * 60)
    print("附加提示")
    print("=" * 60)
    print("- 如果上述方法无效，请尝试使用其他SQLite工具，如DB Browser for SQLite或SQLite Studio")
    print("- 确保您使用的是最新版本的Navicat")
    print("- 检查是否有其他程序正在占用数据库文件")
    print("- 可以尝试在Navicat中重新创建数据库连接")

if __name__ == "__main__":
    main()
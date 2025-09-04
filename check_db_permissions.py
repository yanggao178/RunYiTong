import os
import stat
import platform

# 检查操作系统
system = platform.system()
print(f"当前操作系统: {system}")

# 数据库文件路径
db_path = 'ai_medical.db'

# 检查文件是否存在
if not os.path.exists(db_path):
    print(f"错误: 数据库文件 '{db_path}' 不存在。")
else:
    # 获取文件权限信息
    file_stats = os.stat(db_path)
    
    print(f"\n数据库文件: {os.path.abspath(db_path)}")
    
    # 在Windows系统上显示基本权限信息
    if system == 'Windows':
        print(f"文件大小: {file_stats.st_size} 字节")
        print(f"创建时间: {file_stats.st_ctime}")
        print(f"修改时间: {file_stats.st_mtime}")
        print(f"访问时间: {file_stats.st_atime}")
        
        # 检查文件是否可读写
        is_readable = os.access(db_path, os.R_OK)
        is_writable = os.access(db_path, os.W_OK)
        
        print(f"\n文件权限:")
        print(f"- 可读: {'✅' if is_readable else '❌'}")
        print(f"- 可写: {'✅' if is_writable else '❌'}")
        
        # 检查文件属性
        file_attributes = stat.filemode(file_stats.st_mode)
        print(f"文件属性: {file_attributes}")
    else:
        # Linux/Mac系统的权限显示
        print(f"文件权限: {stat.filemode(file_stats.st_mode)}")
        print(f"所有者ID: {file_stats.st_uid}")
        print(f"组ID: {file_stats.st_gid}")

# Navicat权限问题解决方案
solutions = """
\n\n# Navicat无法查看表的权限问题解决方案

在Windows系统上，解决Navicat查看表权限问题的步骤：

1. **以管理员身份运行Navicat**
   - 右键点击Navicat快捷方式
   - 选择"以管理员身份运行"
   - 重新连接数据库查看是否能看到表

2. **检查文件权限设置**
   - 右键点击ai_medical.db文件
   - 选择"属性" > "安全"选项卡
   - 确保当前用户有"完全控制"或至少"读取"和"写入"权限
   - 如果没有，点击"编辑"按钮添加所需权限

3. **检查数据库文件锁定状态**
   - 确保没有其他程序正在使用该数据库文件
   - 关闭可能正在访问该数据库的程序（如Python脚本、其他数据库工具等）

4. **复制数据库文件到新位置**
   - 将ai_medical.db复制到另一个位置（如桌面）
   - 在Navicat中连接复制后的文件，查看是否能看到所有表

5. **使用SQLite命令行工具验证**
   - 打开命令提示符
   - 运行: sqlite3 ai_medical.db
   - 在SQLite提示符下运行: .tables
   - 这将显示数据库中所有表的列表，验证表是否存在

6. **更新Navicat到最新版本**
   - 旧版本的Navicat可能存在兼容性问题
   - 下载并安装最新版本的Navicat

7. **尝试使用其他SQLite工具**
   - 可以尝试使用SQLite Studio、DB Browser for SQLite等免费工具
   - 这些工具可能对文件权限有更宽松的处理方式
"""

print(solutions)
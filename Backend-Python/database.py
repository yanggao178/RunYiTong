from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
import os
from dotenv import load_dotenv

# 加载环境变量
load_dotenv()

# 数据库URL配置
# 默认使用SQLite，可以通过环境变量配置MySQL
DATABASE_URL = os.getenv(
    "DATABASE_URL", 
    "sqlite:///./ai_medical.db"
)

# 如果使用MySQL，URL格式如下：
# DATABASE_URL = "mysql+pymysql://username:password@localhost/ai_medical"

# 创建数据库引擎
if DATABASE_URL.startswith("sqlite"):
    engine = create_engine(
        DATABASE_URL, 
        connect_args={"check_same_thread": False}
    )
else:
    engine = create_engine(DATABASE_URL)

# 创建会话工厂
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# 创建基类
Base = declarative_base()

# 依赖注入：获取数据库会话
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
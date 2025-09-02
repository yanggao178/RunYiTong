from sqlalchemy.orm import Session
from database import SessionLocal, engine
from models import Base, Product, Book, User, HealthRecord
from schemas import ProductCreate, BookCreate, UserCreate
from routers.users import get_password_hash
from datetime import datetime

def init_database():
    """初始化数据库和示例数据"""
    # 删除所有表并重新创建
    Base.metadata.drop_all(bind=engine)
    Base.metadata.create_all(bind=engine)
    
    db = SessionLocal()
    
    try:
        # 初始化商品数据
        init_products(db)
        
        # 初始化图书数据
        init_books(db)
        
        # 初始化用户数据
        init_users(db)
        
        print("数据库初始化完成！")
        
    except Exception as e:
        print(f"初始化数据时出错: {e}")
        db.rollback()
    finally:
        db.close()

def init_products(db: Session):
    """初始化商品数据"""
    products_data = [
        # 保健品类
        {
            "name": "养生茶",
            "price": 98.0,
            "description": "纯天然草本配方，滋阴补肾",
            "featured_image_url": "https://example.com/tea.jpg",
            "category_id": 1,
            "stock_quantity": 100,
            "manufacturer": "康健药业",
            "sales_count": 156
        },
        {
            "name": "艾灸贴",
            "price": 68.0,
            "description": "缓解疲劳，促进血液循环",
            "featured_image_url": "https://example.com/moxibustion.jpg",
            "category_id": 1,
            "stock_quantity": 200,
            "manufacturer": "中医堂",
            "sales_count": 89
        },
        {
            "name": "按摩仪",
            "price": 199.0,
            "description": "智能按摩，舒缓肌肉紧张",
            "featured_image_url": "https://example.com/massager.jpg",
            "category_id": 1,
            "stock_quantity": 50,
            "manufacturer": "健康科技",
            "sales_count": 234
        },
        {
            "name": "中药饮片",
            "price": 128.0,
            "description": "精选中药材，调理身体",
            "featured_image_url": "https://example.com/herb.jpg",
            "category_id": 1,
            "stock_quantity": 80,
            "manufacturer": "同仁堂",
            "sales_count": 67
        },
        {
            "name": "蜂胶胶囊",
            "price": 158.0,
            "description": "天然蜂胶提取，增强免疫力",
            "featured_image_url": "https://example.com/propolis.jpg",
            "category_id": 1,
            "stock_quantity": 120,
            "manufacturer": "蜂之语",
            "sales_count": 203
        },
        {
            "name": "维生素C片",
            "price": 45.0,
            "description": "补充维生素C，提高抵抗力",
            "featured_image_url": "https://example.com/vitamin_c.jpg",
            "category_id": 1,
            "stock_quantity": 300,
            "manufacturer": "汤臣倍健",
            "sales_count": 567
        },
        {
            "name": "钙片",
            "price": 78.0,
            "description": "高钙配方，强健骨骼",
            "featured_image_url": "https://example.com/calcium.jpg",
            "category_id": 1,
            "stock_quantity": 180,
            "manufacturer": "钙尔奇",
            "sales_count": 345
        },
        {
            "name": "鱼油软胶囊",
            "price": 188.0,
            "description": "深海鱼油，保护心血管健康",
            "featured_image_url": "https://example.com/fish_oil.jpg",
            "category_id": 1,
            "stock_quantity": 90,
            "manufacturer": "挪威小鱼",
            "sales_count": 278
        },
        
        # 医疗器械类
        {
            "name": "血压计",
            "price": 299.0,
            "description": "家用智能血压监测仪",
            "featured_image_url": "https://example.com/blood_pressure.jpg",
            "category_id": 2,
            "stock_quantity": 30,
            "manufacturer": "欧姆龙",
            "sales_count": 145
        },
        {
            "name": "血糖仪",
            "price": 168.0,
            "description": "便携式血糖检测仪，操作简单",
            "featured_image_url": "https://example.com/glucose_meter.jpg",
            "category_id": 2,
            "stock_quantity": 45,
            "manufacturer": "强生",
            "sales_count": 89
        },
        {
            "name": "体温计",
            "price": 58.0,
            "description": "红外线额温枪，快速测温",
            "featured_image_url": "https://example.com/thermometer.jpg",
            "category_id": 2,
            "stock_quantity": 200,
            "manufacturer": "博朗",
            "sales_count": 456
        },
        {
            "name": "制氧机",
            "price": 1299.0,
            "description": "家用制氧机，改善呼吸质量",
            "featured_image_url": "https://example.com/oxygen_concentrator.jpg",
            "category_id": 2,
            "stock_quantity": 15,
            "manufacturer": "鱼跃医疗",
            "sales_count": 67
        },
        {
            "name": "雾化器",
            "price": 189.0,
            "description": "超声波雾化器，呼吸道护理",
            "featured_image_url": "https://example.com/nebulizer.jpg",
            "category_id": 2,
            "stock_quantity": 80,
            "manufacturer": "欧姆龙",
            "sales_count": 123
        },
        
        # 中药材类
        {
            "name": "人参片",
            "price": 268.0,
            "description": "长白山人参，大补元气",
            "featured_image_url": "https://example.com/ginseng.jpg",
            "category_id": 3,
            "stock_quantity": 60,
            "manufacturer": "长白山参业",
            "sales_count": 134
        },
        {
            "name": "枸杞子",
            "price": 88.0,
            "description": "宁夏枸杞，滋补肝肾",
            "featured_image_url": "https://example.com/goji.jpg",
            "category_id": 3,
            "stock_quantity": 150,
            "manufacturer": "宁夏红",
            "sales_count": 289
        },
        {
            "name": "当归片",
            "price": 78.0,
            "description": "甘肃当归，补血调经",
            "featured_image_url": "https://example.com/angelica.jpg",
            "category_id": 3,
            "stock_quantity": 90,
            "manufacturer": "陇西药材",
            "sales_count": 156
        },
        {
            "name": "黄芪片",
            "price": 65.0,
            "description": "内蒙古黄芪，补气固表",
            "featured_image_url": "https://example.com/astragalus.jpg",
            "category_id": 3,
            "stock_quantity": 110,
            "manufacturer": "内蒙古药材",
            "sales_count": 198
        },
        
        # 护理用品类
        {
            "name": "医用口罩",
            "price": 25.0,
            "description": "一次性医用外科口罩",
            "featured_image_url": "https://example.com/mask.jpg",
            "category_id": 1,
            "stock_quantity": 500,
            "manufacturer": "3M",
            "sales_count": 1234
        },
        {
            "name": "酒精消毒液",
            "price": 18.0,
            "description": "75%医用酒精，杀菌消毒",
            "featured_image_url": "https://example.com/alcohol.jpg",
            "category_id": 1,
            "stock_quantity": 300,
            "manufacturer": "海氏海诺",
            "sales_count": 789
        },
        {
            "name": "创可贴",
            "price": 12.0,
            "description": "防水透气创可贴",
            "featured_image_url": "https://example.com/bandaid.jpg",
            "category_id": 1,
            "stock_quantity": 400,
            "manufacturer": "邦迪",
            "sales_count": 567
        },
        {
            "name": "医用纱布",
            "price": 15.0,
            "description": "无菌医用纱布块",
            "featured_image_url": "https://example.com/gauze.jpg",
            "category_id": 1,
            "stock_quantity": 250,
            "manufacturer": "振德医疗",
            "sales_count": 345
        }
    ]
    
    for product_data in products_data:
        # 检查是否已存在
        existing = db.query(Product).filter(Product.name == product_data["name"]).first()
        if not existing:
            product = Product(**product_data)
            db.add(product)
    
    db.commit()
    print("商品数据初始化完成")

def init_books(db: Session):
    """初始化图书数据"""
    books_data = [
        # 中医古籍
        {
            "name": "黄帝内经",
            "author": "佚名",
            "category": "中医基础",
            "description": "中国最早的医学典籍，传统医学四大经典著作之一。",
            "cover_url": "https://example.com/huangdi.jpg",
            "pdf_file_path": "books/pdfs/huangdi_neijing.pdf",
            "file_size": 15728640,
            "publish_date": datetime.now()
        },
        {
            "name": "伤寒杂病论",
            "author": "张仲景",
            "category": "中医临床",
            "description": "确立了辨证论治原则，是中医临床的基本原则。",
            "cover_url": "https://example.com/shanghan.jpg",
            "pdf_file_path": "books/pdfs/shanghan_zabing_lun.pdf",
            "file_size": 12582912,
            "publish_date": datetime.now()
        },
        {
            "name": "神农本草经",
            "author": "佚名",
            "category": "中药学",
            "description": "中国现存最早的中药学著作。",
            "cover_url": "https://example.com/shennong.jpg",
            "pdf_file_path": "books/pdfs/shennong_bencao_jing.pdf",
            "file_size": 8388608,
            "publish_date": datetime.now()
        },
        {
            "name": "本草纲目",
            "author": "李时珍",
            "category": "中药学",
            "description": "集我国16世纪以前药学成就之大成。",
            "cover_url": "https://example.com/bencao.jpg",
            "pdf_file_path": "books/pdfs/bencao_gangmu.pdf",
            "file_size": 25165824,
            "publish_date": datetime.now()
        },
        {
            "name": "针灸甲乙经",
            "author": "皇甫谧",
            "category": "针灸学",
            "description": "中国现存最早的针灸学专著。",
            "cover_url": "https://example.com/zhenjiu.jpg",
            "pdf_file_path": "books/pdfs/zhenjiu_jiayijing.pdf",
            "file_size": 10485760,
            "publish_date": datetime.now()
        },
        
        # 西医经典
        {
            "name": "希波克拉底文集",
            "author": "希波克拉底",
            "category": "医学理论",
            "description": "西方医学的奠基之作。",
            "cover_url": "https://example.com/hippocrates.jpg",
            "pdf_file_path": "books/pdfs/hippocrates_collection.pdf",
            "file_size": 18874368,
            "publish_date": datetime.now()
        },
        {
            "name": "人体的构造",
            "author": "维萨里",
            "category": "解剖学",
            "description": "近代解剖学的奠基之作。",
            "cover_url": "https://example.com/structure.jpg",
            "pdf_file_path": "books/pdfs/human_structure.pdf",
            "file_size": 31457280,
            "publish_date": datetime.now()
        },
        {
            "name": "内科学原理与实践",
            "author": "奥斯勒",
            "category": "内科学",
            "description": "现代内科学的奠基之作。",
            "cover_url": "https://example.com/internal.jpg",
            "pdf_file_path": "books/pdfs/internal_medicine.pdf",
            "file_size": 41943040,
            "publish_date": datetime.now()
        },
        {
            "name": "细胞病理学",
            "author": "微尔啸",
            "category": "病理学",
            "description": "细胞病理学的创始之作。",
            "cover_url": "https://example.com/cell.jpg",
            "pdf_file_path": "books/pdfs/cell_pathology.pdf",
            "file_size": 20971520,
            "publish_date": datetime.now()
        },
        {
            "name": "医学衷中参西录",
            "author": "张锡纯",
            "category": "中西医结合",
            "description": "试图结合中西医理论的著作。",
            "cover_url": "https://example.com/combination.jpg",
            "pdf_file_path": "books/pdfs/yixue_zhongzhong_canxi_lu.pdf",
            "file_size": 16777216,
            "publish_date": datetime.now()
        }
    ]
    
    for book_data in books_data:
        # 检查是否已存在
        existing = db.query(Book).filter(
            Book.name == book_data["name"],
            Book.author == book_data["author"]
        ).first()
        if not existing:
            book = Book(**book_data)
            db.add(book)
    
    db.commit()
    print("图书数据初始化完成")

def init_users(db: Session):
    """初始化用户数据"""
    users_data = [
        {
            "username": "admin",
            "email": "admin@aimedical.com",
            "full_name": "系统管理员",
            "phone": "13800138000",
            "password": "admin123"
        },
        {
            "username": "testuser",
            "email": "test@example.com",
            "full_name": "测试用户",
            "phone": "13900139000",
            "password": "test123"
        }
    ]
    
    for user_data in users_data:
        # 检查是否已存在
        existing = db.query(User).filter(User.username == user_data["username"]).first()
        if not existing:
            password = user_data.pop("password")
            user = User(
                **user_data,
                hashed_password=get_password_hash(password)
            )
            db.add(user)
    
    db.commit()
    print("用户数据初始化完成")

if __name__ == "__main__":
    init_database()
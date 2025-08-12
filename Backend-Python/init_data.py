from sqlalchemy.orm import Session
from database import SessionLocal, engine
from models import Base, Product, Book, User
from schemas import ProductCreate, BookCreate, UserCreate
from routers.users import get_password_hash
from datetime import datetime

def init_database():
    """初始化数据库和示例数据"""
    # 创建所有表
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
            "image_url": "https://example.com/tea.jpg",
            "category": "保健品",
            "stock": 100,
            "specification": "250g/盒",
            "manufacturer": "康健药业",
            "purchase_count": 156
        },
        {
            "name": "艾灸贴",
            "price": 68.0,
            "description": "缓解疲劳，促进血液循环",
            "image_url": "https://example.com/moxibustion.jpg",
            "category": "保健品",
            "stock": 200,
            "specification": "10贴/盒",
            "manufacturer": "中医堂",
            "purchase_count": 89
        },
        {
            "name": "按摩仪",
            "price": 199.0,
            "description": "智能按摩，舒缓肌肉紧张",
            "image_url": "https://example.com/massager.jpg",
            "category": "保健品",
            "stock": 50,
            "specification": "便携式",
            "manufacturer": "健康科技",
            "purchase_count": 234
        },
        {
            "name": "中药饮片",
            "price": 128.0,
            "description": "精选中药材，调理身体",
            "image_url": "https://example.com/herb.jpg",
            "category": "保健品",
            "stock": 80,
            "specification": "500g/袋",
            "manufacturer": "同仁堂",
            "purchase_count": 67
        },
        {
            "name": "蜂胶胶囊",
            "price": 158.0,
            "description": "天然蜂胶提取，增强免疫力",
            "image_url": "https://example.com/propolis.jpg",
            "category": "保健品",
            "stock": 120,
            "specification": "60粒/瓶",
            "manufacturer": "蜂之语",
            "purchase_count": 203
        },
        {
            "name": "维生素C片",
            "price": 45.0,
            "description": "补充维生素C，提高抵抗力",
            "image_url": "https://example.com/vitamin_c.jpg",
            "category": "保健品",
            "stock": 300,
            "specification": "100片/瓶",
            "manufacturer": "汤臣倍健",
            "purchase_count": 567
        },
        {
            "name": "钙片",
            "price": 78.0,
            "description": "高钙配方，强健骨骼",
            "image_url": "https://example.com/calcium.jpg",
            "category": "保健品",
            "stock": 180,
            "specification": "120片/瓶",
            "manufacturer": "钙尔奇",
            "purchase_count": 345
        },
        {
            "name": "鱼油软胶囊",
            "price": 188.0,
            "description": "深海鱼油，保护心血管健康",
            "image_url": "https://example.com/fish_oil.jpg",
            "category": "保健品",
            "stock": 90,
            "specification": "100粒/瓶",
            "manufacturer": "挪威小鱼",
            "purchase_count": 278
        },
        
        # 医疗器械类
        {
            "name": "血压计",
            "price": 299.0,
            "description": "家用智能血压监测仪",
            "image_url": "https://example.com/blood_pressure.jpg",
            "category": "医疗器械",
            "stock": 30,
            "specification": "电子式",
            "manufacturer": "欧姆龙",
            "purchase_count": 145
        },
        {
            "name": "血糖仪",
            "price": 168.0,
            "description": "便携式血糖检测仪，操作简单",
            "image_url": "https://example.com/glucose_meter.jpg",
            "category": "医疗器械",
            "stock": 45,
            "specification": "含试纸50片",
            "manufacturer": "强生",
            "purchase_count": 89
        },
        {
            "name": "体温计",
            "price": 58.0,
            "description": "红外线额温枪，快速测温",
            "image_url": "https://example.com/thermometer.jpg",
            "category": "医疗器械",
            "stock": 200,
            "specification": "非接触式",
            "manufacturer": "博朗",
            "purchase_count": 456
        },
        {
            "name": "制氧机",
            "price": 1299.0,
            "description": "家用制氧机，改善呼吸质量",
            "image_url": "https://example.com/oxygen_concentrator.jpg",
            "category": "医疗器械",
            "stock": 15,
            "specification": "3L/min",
            "manufacturer": "鱼跃医疗",
            "purchase_count": 67
        },
        {
            "name": "雾化器",
            "price": 189.0,
            "description": "超声波雾化器，呼吸道护理",
            "image_url": "https://example.com/nebulizer.jpg",
            "category": "医疗器械",
            "stock": 80,
            "specification": "便携式",
            "manufacturer": "欧姆龙",
            "purchase_count": 123
        },
        
        # 中药材类
        {
            "name": "人参片",
            "price": 268.0,
            "description": "长白山人参，大补元气",
            "image_url": "https://example.com/ginseng.jpg",
            "category": "中药材",
            "stock": 60,
            "specification": "100g/盒",
            "manufacturer": "长白山参业",
            "purchase_count": 134
        },
        {
            "name": "枸杞子",
            "price": 88.0,
            "description": "宁夏枸杞，滋补肝肾",
            "image_url": "https://example.com/goji.jpg",
            "category": "中药材",
            "stock": 150,
            "specification": "500g/袋",
            "manufacturer": "宁夏红",
            "purchase_count": 289
        },
        {
            "name": "当归片",
            "price": 78.0,
            "description": "甘肃当归，补血调经",
            "image_url": "https://example.com/angelica.jpg",
            "category": "中药材",
            "stock": 90,
            "specification": "200g/袋",
            "manufacturer": "陇西药材",
            "purchase_count": 156
        },
        {
            "name": "黄芪片",
            "price": 65.0,
            "description": "内蒙古黄芪，补气固表",
            "image_url": "https://example.com/astragalus.jpg",
            "category": "中药材",
            "stock": 110,
            "specification": "300g/袋",
            "manufacturer": "内蒙古药材",
            "purchase_count": 198
        },
        
        # 护理用品类
        {
            "name": "医用口罩",
            "price": 25.0,
            "description": "一次性医用外科口罩",
            "image_url": "https://example.com/mask.jpg",
            "category": "护理用品",
            "stock": 500,
            "specification": "50只/盒",
            "manufacturer": "3M",
            "purchase_count": 1234
        },
        {
            "name": "酒精消毒液",
            "price": 18.0,
            "description": "75%医用酒精，杀菌消毒",
            "image_url": "https://example.com/alcohol.jpg",
            "category": "护理用品",
            "stock": 300,
            "specification": "500ml/瓶",
            "manufacturer": "海氏海诺",
            "purchase_count": 789
        },
        {
            "name": "创可贴",
            "price": 12.0,
            "description": "防水透气创可贴",
            "image_url": "https://example.com/bandaid.jpg",
            "category": "护理用品",
            "stock": 400,
            "specification": "100片/盒",
            "manufacturer": "邦迪",
            "purchase_count": 567
        },
        {
            "name": "医用纱布",
            "price": 15.0,
            "description": "无菌医用纱布块",
            "image_url": "https://example.com/gauze.jpg",
            "category": "护理用品",
            "stock": 250,
            "specification": "10片/包",
            "manufacturer": "振德医疗",
            "purchase_count": 345
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
            "publish_date": datetime.now()
        },
        {
            "name": "伤寒杂病论",
            "author": "张仲景",
            "category": "中医临床",
            "description": "确立了辨证论治原则，是中医临床的基本原则。",
            "cover_url": "https://example.com/shanghan.jpg",
            "publish_date": datetime.now()
        },
        {
            "name": "神农本草经",
            "author": "佚名",
            "category": "中药学",
            "description": "中国现存最早的中药学著作。",
            "cover_url": "https://example.com/shennong.jpg",
            "publish_date": datetime.now()
        },
        {
            "name": "本草纲目",
            "author": "李时珍",
            "category": "中药学",
            "description": "集我国16世纪以前药学成就之大成。",
            "cover_url": "https://example.com/bencao.jpg",
            "publish_date": datetime.now()
        },
        {
            "name": "针灸甲乙经",
            "author": "皇甫谧",
            "category": "针灸学",
            "description": "中国现存最早的针灸学专著。",
            "cover_url": "https://example.com/zhenjiu.jpg",
            "publish_date": datetime.now()
        },
        
        # 西医经典
        {
            "name": "希波克拉底文集",
            "author": "希波克拉底",
            "category": "医学理论",
            "description": "西方医学的奠基之作。",
            "cover_url": "https://example.com/hippocrates.jpg",
            "publish_date": datetime.now()
        },
        {
            "name": "人体的构造",
            "author": "维萨里",
            "category": "解剖学",
            "description": "近代解剖学的奠基之作。",
            "cover_url": "https://example.com/structure.jpg",
            "publish_date": datetime.now()
        },
        {
            "name": "内科学原理与实践",
            "author": "奥斯勒",
            "category": "内科学",
            "description": "现代内科学的奠基之作。",
            "cover_url": "https://example.com/internal.jpg",
            "publish_date": datetime.now()
        },
        {
            "name": "细胞病理学",
            "author": "微尔啸",
            "category": "病理学",
            "description": "细胞病理学的创始之作。",
            "cover_url": "https://example.com/cell.jpg",
            "publish_date": datetime.now()
        },
        {
            "name": "医学衷中参西录",
            "author": "张锡纯",
            "category": "中西医结合",
            "description": "试图结合中西医理论的著作。",
            "cover_url": "https://example.com/combination.jpg",
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
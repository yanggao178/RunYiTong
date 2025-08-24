from database import get_db
from models import Book
from sqlalchemy.orm import Session

def check_books():
    db = next(get_db())
    try:
        # 检查所有书籍
        all_books = db.query(Book).all()
        print(f"总书籍数量: {len(all_books)}")
        
        # 检查中医书籍
        chinese_books = db.query(Book).filter(Book.category == "中医").all()
        print(f"中医书籍数量: {len(chinese_books)}")
        
        # 显示前几本书的信息
        for i, book in enumerate(all_books[:5]):
            print(f"书籍 {i+1}: ID={book.id}, 名称={book.name}, 分类={book.category}")
            
        # 显示中医书籍
        print("\n中医书籍:")
        for book in chinese_books:
            print(f"- {book.name} (ID: {book.id})")
            
    except Exception as e:
        print(f"数据库查询错误: {e}")
    finally:
        db.close()

if __name__ == "__main__":
    check_books()
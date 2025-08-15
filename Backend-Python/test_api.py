import urllib.request
import json

def test_api():
    try:
        # 测试中医书籍API
        print("Testing Chinese Medicine Books API...")
        response = urllib.request.urlopen('http://localhost:8000/api/v1/books/chinese-medicine')
        data = json.loads(response.read().decode())
        
        print(f"Status: {response.status}")
        print(f"Success: {data.get('success')}")
        print(f"Message: {data.get('message')}")
        print(f"Data count: {len(data.get('data', []))}")
        
        if data.get('data'):
            print("First book:", data['data'][0].get('name', 'N/A'))
        
        # 测试西医书籍API
        print("\nTesting Western Medicine Books API...")
        response = urllib.request.urlopen('http://localhost:8000/api/v1/books/western-medicine')
        data = json.loads(response.read().decode())
        
        print(f"Status: {response.status}")
        print(f"Success: {data.get('success')}")
        print(f"Message: {data.get('message')}")
        print(f"Data count: {len(data.get('data', []))}")
        
        if data.get('data'):
            print("First book:", data['data'][0].get('name', 'N/A'))
            
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    test_api()

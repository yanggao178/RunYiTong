# HealthFragment 书籍信息修复测试指南

## 问题描述
养生fragment中的书籍信息消失不见的问题已经修复。

## 修复内容
1. **Fragment生命周期优化**：改进了数据加载时机
2. **防止重复加载**：添加了`isDataLoaded`状态标记
3. **UI更新优化**：确保在UI线程中正确更新RecyclerView
4. **错误处理增强**：添加了详细的日志和错误处理
5. **网络请求管理**：分别管理中医和西医书籍的API请求

## 测试步骤

### 1. 启动应用
- 确保后端服务器正在运行（端口8000）
- 启动Android应用

### 2. 测试养生Fragment
- 点击底部导航栏的"养生"选项
- 观察是否显示中医古籍和西医经典两个区域
- 检查每个区域是否显示书籍列表

### 3. 验证数据加载
- 查看Logcat日志，搜索"HealthFragment"标签
- 应该看到类似以下的日志：
  ```
  HealthFragment: onResume called
  HealthFragment: 开始加载书籍数据
  HealthFragment: 开始请求中医书籍数据
  HealthFragment: 开始请求西医书籍数据
  HealthFragment: 中医书籍API响应: 200
  HealthFragment: 收到中医书籍数据: X 本
  HealthFragment: 中医书籍适配器数据已更新
  ```

### 4. 测试Fragment切换
- 切换到其他Fragment（如商品、处方等）
- 再切换回养生Fragment
- 验证书籍信息是否仍然显示（不应该重复加载）

### 5. 检查网络连接
- 如果书籍信息不显示，检查：
  - 后端服务器是否运行在正确的端口（8000）
  - 网络连接是否正常
  - API地址是否正确（模拟器：10.0.2.2:8000，真机：192.168.0.6:8000）

## 预期结果
- 养生Fragment应该正确显示中医古籍和西医经典书籍
- 书籍信息包含：书名、作者、描述、封面图片
- Fragment切换时书籍信息应该保持显示
- 不应该出现重复加载或数据丢失的情况

## 故障排除
如果问题仍然存在，请检查：
1. 后端API是否正常响应（使用浏览器访问 http://localhost:8000/api/v1/books/chinese-medicine）
2. 网络权限是否正确配置
3. Logcat中是否有错误信息
4. 数据库是否包含书籍数据

## 相关文件
- `HealthFragment.java` - 主要修复文件
- `BookAdapter.java` - 书籍列表适配器
- `fragment_health.xml` - 养生Fragment布局
- `book_item.xml` - 书籍项目布局

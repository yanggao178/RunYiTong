# 屏幕方向设置测试指南

## 修改内容
已将应用的屏幕方向从横向（Horizontal）改为纵向（Vertical）。

## 修改的文件
- `app/src/main/AndroidManifest.xml` - 为所有Activity添加了`android:screenOrientation="portrait"`配置

## 测试步骤

### 1. 重新编译应用
```bash
# 在项目根目录执行
./gradlew clean
./gradlew assembleDebug
```

### 2. 安装并运行应用
- 卸载旧版本应用（如果存在）
- 安装新编译的APK
- 启动应用

### 3. 验证屏幕方向
- **应用启动时**：应用应该以纵向模式启动
- **旋转设备时**：应用应该保持纵向模式，不会随设备旋转
- **所有页面**：MainActivity、ProductDetailActivity、OrderActivity都应该保持纵向

### 4. 测试养生Fragment
- 点击底部导航的"养生"选项
- 验证书籍以3列网格形式显示
- 确认书架布局正常显示

## 预期结果
- ✅ 应用始终以纵向模式显示
- ✅ 设备旋转时应用不会改变方向
- ✅ 所有Activity都保持纵向模式
- ✅ 养生Fragment的书籍网格布局正常显示

## 故障排除

### 如果屏幕方向仍然会旋转：
1. **检查设备设置**：
   - 确保设备的自动旋转功能已关闭
   - 或者确保应用权限设置正确

2. **重新安装应用**：
   - 完全卸载应用
   - 重新安装新版本

3. **检查AndroidManifest.xml**：
   - 确认所有Activity都添加了`android:screenOrientation="portrait"`
   - 确认没有其他方向配置覆盖

### 如果书籍显示异常：
1. **检查BookshelfAdapter**：确认适配器正确实现
2. **检查bookshelf_item.xml**：确认布局文件存在且正确
3. **检查GridLayoutManager**：确认3列网格设置正确

## 相关配置说明

### AndroidManifest.xml中的方向配置：
```xml
<activity
    android:name=".MainActivity"
    android:screenOrientation="portrait" />
```

### 可用的屏幕方向选项：
- `portrait` - 纵向（默认）
- `landscape` - 横向
- `sensor` - 根据传感器自动旋转
- `nosensor` - 忽略传感器
- `user` - 用户首选方向
- `behind` - 与上一个Activity相同
- `fullSensor` - 全传感器支持
- `reverseLandscape` - 反向横向
- `reversePortrait` - 反向纵向
- `sensorLandscape` - 传感器横向
- `sensorPortrait` - 传感器纵向

## 注意事项
- 屏幕方向设置会影响所有Activity
- 如果需要某些特定Activity支持旋转，可以单独配置
- 纵向模式更适合移动设备的阅读和浏览体验

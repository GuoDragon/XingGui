# 修复 BOM 编译错误 - 验证检查清单

## BOM 移除检查
- [x] 27 个被感染的 Kotlin 文件均不再以 BOM 字节开头
- [x] 文件内容在移除 BOM 后逻辑不变

## 导入修复检查
- [x] ProfileScreen.kt 包含 `import androidx.compose.runtime.getValue`
- [x] ProfileScreen.kt 包含 `import androidx.compose.runtime.setValue`
- [x] ProfileScreen.kt 中 `var showChildSelectorDialog by remember { mutableStateOf(false) }` 编译无错

## 编译验证检查
- [x] `gradlew compileDebugKotlin` 返回退出码 0
- [x] 生成 769 个 class 文件，编译成功
- [x] 无 "Expecting a top level declaration" 错误
- [x] 无 "no method 'getValue'" 错误

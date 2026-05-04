# 修复 BOM 编译错误 - 任务计划

## [x] Task 1: 移除全部 27 个文件的 BOM 字符
- **Priority**: P0
- **Depends On**: None
- **Description**:
  - 使用 PowerShell 脚本批量扫描 `app/src/main/java` 下所有 .kt 文件
  - 检测并移除文件开头的 UTF-8 BOM (0xEF 0xBB 0xBF) 字节
  - 以 UTF-8 无 BOM 格式重新写入文件
- **Acceptance Criteria Addressed**: BOM 字符移除
- **Test Requirements**:
  - `programmatic` TR-1.1: 全部 27 个文件的第一个字节不再是 0xEF
  - `programmatic` TR-1.2: 文件内容在移除 BOM 后保持不变（仅去掉前3字节）
- **Notes**: 使用 PowerShell `[System.IO.File]` 读取字节并跳过前3字节写入

## [x] Task 2: 修复 ProfileScreen.kt 缺失的 Compose runtime 导入
- **Priority**: P0
- **Depends On**: None
- **Description**:
  - 在 ProfileScreen.kt 的导入区域添加 `import androidx.compose.runtime.getValue`
  - 添加 `import androidx.compose.runtime.setValue`
- **Acceptance Criteria Addressed**: Compose runtime 导入完整性
- **Test Requirements**:
  - `programmatic` TR-2.1: ProfileScreen.kt 包含 `getValue` 和 `setValue` 导入
  - `programmatic` TR-2.2: 编译无 "no method 'getValue'" 错误
- **Notes**: 这是 Compose 中使用 `by` 委托语法的必要导入

## [x] Task 3: 编译验证
- **Priority**: P0
- **Depends On**: Task 1, Task 2
- **Description**:
  - 运行 `gradlew compileDebugKotlin` 或 `gradlew assembleDebug`
  - 验证编译成功且无错误
- **Acceptance Criteria Addressed**: 全部
- **Test Requirements**:
  - `programmatic` TR-3.1: 编译退出代码为 0 ✅
  - `programmatic` TR-3.2: 生成 769 个 class 文件，最新文件时间戳为 00:24:02 ✅
- **Notes**: 编译成功，无任何 BOM 相关错误

# Task Dependencies
- Task 3 依赖 Task 1 和 Task 2

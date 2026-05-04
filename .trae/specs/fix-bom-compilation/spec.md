# 修复 BOM 编译错误 - 规格说明

## Why
之前的文件编辑操作（SearchReplace/Write工具）在写入 Kotlin 文件时注入了 UTF-8 BOM（字节序标记，0xEF 0xBB 0xBF）字符。Kotlin 编译器不接受 BOM 作为文件开头，导致所有被感染的文件在第1行第1列报错 "Expecting a top level declaration"，进而引发整个项目的编译失败。此外 ProfileScreen.kt 还缺少 `getValue`/`setValue` 的 Compose runtime 导入。

## What Changes
- 从全部 27 个被感染的 Kotlin 源文件中移除 UTF-8 BOM 字节
- 为 ProfileScreen.kt 补充缺失的 `import androidx.compose.runtime.getValue` 和 `import androidx.compose.runtime.setValue`
- 验证编译通过

## Impact
- Affected specs: fix-bom-compilation
- Affected code: 27 个 .kt 文件 + ProfileScreen.kt 导入修复

## 受影响的文件列表（共27个 BOM 文件）
1. `data/model/GrowthDimension.kt`
2. `data/model/UserRole.kt`
3. `presentation/archive/ArchiveContract.kt`
4. `presentation/archive/ArchivePresenter.kt`
5. `presentation/archive/ArchiveScreen.kt`
6. `presentation/archive/components/ArchiveSections.kt`
7. `presentation/auth/login/LoginContract.kt`
8. `presentation/auth/login/LoginPresenter.kt`
9. `presentation/auth/login/LoginScreen.kt`
10. `presentation/auth/register/RegisterPresenter.kt`
11. `presentation/auth/register/RegisterScreen.kt`
12. `presentation/auth/roleselect/RoleSelectContract.kt`
13. `presentation/auth/roleselect/RoleSelectPresenter.kt`
14. `presentation/auth/roleselect/RoleSelectScreen.kt`
15. `presentation/main/MainPresenter.kt`
16. `presentation/main/MainScreen.kt`
17. `presentation/report/ReportContract.kt`
18. `presentation/report/ReportPresenter.kt`
19. `presentation/report/ReportScreen.kt`
20. `presentation/report/components/ReportRadarChart.kt`
21. `presentation/report/components/ReportReferenceLayout.kt`
22. `presentation/report/components/ReportShareUtils.kt`
23. `presentation/resources/ResourceCategoryNames.kt`
24. `presentation/resources/ResourcesContract.kt`
25. `presentation/resources/ResourcesPresenter.kt`
26. `presentation/resources/ResourcesScreen.kt`
27. `ui/components/ChildSelectorBar.kt`

## ADDED Requirements
### Requirement: BOM 字符移除
系统 SHALL 确保所有 Kotlin 源文件不包含 UTF-8 BOM 字节。

#### Scenario: 编译无 BOM 错误
- **WHEN** 执行 `gradlew compileDebugKotlin`
- **THEN** 不出现 "Expecting a top level declaration" 错误

### Requirement: Compose runtime 导入完整性
ProfileScreen.kt SHALL 包含 `getValue` 和 `setValue` 的 Compose runtime 扩展导入。

#### Scenario: 属性委托正常工作
- **WHEN** 使用 `by remember { mutableStateOf(...) }` 语法
- **THEN** 编译器正确解析委托操作符

## Constraints
- **Technical**: 仅修改文件编码和导入语句，不改动任何业务逻辑
- **Dependencies**: 无外部依赖

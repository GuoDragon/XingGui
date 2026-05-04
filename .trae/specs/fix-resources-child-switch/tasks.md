# 星轨 XingGui - 修复资源和儿童切换功能 - The Implementation Plan (Decomposed and Prioritized Task List)

## [ ] Task 1: 移除CDC PDF资源（res101-112）
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改`app/src/main/assets/data/resources.json`，删除res101到res112的内容
  - 保留前12个正常的PDF资源（res001-012）
- **Acceptance Criteria Addressed**: [AC-1]
- **Test Requirements**:
  - `human-judgement` TR-1.1: 星资源页面只显示前12个资源
  - `human-judgement` TR-1.2: 资源数量从24个变为12个
  - `human-judgement` TR-1.3: 剩余资源可以正常打开
- **Notes**: 只修改JSON配置文件，不删除PDF文件

## [ ] Task 2: 在报告页面集成儿童选择器
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改`app/src/main/java/com/example/xinggui/presentation/report/components/ReportReferenceLayout.kt`
  - 将ChildInfoProfileCard替换为ChildSelectorBar
  - 确保仅当用户是教师且有多个儿童时显示选择器
- **Acceptance Criteria Addressed**: [AC-2, AC-3]
- **Test Requirements**:
  - `human-judgement` TR-2.1: 教师登录后报告页面显示儿童选择器
  - `human-judgement` TR-2.2: 家长登录后显示儿童信息卡片
  - `human-judgement` TR-2.3: 选择器能正常切换儿童

## [ ] Task 3: 在档案页面集成儿童选择器
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 修改`app/src/main/java/com/example/xinggui/presentation/archive/components/ArchiveSections.kt`
  - 将ChildInfoProfileCard替换为ChildSelectorBar
- **Acceptance Criteria Addressed**: [AC-2, AC-3]
- **Test Requirements**:
  - `human-judgement` TR-3.1: 教师登录后档案页面显示儿童选择器

## [ ] Task 4: 在目标页面集成儿童选择器
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 找到目标页面的布局文件
  - 集成儿童选择器
- **Acceptance Criteria Addressed**: [AC-2, AC-3]
- **Test Requirements**:
  - `human-judgement` TR-4.1: 教师登录后目标页面显示儿童选择器

## [ ] Task 5: 在资源页面集成儿童选择器
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 找到资源页面的布局文件
  - 集成儿童选择器
- **Acceptance Criteria Addressed**: [AC-2, AC-3]
- **Test Requirements**:
  - `human-judgement` TR-5.1: 教师登录后资源页面显示儿童选择器

## [ ] Task 6: 完整功能验证
- **Priority**: P0
- **Depends On**: Task 1, 2, 3, 4, 5
- **Description**: 
  - 测试家长端功能正常
  - 测试教师端功能正常
  - 验证资源移除后的效果
- **Acceptance Criteria Addressed**: [AC-1, AC-2, AC-3, AC-4]
- **Test Requirements**:
  - `human-judgement` TR-6.1: 完整端对端测试通过

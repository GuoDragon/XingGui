# 星轨 - 儿童信息展示与切换统一 - 任务计划

## [ ] Task 1: 星报告页面儿童信息组件统一
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改 ReportReferenceLayout，确保当用户不是教师或只有一个儿童时，显示 ChildInfoProfileCard
  - 检查当前实现是否已符合要求
  - 验证显示效果
- **Acceptance Criteria Addressed**: [AC-1]
- **Test Requirements**:
  - `human-judgement` TR-1.1: 家长端显示 ChildInfoProfileCard
  - `human-judgement` TR-1.2: 教师端且有多个儿童显示 ChildSelectorBar
  - `human-judgement` TR-1.3: 切换儿童后内容正确更新
- **Notes**: 已部分实现，需要验证

## [ ] Task 2: 星档案页面儿童信息组件统一
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改 ArchiveReferencePage（ArchiveSections.kt）
  - 确保根据用户角色和儿童数量显示正确的组件
- **Acceptance Criteria Addressed**: [AC-2]
- **Test Requirements**:
  - `human-judgement` TR-2.1: 家长端显示 ChildInfoProfileCard
  - `human-judgement` TR-2.2: 教师端且有多个儿童显示 ChildSelectorBar
  - `human-judgement` TR-2.3: 切换儿童后内容正确更新
- **Notes**: 已部分实现，需要验证

## [ ] Task 3: 星目标页面添加儿童信息展示
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改 GoalsScreen
  - 添加儿童信息展示组件（ChildInfoProfileCard 或 ChildSelectorBar）
  - 确保参数正确传递
- **Acceptance Criteria Addressed**: [AC-3]
- **Test Requirements**:
  - `human-judgement` TR-3.1: 页面显示儿童信息组件
  - `human-judgement` TR-3.2: 家长端显示 ChildInfoProfileCard
  - `human-judgement` TR-3.3: 教师端且有多个儿童显示 ChildSelectorBar
- **Notes**: 当前 GoalsScreen 没有儿童信息展示

## [ ] Task 4: 星资源页面添加儿童信息展示
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改 ResourcesScreen
  - 添加儿童信息展示组件（ChildInfoProfileCard 或 ChildSelectorBar）
  - 确保参数正确传递
- **Acceptance Criteria Addressed**: [AC-3]
- **Test Requirements**:
  - `human-judgement` TR-4.1: 页面显示儿童信息组件
  - `human-judgement` TR-4.2: 家长端显示 ChildInfoProfileCard
  - `human-judgement` TR-4.3: 教师端且有多个儿童显示 ChildSelectorBar
- **Notes**: 当前 ResourcesScreen 没有儿童信息展示

## [ ] Task 5: 实现教师端"我的"页面儿童选择功能
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改 ProfileScreen（ProfileScreen.kt）
  - 点击"已关联X名儿童"徽章触发儿童选择
  - 实现儿童选择弹窗或界面
  - 集成选择逻辑
- **Acceptance Criteria Addressed**: [AC-4, AC-5]
- **Test Requirements**:
  - `human-judgement` TR-5.1: 点击徽章显示儿童选择列表
  - `human-judgement` TR-5.2: 列表显示所有已关联儿童
  - `human-judgement` TR-5.3: 点击列表项可切换儿童
  - `human-judgement` TR-5.4: 切换后所有页面更新
- **Notes**: 需要设计儿童选择列表的UI

## [ ] Task 6: 验证最后选择的儿童状态保存功能
- **Priority**: P1
- **Depends On**: None
- **Description**: 
  - 检查 DataRepository 是否已实现保存和加载最后选择的儿童
  - 验证 MainPresenter 的 onChildSelected 是否正确保存
  - 测试下次打开应用时默认显示上次选择的儿童
- **Acceptance Criteria Addressed**: [AC-6]
- **Test Requirements**:
  - `human-judgement` TR-6.1: 切换儿童后关闭应用
  - `human-judgement` TR-6.2: 再次打开应用时显示上次选择的儿童
- **Notes**: 可能已实现，需要验证

## [ ] Task 7: 家长端功能验证
- **Priority**: P0
- **Depends On**: Task 1, 2, 3, 4
- **Description**: 
  - 使用家长账号登录
  - 验证各个页面显示 ChildInfoProfileCard
  - 验证不显示儿童选择器
  - 验证功能正常
- **Acceptance Criteria Addressed**: [AC-7]
- **Test Requirements**:
  - `human-judgement` TR-7.1: 所有页面显示 ChildInfoProfileCard
  - `human-judgement` TR-7.2: 不显示儿童选择器
  - `human-judgement` TR-7.3: 功能正常
- **Notes**: 确保家长端功能不受影响

## [ ] Task 8: 完整功能测试与验证
- **Priority**: P0
- **Depends On**: Task 1, 2, 3, 4, 5, 6, 7
- **Description**: 
  - 完整的端对端测试
  - 家长和教师账号分别测试
  - 儿童切换功能验证
  - 所有页面同步验证
- **Acceptance Criteria Addressed**: [AC-1, AC-2, AC-3, AC-4, AC-5, AC-6, AC-7]
- **Test Requirements**:
  - `human-judgement` TR-8.1: 所有页面儿童信息展示统一
  - `human-judgement` TR-8.2: 教师端儿童切换功能正常
  - `human-judgement` TR-8.3: 切换后所有页面同步更新
  - `human-judgement` TR-8.4: 家长端功能正常

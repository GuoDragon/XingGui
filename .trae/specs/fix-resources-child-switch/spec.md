# 星轨 XingGui - 修复资源和儿童切换功能 - Product Requirement Document

## Overview
- **Summary**: 修复星轨APP的两个问题：1）移除有乱码问题的CDC PDF资源；2）修复教师端无法切换儿童的功能
- **Purpose**: 解决APP在大赛展示前的关键问题，确保功能正常可用
- **Target Users**: 参赛团队、评委、演示用户

## Goals
- 移除有乱码问题的CDC发展里程碑PDF资源（res101-112）
- 修复教师端儿童切换功能，让教师可以正常查看不同儿童的资料
- 保持APP其他功能的完整性

## Non-Goals (Out of Scope)
- 不添加新功能
- 不重构现有架构
- 不替换有问题的PDF为其他资源
- 不修改PDF渲染逻辑

## Background & Context
1. **CDC PDF乱码问题**: 项目中有24个PDF资源，其中12个是CDC官方的发展里程碑检查表（res101-112），这些PDF在Android PdfRenderer中显示为乱码，影响用户体验
2. **教师端儿童切换问题**: 虽然ChildSelectorBar组件已实现，但实际页面使用的是ChildInfoProfileCard，导致教师无法切换查看不同儿童的资料
3. **大赛准备**: 项目正在准备参加计算机设计大赛，需要确保功能完整可用

## Functional Requirements
- **FR-1**: 从资源列表中移除12个CDC PDF资源
- **FR-2**: 教师登录后能看到儿童选择器并能切换儿童
- **FR-3**: 切换儿童后，各页面（报告、档案、目标、资源）显示对应儿童的内容

## Non-Functional Requirements
- **NFR-1**: 修改不影响家长端功能
- **NFR-2**: 资源移除后APP启动正常
- **NFR-3**: 切换儿童操作流畅，无明显延迟

## Constraints
- **Technical**: 仅修改现有代码，不引入新依赖
- **Business**: 必须在大赛前完成修复
- **Dependencies**: 依赖现有MVP架构和数据结构

## Assumptions
- ChildSelectorBar组件本身实现正确
- 儿童数据和关联关系完整
- 移除CDC资源不会影响其他功能

## Acceptance Criteria

### AC-1: CDC资源移除
- **Given**: 用户打开APP并进入星资源页面
- **When**: 查看资源列表
- **Then**: 只显示前12个正常的PDF资源（res001-012），不显示CDC的12个资源（res101-112）
- **Verification**: `human-judgment`

### AC-2: 教师端显示儿童选择器
- **Given**: 教师用户登录APP
- **When**: 进入报告、档案、目标、资源或个人中心页面
- **Then**: 页面顶部显示儿童选择器，可以看到教师关联的所有儿童
- **Verification**: `human-judgment`

### AC-3: 教师端可以切换儿童
- **Given**: 教师用户已登录，看到儿童选择器
- **When**: 点击选择不同的儿童
- **Then**: 当前儿童切换成功，页面显示新选择儿童的内容
- **Verification**: `human-judgment`

### AC-4: 家长端不受影响
- **Given**: 家长用户登录APP
- **When**: 使用各个功能
- **Then**: 功能正常，显示其唯一关联的儿童资料
- **Verification**: `human-judgment`

## Open Questions
- [ ] 无

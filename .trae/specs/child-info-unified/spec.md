# 星轨 - 儿童信息展示与切换统一
## Overview
- **Summary**: 统一星报告、星档案及其他相关页面中的儿童信息展示组件，确保视觉样式、交互逻辑和数据格式的一致性，并在教师端"我的"页面实现儿童选择功能。
- **Purpose**: 提升用户体验，确保所有页面儿童信息展示的统一性，方便教师用户快速切换查看不同儿童的信息。
- **Target Users**: 教师用户（主要）、家长用户（次要）

## Goals
- 在星报告、星档案、星目标、星资源等相关页面统一使用 ChildInfoProfileCard 组件展示儿童信息
- 在教师端"我的"页面实现点击"已关联X名儿童"徽章触发儿童选择功能
- 支持通过儿童选择器切换儿童，所有页面同步更新
- 保存用户最后选择的儿童，下次打开时默认显示

## Non-Goals (Out of Scope)
- 不修改 ChildInfoProfileCard 组件的内部实现
- 不改变家长端的功能，家长端保持使用 ChildInfoProfileCard 而不显示选择器
- 不修改数据模型结构

## Background & Context
1. **当前实现**: ChildInfoProfileCard 组件已在"我的"页面使用，ChildSelectorBar 已实现但未在所有页面统一集成
2. **之前的修改**: 最近已在报告和档案页面部分集成了儿童选择功能，但需要完善
3. **设计要求**: 需要统一所有页面的儿童信息展示方式

## Functional Requirements
- **FR-1**: 在星报告、星档案、星目标、星资源等所有相关页面统一集成 ChildInfoProfileCard 组件
- **FR-2**: 教师角色时，在"我的"页面点击"已关联X名儿童"徽章显示儿童选择列表
- **FR-3**: 儿童选择列表支持切换儿童
- **FR-4**: 切换儿童后所有页面同步更新
- **FR-5**: 保存用户最后选择的儿童状态

## Non-Functional Requirements
- **NFR-1**: 所有页面儿童信息展示保持一致的视觉样式
- **NFR-2**: 切换儿童后页面响应流畅，无明显延迟
- **NFR-3**: 儿童选择器设计符合现有设计系统风格

## Constraints
- **Technical**: 仅修改现有代码，遵循当前架构和设计风格
- **Business**: 需要在大赛前完成实现
- **Dependencies**: 依赖现有 ChildInfoProfileCard、ChildSelectorBar、MainPresenter 的实现

## Assumptions
- ChildInfoProfileCard 和 ChildSelectorBar 组件本身功能正常
- MainPresenter 的 onChildSelected 方法已实现儿童切换逻辑
- DataRepository 已支持保存和加载用户最后选择的儿童状态

## Acceptance Criteria

### AC-1: 星报告页面儿童信息展示统一
- **Given**: 用户打开星报告页面
- **When**: 查看页面顶部
- **Then**: 显示 ChildInfoProfileCard 组件（家长端）或 ChildSelectorBar（教师端且有多个儿童）
- **Verification**: `human-judgment`

### AC-2: 星档案页面儿童信息展示统一
- **Given**: 用户打开星档案页面
- **When**: 查看页面顶部
- **Then**: 显示 ChildInfoProfileCard 组件（家长端）或 ChildSelectorBar（教师端且有多个儿童）
- **Verification**: `human-judgment`

### AC-3: 其他相关页面儿童信息展示统一
- **Given**: 用户打开星目标、星资源页面
- **When**: 查看页面
- **Then**: 显示 ChildInfoProfileCard 组件（家长端）或 ChildSelectorBar（教师端且有多个儿童）
- **Verification**: `human-judgment`

### AC-4: 教师端"我的"页面点击"已关联X名儿童"显示选择器
- **Given**: 教师用户打开"我的"页面，且有多个已关联儿童
- **When**: 点击"已关联X名儿童"徽章
- **Then**: 显示儿童选择列表弹窗或界面
- **Verification**: `human-judgment`

### AC-5: 儿童选择列表支持切换
- **Given**: 显示儿童选择列表
- **When**: 点击列表中的某一儿童
- **Then**: 当前选择更新，所有页面同步刷新
- **Verification**: `human-judgment`

### AC-6: 保存用户最后选择的儿童
- **Given**: 用户切换儿童后关闭应用
- **When**: 再次打开应用
- **Then**: 默认显示上次选择的儿童
- **Verification**: `human-judgment`

### AC-7: 家长端不受影响
- **Given**: 家长用户打开应用
- **When**: 使用各功能
- **Then**: 显示 ChildInfoProfileCard，不显示选择器，功能正常
- **Verification**: `human-judgment`

## Open Questions
- [ ] 儿童选择列表使用什么样的UI样式？（弹窗、底部抽屉还是其他）

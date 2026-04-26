# PDF 资源准备说明

## 当前状态

数据库已更新，前4个资源现在都有 PDF 路径：

1. **res001_inclusive_education_guide.pdf** - 融合教育政策要点解读
2. **res002_iep_writing_guide.pdf** - 个别化教育计划（IEP）编写指南
3. **res003_autism_support_strategies.pdf** - 自闭症儿童教育支持策略
4. **res004_sensory_integration_activities.pdf** - 感觉统合训练活动手册

## 需要准备的 PDF 文件

请将 PDF 文件放置在以下目录：
```
app/src/main/assets/resources/pdfs/
```

### 方案1：使用公开的教育资源（推荐）

**融合教育相关**：
- 可以从教育部官网下载公开的融合教育政策文件
- 搜索关键词："融合教育 政策 PDF"

**IEP 编写指南**：
- 可以使用公开的 IEP 模板和指南
- 搜索关键词："IEP template PDF" 或 "个别化教育计划 模板"

**自闭症教育支持**：
- 可以使用公开的自闭症教育资源
- 推荐来源：
  - Autism Speaks 官网的免费资源
  - 各地特殊教育资源中心的公开材料

**感统训练活动**：
- 可以使用公开的感统训练指南
- 搜索关键词："sensory integration activities PDF"

### 方案2：创建示例 PDF

如果找不到合适的公开资源，可以：
1. 创建简单的示例 PDF 文档
2. 包含基本的说明和示例内容
3. 标注"示例文档"字样

### 方案3：使用占位 PDF

我可以帮你创建一个简单的占位 PDF，显示"资源准备中"的提示。

## CDC 检查表

CDC 检查表的 PDF 文件已经存在于：
```
app/src/main/assets/resources/pdfs/res101_cdc_2_months_checklist.pdf
app/src/main/assets/resources/pdfs/res102_cdc_4_months_checklist.pdf
...
```

这些文件来自 CDC 官网，是公开的教育资源。

## 重要提示

⚠️ **版权注意**：
- 请确保使用的 PDF 文件是公开的、免费的，或者你有使用权限
- 不要使用受版权保护的商业出版物
- 建议使用政府机构、非营利组织发布的公开教育资源

## 下一步

1. 准备好 PDF 文件后，放入 `app/src/main/assets/resources/pdfs/` 目录
2. 重新构建 app
3. 测试点击资源是否能正常打开 PDF

如果需要帮助创建占位 PDF 或寻找公开资源，请告诉我！

-- 更新资源数据，添加 PDF 路径
USE xinggui;
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 更新前4个资源，添加 PDF 路径
UPDATE resources
SET
    title = '融合教育政策要点解读',
    summary = '融合教育的核心理念、政策框架和实施要点，帮助教师和家长理解融合教育的本质。',
    asset_path = 'resources/pdfs/res001_inclusive_education_guide.pdf',
    source_url = NULL
WHERE resource_id = 'res001';

UPDATE resources
SET
    title = '个别化教育计划（IEP）编写指南',
    summary = 'IEP 编写的标准流程、目标设定方法和评估工具，附实际案例参考。',
    asset_path = 'resources/pdfs/res002_iep_writing_guide.pdf',
    source_url = NULL
WHERE resource_id = 'res002';

UPDATE resources
SET
    title = '自闭症儿童教育支持策略',
    summary = '基于循证实践的自闭症儿童教育支持方法，包括课堂适应和行为管理策略。',
    asset_path = 'resources/pdfs/res003_autism_support_strategies.pdf',
    source_url = NULL
WHERE resource_id = 'res003';

UPDATE resources
SET
    title = '感觉统合训练活动手册',
    summary = '家庭和课堂可用的感统训练活动设计，包含材料准备和实施步骤。',
    asset_path = 'resources/pdfs/res004_sensory_integration_activities.pdf',
    source_url = NULL
WHERE resource_id = 'res004';

-- 查看更新结果
SELECT resource_id, title, asset_path
FROM resources
WHERE resource_id IN ('res001', 'res002', 'res003', 'res004')
ORDER BY resource_id;

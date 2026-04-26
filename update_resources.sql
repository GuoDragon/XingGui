-- 更新星资源数据，使其更像最终产品
USE xinggui;

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 更新主要资源（前4个）
UPDATE resources
SET
    title = '2024融合教育实施指南',
    category = '资讯政策',
    is_paid = 0,
    summary = '教育部最新发布的融合教育实施细则，涵盖课程调整、评估标准和支持体系建设。',
    recommended_reason = '政策解读',
    source_url = 'https://www.moe.gov.cn'
WHERE resource_id = 'res001';

UPDATE resources
SET
    title = '特殊儿童个别化教育计划编写指南',
    category = '资讯政策',
    is_paid = 0,
    summary = 'IEP 编写的标准流程、目标设定方法和评估工具，附实际案例参考。',
    recommended_reason = '实用工具',
    asset_path = NULL,
    source_url = NULL
WHERE resource_id = 'res002';

UPDATE resources
SET
    title = '自闭症谱系障碍早期干预策略',
    category = '政策解读',
    is_paid = 1,
    summary = '基于循证实践的 ASD 早期干预方法，包括应用行为分析（ABA）和结构化教学。',
    recommended_reason = '专业指导',
    asset_path = NULL,
    source_url = NULL
WHERE resource_id = 'res003';

UPDATE resources
SET
    title = '感觉统合训练教具制作手册',
    category = '教具指南',
    is_paid = 0,
    summary = '家庭和课堂可用的感统训练教具制作方法，包含材料清单和使用说明。',
    recommended_reason = '动手实践',
    asset_path = NULL,
    source_url = NULL
WHERE resource_id = 'res004';

-- 更新 CDC 检查表系列
UPDATE resources SET
    title = 'CDC 发展里程碑检查表（2个月）',
    summary = '美国疾控中心官方发布的 2 个月婴儿发展里程碑评估工具。',
    recommended_reason = '早期筛查'
WHERE resource_id = 'res101';

UPDATE resources SET
    title = 'CDC 发展里程碑检查表（4个月）',
    summary = '美国疾控中心官方发布的 4 个月婴儿发展里程碑评估工具。',
    recommended_reason = '发展追踪'
WHERE resource_id = 'res102';

UPDATE resources SET
    title = 'CDC 发展里程碑检查表（6个月）',
    summary = '美国疾控中心官方发布的 6 个月婴儿发展里程碑评估工具。',
    recommended_reason = '档案记录'
WHERE resource_id = 'res103';

UPDATE resources SET
    title = 'CDC 发展里程碑检查表（9个月）',
    summary = '美国疾控中心官方发布的 9 个月婴儿发展里程碑评估工具。',
    recommended_reason = '案例参考'
WHERE resource_id = 'res104';

UPDATE resources SET
    title = 'CDC 发展里程碑检查表（12个月）',
    summary = '美国疾控中心官方发布的 12 个月婴儿发展里程碑评估工具。',
    recommended_reason = '一岁评估'
WHERE resource_id = 'res105';

UPDATE resources SET
    title = 'CDC 发展里程碑检查表（15个月）',
    summary = '美国疾控中心官方发布的 15 个月幼儿发展里程碑评估工具。',
    recommended_reason = '干预评估'
WHERE resource_id = 'res106';

UPDATE resources SET
    title = 'CDC 发展里程碑检查表（18个月）',
    summary = '美国疾控中心官方发布的 18 个月幼儿发展里程碑评估工具。',
    recommended_reason = '筛查工具'
WHERE resource_id = 'res107';

UPDATE resources SET
    title = 'CDC 发展里程碑检查表（2岁）',
    summary = '美国疾控中心官方发布的 2 岁幼儿发展里程碑评估工具。',
    recommended_reason = '标准对照'
WHERE resource_id = 'res108';

UPDATE resources SET
    title = 'CDC 发展里程碑检查表（30个月）',
    summary = '美国疾控中心官方发布的 30 个月幼儿发展里程碑评估工具。',
    recommended_reason = '过渡评估'
WHERE resource_id = 'res109';

UPDATE resources SET
    title = 'CDC 发展里程碑检查表（3岁）',
    summary = '美国疾控中心官方发布的 3 岁儿童发展里程碑评估工具。',
    recommended_reason = '学前准备'
WHERE resource_id = 'res110';

UPDATE resources SET
    title = 'CDC 发展里程碑检查表（4岁）',
    summary = '美国疾控中心官方发布的 4 岁儿童发展里程碑评估工具。',
    recommended_reason = '能力评估'
WHERE resource_id = 'res111';

UPDATE resources SET
    title = 'CDC 发展里程碑检查表（5岁）',
    summary = '美国疾控中心官方发布的 5 岁儿童发展里程碑评估工具。',
    recommended_reason = '入学准备'
WHERE resource_id = 'res112';

-- 查看更新结果
SELECT resource_id, title, category, summary, recommended_reason
FROM resources
ORDER BY resource_id;

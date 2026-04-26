-- 添加新的教育资源
USE xinggui;
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 插入新资源
INSERT INTO resources (resource_id, title, category, is_paid, summary, recommended_reason, asset_path, source_url) VALUES
('res005', '特殊儿童语言发展训练方案', '典型案例', 0, '针对语言发展迟缓儿童的系统训练方案，包含评估工具和训练活动。', '案例参考', 'resources/pdfs/res005_language_development.pdf', NULL),
('res006', '注意力缺陷多动障碍（ADHD）课堂管理', '典型案例', 0, 'ADHD 儿童的课堂行为管理策略和环境调整建议。', '行为管理', 'resources/pdfs/res006_adhd_classroom_management.pdf', NULL),
('res007', '视觉支持工具制作与应用', '教具指南', 0, '视觉时间表、社交故事和提示卡的制作方法与使用技巧。', '实用工具', 'resources/pdfs/res007_visual_supports.pdf', NULL),
('res008', '家长培训手册：早期干预的重要性', '资讯政策', 0, '帮助家长理解早期干预的价值，掌握家庭训练的基本方法。', '家长指导', 'resources/pdfs/res008_parent_training.pdf', NULL),
('res009', '社交技能训练课程设计', '典型案例', 1, '结构化的社交技能训练课程，包含角色扮演和情境练习。', '课程设计', 'resources/pdfs/res009_social_skills_training.pdf', NULL),
('res010', '辅助沟通系统（AAC）应用指南', '教具指南', 1, '图片交换沟通系统（PECS）和辅助沟通设备的选择与使用。', '沟通支持', 'resources/pdfs/res010_aac_guide.pdf', NULL),
('res011', '特殊教育评估工具使用手册', '政策解读', 0, '常用特殊教育评估工具的介绍、使用方法和结果解读。', '评估工具', 'resources/pdfs/res011_assessment_tools.pdf', NULL),
('res012', '融合课堂教学策略与调整', '典型案例', 0, '融合课堂中的差异化教学策略和课程调整方法。', '教学策略', 'resources/pdfs/res012_inclusive_teaching.pdf', NULL)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  category = VALUES(category),
  is_paid = VALUES(is_paid),
  summary = VALUES(summary),
  recommended_reason = VALUES(recommended_reason),
  asset_path = VALUES(asset_path),
  source_url = VALUES(source_url);

-- 查看所有资源
SELECT resource_id, title, category, is_paid
FROM resources
ORDER BY resource_id;

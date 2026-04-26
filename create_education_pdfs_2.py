#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成更多教育资源 PDF 文件（第二批）
"""

from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib.enums import TA_CENTER, TA_LEFT, TA_JUSTIFY
import os

# 注册中文字体
try:
    pdfmetrics.registerFont(TTFont('SimSun', 'C:/Windows/Fonts/simsun.ttc'))
    chinese_font = 'SimSun'
except:
    chinese_font = 'Helvetica'

def create_pdf(filename, title, sections):
    """创建 PDF 文件"""
    doc = SimpleDocTemplate(filename, pagesize=A4,
                           topMargin=0.75*inch, bottomMargin=0.75*inch,
                           leftMargin=0.75*inch, rightMargin=0.75*inch)
    story = []
    styles = getSampleStyleSheet()

    title_style = ParagraphStyle(
        'CustomTitle',
        parent=styles['Heading1'],
        fontSize=20,
        textColor='#2E6CE6',
        spaceAfter=20,
        alignment=TA_CENTER,
        fontName=chinese_font if chinese_font != 'Helvetica' else 'Helvetica-Bold',
        leading=28
    )

    h1_style = ParagraphStyle(
        'CustomH1',
        parent=styles['Heading2'],
        fontSize=16,
        spaceAfter=12,
        spaceBefore=16,
        fontName=chinese_font if chinese_font != 'Helvetica' else 'Helvetica-Bold',
        textColor='#1A1A1A',
        leading=22
    )

    h2_style = ParagraphStyle(
        'CustomH2',
        parent=styles['Heading3'],
        fontSize=14,
        spaceAfter=10,
        spaceBefore=12,
        fontName=chinese_font if chinese_font != 'Helvetica' else 'Helvetica-Bold',
        textColor='#333333',
        leading=20
    )

    body_style = ParagraphStyle(
        'CustomBody',
        parent=styles['BodyText'],
        fontSize=11,
        spaceAfter=8,
        alignment=TA_JUSTIFY,
        fontName=chinese_font,
        leading=18,
        firstLineIndent=22
    )

    list_style = ParagraphStyle(
        'CustomList',
        parent=styles['BodyText'],
        fontSize=11,
        spaceAfter=6,
        leftIndent=20,
        fontName=chinese_font,
        leading=18
    )

    story.append(Paragraph(title, title_style))
    story.append(Spacer(1, 0.3*inch))

    for section in sections:
        if section['type'] == 'h1':
            story.append(Paragraph(section['text'], h1_style))
        elif section['type'] == 'h2':
            story.append(Paragraph(section['text'], h2_style))
        elif section['type'] == 'body':
            story.append(Paragraph(section['text'], body_style))
        elif section['type'] == 'list':
            story.append(Paragraph(section['text'], list_style))
        elif section['type'] == 'spacer':
            story.append(Spacer(1, section.get('height', 0.2)*inch))

    doc.build(story)
    print(f"Created: {filename}")

os.chdir('app/src/main/assets/resources/pdfs')

# 4. 家长培训手册
create_pdf(
    "res008_parent_training.pdf",
    "家长培训手册：早期干预的重要性",
    [
        {'type': 'h1', 'text': '一、为什么早期干预如此重要'},
        {'type': 'body', 'text': '研究表明，0-6岁是儿童大脑发育的关键期。在这个阶段进行干预，可以最大限度地促进儿童的发展，改善长期预后。'},
        {'type': 'h2', 'text': '早期干预的优势'},
        {'type': 'list', 'text': '• 大脑可塑性最强，学习能力最佳'},
        {'type': 'list', 'text': '• 可以预防或减轻继发性问题'},
        {'type': 'list', 'text': '• 家庭参与度高，效果更好'},
        {'type': 'list', 'text': '• 长期来看，成本效益最高'},
        {'type': 'spacer'},

        {'type': 'h1', 'text': '二、识别发展迟缓的信号'},
        {'type': 'body', 'text': '家长是最了解孩子的人。如果发现以下信号，应及时寻求专业评估：'},
        {'type': 'h2', 'text': '语言发展'},
        {'type': 'list', 'text': '• 12个月时不会发出咿呀声'},
        {'type': 'list', 'text': '• 18个月时不会说单词'},
        {'type': 'list', 'text': '• 24个月时不会说两词短语'},
        {'type': 'h2', 'text': '社交互动'},
        {'type': 'list', 'text': '• 不与人眼神接触'},
        {'type': 'list', 'text': '• 对叫名字没有反应'},
        {'type': 'list', 'text': '• 不会模仿动作或表情'},
        {'type': 'h2', 'text': '运动发展'},
        {'type': 'list', 'text': '• 6个月时不会翻身'},
        {'type': 'list', 'text': '• 12个月时不会坐'},
        {'type': 'list', 'text': '• 18个月时不会走'},
        {'type': 'spacer'},

        {'type': 'h1', 'text': '三、家庭干预策略'},
        {'type': 'h2', 'text': '1. 创造丰富的学习环境'},
        {'type': 'body', 'text': '日常生活中的每个时刻都是学习机会。'},
        {'type': 'list', 'text': '• 多与孩子交谈，描述正在做的事'},
        {'type': 'list', 'text': '• 提供多样化的玩具和材料'},
        {'type': 'list', 'text': '• 阅读图画书，讲故事'},
        {'type': 'list', 'text': '• 唱歌、做手指游戏'},
        {'type': 'h2', 'text': '2. 遵循孩子的兴趣'},
        {'type': 'body', 'text': '观察孩子感兴趣的事物，以此为基础进行互动和教学。'},
        {'type': 'list', 'text': '• 跟随孩子的视线和注意力'},
        {'type': 'list', 'text': '• 加入孩子的游戏'},
        {'type': 'list', 'text': '• 扩展孩子的兴趣'},
        {'type': 'h2', 'text': '3. 使用自然教学法'},
        {'type': 'body', 'text': '在日常活动中自然地教授技能，而非刻板的训练。'},
        {'type': 'list', 'text': '• 吃饭时教授词汇和社交技能'},
        {'type': 'list', 'text': '• 洗澡时练习身体部位名称'},
        {'type': 'list', 'text': '• 购物时学习颜色和数字'},
        {'type': 'spacer'},

        {'type': 'h1', 'text': '四、与专业人员合作'},
        {'type': 'body', 'text': '家长是干预团队的核心成员。与专业人员建立良好的合作关系至关重要。'},
        {'type': 'h2', 'text': '如何有效合作'},
        {'type': 'list', 'text': '• 积极参与评估和计划制定'},
        {'type': 'list', 'text': '• 在家中实施专业人员建议的策略'},
        {'type': 'list', 'text': '• 记录孩子的进步和困难'},
        {'type': 'list', 'text': '• 定期沟通，及时反馈'},
        {'type': 'spacer'},

        {'type': 'h1', 'text': '五、照顾好自己'},
        {'type': 'body', 'text': '养育特殊需要儿童是一项长期而艰巨的任务。家长需要照顾好自己，才能更好地支持孩子。'},
        {'type': 'list', 'text': '• 寻求家人和朋友的支持'},
        {'type': 'list', 'text': '• 加入家长支持小组'},
        {'type': 'list', 'text': '• 安排自己的休息时间'},
        {'type': 'list', 'text': '• 必要时寻求心理咨询'},
        {'type': 'body', 'text': '记住：你不是一个人在战斗。寻求帮助是力量的表现，而非软弱。'},
    ]
)

# 5. 社交技能训练
create_pdf(
    "res009_social_skills_training.pdf",
    "社交技能训练课程设计",
    [
        {'type': 'h1', 'text': '一、社交技能的重要性'},
        {'type': 'body', 'text': '社交技能是儿童融入社会、建立友谊和获得成功的基础。许多特殊需要儿童在社交方面面临挑战，需要系统的训练。'},
        {'type': 'spacer'},

        {'type': 'h1', 'text': '二、核心社交技能'},
        {'type': 'h2', 'text': '1. 基础技能'},
        {'type': 'list', 'text': '• 眼神接触：看着对方说话和倾听'},
        {'type': 'list', 'text': '• 问候：说"你好"、"再见"'},
        {'type': 'list', 'text': '• 轮流：等待自己的回合'},
        {'type': 'list', 'text': '• 分享：与他人分享物品'},
        {'type': 'h2', 'text': '2. 对话技能'},
        {'type': 'list', 'text': '• 开始对话：主动打招呼或提问'},
        {'type': 'list', 'text': '• 维持对话：回应他人，提出相关问题'},
        {'type': 'list', 'text': '• 结束对话：礼貌地告别'},
        {'type': 'h2', 'text': '3. 情绪识别'},
        {'type': 'list', 'text': '• 识别面部表情'},
        {'type': 'list', 'text': '• 理解他人的感受'},
        {'type': 'list', 'text': '• 表达自己的情绪'},
        {'type': 'spacer'},

        {'type': 'h1', 'text': '三、训练方法'},
        {'type': 'h2', 'text': '1. 直接教学'},
        {'type': 'body', 'text': '明确教授社交规则和技能。'},
        {'type': 'list', 'text': '• 讲解：说明技能的重要性和步骤'},
        {'type': 'list', 'text': '• 示范：教师或同伴演示正确行为'},
        {'type': 'list', 'text': '• 练习：学生在支持下练习'},
        {'type': 'list', 'text': '• 反馈：提供具体的正面反馈'},
        {'type': 'h2', 'text': '2. 角色扮演'},
        {'type': 'body', 'text': '通过模拟真实情境，让学生练习社交技能。'},
        {'type': 'list', 'text': '• 设计常见社交情境（如加入游戏）'},
        {'type': 'list', 'text': '• 分配角色，进行演练'},
        {'type': 'list', 'text': '• 讨论不同的应对方式'},
        {'type': 'list', 'text': '• 重复练习，直到熟练'},
        {'type': 'h2', 'text': '3. 社交故事'},
        {'type': 'body', 'text': '使用故事帮助学生理解社交情境和期望。'},
        {'type': 'list', 'text': '• 描述具体情境'},
        {'type': 'list', 'text': '• 说明他人的想法和感受'},
        {'type': 'list', 'text': '• 提供适当的应对策略'},
        {'type': 'spacer'},

        {'type': 'h1', 'text': '四、课程示例：加入游戏'},
        {'type': 'h2', 'text': '目标'},
        {'type': 'body', 'text': '学生能够使用适当的方式加入同伴的游戏。'},
        {'type': 'h2', 'text': '步骤'},
        {'type': 'list', 'text': '1. 观察：看看其他人在玩什么'},
        {'type': 'list', 'text': '2. 靠近：走到游戏区域附近'},
        {'type': 'list', 'text': '3. 等待：等待合适的时机'},
        {'type': 'list', 'text': '4. 询问："我可以一起玩吗？"'},
        {'type': 'list', 'text': '5. 接受回应：如果被拒绝，礼貌地离开'},
        {'type': 'h2', 'text': '练习活动'},
        {'type': 'list', 'text': '• 视频示范：观看正确和错误的示例'},
        {'type': 'list', 'text': '• 角色扮演：练习加入不同类型的游戏'},
        {'type': 'list', 'text': '• 真实练习：在课间或游戏时间应用'},
        {'type': 'spacer'},

        {'type': 'h1', 'text': '五、泛化和维持'},
        {'type': 'body', 'text': '确保学生能在不同情境中使用所学技能。'},
        {'type': 'list', 'text': '• 在多种情境中练习（教室、操场、家中）'},
        {'type': 'list', 'text': '• 与不同的人练习（老师、同学、家人）'},
        {'type': 'list', 'text': '• 定期复习和强化'},
        {'type': 'list', 'text': '• 家校合作，共同支持'},
        {'type': 'body', 'text': '社交技能的学习是一个长期过程，需要耐心和持续的支持。'},
    ]
)

# 6. AAC 应用指南
create_pdf(
    "res010_aac_guide.pdf",
    "辅助沟通系统（AAC）应用指南",
    [
        {'type': 'h1', 'text': '一、什么是 AAC'},
        {'type': 'body', 'text': '辅助沟通系统（Augmentative and Alternative Communication, AAC）是指帮助有沟通障碍的人表达想法和需求的工具和策略。'},
        {'type': 'h2', 'text': 'AAC 的类型'},
        {'type': 'list', 'text': '• 无辅助 AAC：手语、手势、面部表情'},
        {'type': 'list', 'text': '• 低科技 AAC：图片卡、沟通板'},
        {'type': 'list', 'text': '• 高科技 AAC：语音输出设备、平板电脑应用'},
        {'type': 'spacer'},

        {'type': 'h1', 'text': '二、图片交换沟通系统（PECS）'},
        {'type': 'body', 'text': 'PECS 是一种广泛使用的低科技 AAC 方法，特别适合自闭症儿童。'},
        {'type': 'h2', 'text': '六个阶段'},
        {'type': 'list', 'text': '阶段1：物理交换 - 学习用图片交换想要的物品'},
        {'type': 'list', 'text': '阶段2：增加距离 - 在不同距离和情境中使用'},
        {'type': 'list', 'text': '阶段3：图片辨别 - 从多个图片中选择'},
        {'type': 'list', 'text': '阶段4：句子结构 - 使用"我要"句式'},
        {'type': 'list', 'text': '阶段5：回应问题 - 回答"你要什么？"'},
        {'type': 'list', 'text': '阶段6：评论 - 表达观察和感受'},
        {'type': 'h2', 'text': '实施要点'},
        {'type': 'list', 'text': '• 从儿童喜欢的物品开始'},
        {'type': 'list', 'text': '• 立即响应沟通尝试'},
        {'type': 'list', 'text': '• 在自然情境中练习'},
        {'type': 'list', 'text': '• 逐步增加难度'},
        {'type': 'spacer'},

        {'type': 'h1', 'text': '三、沟通板的制作和使用'},
        {'type': 'h2', 'text': '制作步骤'},
        {'type': 'list', 'text': '1. 选择词汇：从最常用和最需要的开始'},
        {'type': 'list', 'text': '2. 准备图片：使用照片或符号'},
        {'type': 'list', 'text': '3. 组织布局：按类别或使用频率排列'},
        {'type': 'list', 'text': '4. 层压保护：使其耐用'},
        {'type': 'h2', 'text': '使用技巧'},
        {'type': 'list', 'text': '• 始终携带沟通板'},
        {'type': 'list', 'text': '• 示范如何使用'},
        {'type': 'list', 'text': '• 给予充足的反应时间'},
        {'type': 'list', 'text': '• 扩展儿童的沟通'},
        {'type': 'spacer'},

        {'type': 'h1', 'text': '四、高科技 AAC 设备'},
        {'type': 'h2', 'text': '常见类型'},
        {'type': 'list', 'text': '• 专用语音输出设备'},
        {'type': 'list', 'text': '• 平板电脑 AAC 应用'},
        {'type': 'list', 'text': '• 眼控沟通设备'},
        {'type': 'h2', 'text': '选择考虑因素'},
        {'type': 'list', 'text': '• 儿童的认知和运动能力'},
        {'type': 'list', 'text': '• 沟通需求和目标'},
        {'type': 'list', 'text': '• 便携性和耐用性'},
        {'type': 'list', 'text': '• 成本和支持服务'},
        {'type': 'h2', 'text': '常用 AAC 应用'},
        {'type': 'list', 'text': '• Proloquo2Go：功能全面的 AAC 应用'},
        {'type': 'list', 'text': '• TouchChat：可定制的沟通应用'},
        {'type': 'list', 'text': '• LAMP Words for Life：基于运动记忆的设计'},
        {'type': 'spacer'},

        {'type': 'h1', 'text': '五、成功实施 AAC 的关键'},
        {'type': 'list', 'text': '• 早期开始：不要等到"准备好"才开始'},
        {'type': 'list', 'text': '• 持续使用：AAC 应该随时可用'},
        {'type': 'list', 'text': '• 多模式沟通：结合多种方式'},
        {'type': 'list', 'text': '• 环境支持：所有人都学习使用'},
        {'type': 'list', 'text': '• 定期评估：根据进步调整系统'},
        {'type': 'body', 'text': 'AAC 不会阻碍口语发展，反而可能促进语言学习。重要的是给予儿童有效的沟通方式。'},
    ]
)

print("\nAll PDFs generated successfully!")

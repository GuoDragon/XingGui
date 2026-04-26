#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成真实的教育资源 PDF 文件
"""

from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, PageBreak
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib.enums import TA_CENTER, TA_LEFT, TA_JUSTIFY
import os

# 尝试注册中文字体
try:
    pdfmetrics.registerFont(TTFont('SimSun', 'C:/Windows/Fonts/simsun.ttc'))
    chinese_font = 'SimSun'
except:
    try:
        pdfmetrics.registerFont(TTFont('SimSun', '/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc'))
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

    # 标题样式
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

    # 一级标题样式
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

    # 二级标题样式
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

    # 正文样式
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

    # 列表样式
    list_style = ParagraphStyle(
        'CustomList',
        parent=styles['BodyText'],
        fontSize=11,
        spaceAfter=6,
        leftIndent=20,
        fontName=chinese_font,
        leading=18
    )

    # 添加标题
    story.append(Paragraph(title, title_style))
    story.append(Spacer(1, 0.3*inch))

    # 添加内容
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

# 切换到 PDF 目录
os.chdir('app/src/main/assets/resources/pdfs')

# 1. 特殊儿童语言发展训练方案
create_pdf(
    "res005_language_development.pdf",
    "特殊儿童语言发展训练方案",
    [
        {'type': 'h1', 'text': '一、语言发展评估'},
        {'type': 'body', 'text': '在开始训练之前，需要对儿童的语言能力进行全面评估，包括理解能力、表达能力、发音清晰度和社交沟通能力。'},
        {'type': 'h2', 'text': '1. 理解能力评估'},
        {'type': 'list', 'text': '• 能否理解简单指令（如"坐下"、"过来"）'},
        {'type': 'list', 'text': '• 能否识别常见物品名称'},
        {'type': 'list', 'text': '• 能否理解简单的问题'},
        {'type': 'h2', 'text': '2. 表达能力评估'},
        {'type': 'list', 'text': '• 词汇量大小'},
        {'type': 'list', 'text': '• 句子长度和复杂度'},
        {'type': 'list', 'text': '• 语法使用情况'},
        {'type': 'spacer', 'height': 0.2},

        {'type': 'h1', 'text': '二、训练目标设定'},
        {'type': 'body', 'text': '根据评估结果，制定个性化的训练目标。目标应该是具体的、可测量的、可实现的。'},
        {'type': 'h2', 'text': '短期目标（1-3个月）'},
        {'type': 'list', 'text': '• 增加词汇量 20-30 个'},
        {'type': 'list', 'text': '• 能说出 2-3 个词的短句'},
        {'type': 'list', 'text': '• 提高发音清晰度'},
        {'type': 'h2', 'text': '长期目标（6-12个月）'},
        {'type': 'list', 'text': '• 能进行简单对话'},
        {'type': 'list', 'text': '• 能表达基本需求'},
        {'type': 'list', 'text': '• 能回答简单问题'},
        {'type': 'spacer', 'height': 0.2},

        {'type': 'h1', 'text': '三、训练活动设计'},
        {'type': 'h2', 'text': '1. 词汇扩展活动'},
        {'type': 'body', 'text': '使用实物、图片和日常情境教授新词汇。每次引入 3-5 个新词，通过重复和强化帮助儿童记忆。'},
        {'type': 'list', 'text': '• 命名游戏：指认物品并说出名称'},
        {'type': 'list', 'text': '• 分类活动：将物品按类别分组'},
        {'type': 'list', 'text': '• 配对游戏：图片与实物配对'},
        {'type': 'h2', 'text': '2. 句子构建活动'},
        {'type': 'body', 'text': '从简单的两词组合开始，逐步增加句子长度和复杂度。'},
        {'type': 'list', 'text': '• 主语+动词：宝宝吃、妈妈走'},
        {'type': 'list', 'text': '• 主语+动词+宾语：宝宝吃饭、妈妈喝水'},
        {'type': 'list', 'text': '• 添加形容词：大苹果、红气球'},
        {'type': 'h2', 'text': '3. 社交沟通训练'},
        {'type': 'body', 'text': '在自然情境中练习语言使用，培养实用的沟通技能。'},
        {'type': 'list', 'text': '• 问候练习：你好、再见'},
        {'type': 'list', 'text': '• 请求表达：我要、给我'},
        {'type': 'list', 'text': '• 轮流对话：简单的一问一答'},
        {'type': 'spacer', 'height': 0.2},

        {'type': 'h1', 'text': '四、家庭训练建议'},
        {'type': 'body', 'text': '家庭是语言学习的重要场所。家长应该在日常生活中创造丰富的语言环境。'},
        {'type': 'list', 'text': '• 多与孩子交谈，使用简单清晰的语言'},
        {'type': 'list', 'text': '• 给孩子充足的反应时间'},
        {'type': 'list', 'text': '• 重复和扩展孩子的话语'},
        {'type': 'list', 'text': '• 使用手势和表情辅助理解'},
        {'type': 'list', 'text': '• 阅读图画书，讲述故事'},
        {'type': 'spacer', 'height': 0.2},

        {'type': 'h1', 'text': '五、进度监测'},
        {'type': 'body', 'text': '定期评估训练效果，根据儿童的进步调整训练计划。建议每月进行一次正式评估，记录词汇量、句子长度等指标的变化。'},
        {'type': 'body', 'text': '本方案仅供参考，具体实施应在专业语言治疗师的指导下进行。'},
    ]
)

# 2. ADHD 课堂管理
create_pdf(
    "res006_adhd_classroom_management.pdf",
    "注意力缺陷多动障碍（ADHD）课堂管理",
    [
        {'type': 'h1', 'text': '一、理解 ADHD'},
        {'type': 'body', 'text': 'ADHD 是一种神经发育障碍，主要表现为注意力不集中、多动和冲动。这些症状会影响儿童的学习和社交。'},
        {'type': 'h2', 'text': '核心症状'},
        {'type': 'list', 'text': '• 注意力不集中：难以专注于任务，容易分心'},
        {'type': 'list', 'text': '• 多动：坐不住，过度活跃'},
        {'type': 'list', 'text': '• 冲动：难以等待，打断他人'},
        {'type': 'spacer', 'height': 0.2},

        {'type': 'h1', 'text': '二、课堂环境调整'},
        {'type': 'h2', 'text': '1. 座位安排'},
        {'type': 'body', 'text': '将 ADHD 学生安排在靠近教师、远离干扰源的位置。'},
        {'type': 'list', 'text': '• 坐在教室前排，便于教师监督'},
        {'type': 'list', 'text': '• 远离窗户和门，减少外界干扰'},
        {'type': 'list', 'text': '• 旁边坐专注力好的同学，起到榜样作用'},
        {'type': 'h2', 'text': '2. 减少视觉干扰'},
        {'type': 'list', 'text': '• 保持课桌整洁，只放必需物品'},
        {'type': 'list', 'text': '• 使用文件夹或隔板减少视觉刺激'},
        {'type': 'list', 'text': '• 墙面装饰简洁，避免过度刺激'},
        {'type': 'spacer', 'height': 0.2},

        {'type': 'h1', 'text': '三、教学策略'},
        {'type': 'h2', 'text': '1. 结构化教学'},
        {'type': 'body', 'text': '提供清晰的结构和常规，帮助学生预测和准备。'},
        {'type': 'list', 'text': '• 使用视觉时间表展示每日活动'},
        {'type': 'list', 'text': '• 将任务分解为小步骤'},
        {'type': 'list', 'text': '• 提供明确的指令，一次一个'},
        {'type': 'h2', 'text': '2. 积极关注'},
        {'type': 'body', 'text': '及时表扬和强化适当行为，比惩罚更有效。'},
        {'type': 'list', 'text': '• 立即表扬良好行为'},
        {'type': 'list', 'text': '• 使用代币制度或积分系统'},
        {'type': 'list', 'text': '• 关注努力过程，而非仅结果'},
        {'type': 'h2', 'text': '3. 运动休息'},
        {'type': 'body', 'text': '定期提供运动机会，帮助释放多余能量。'},
        {'type': 'list', 'text': '• 每 20-30 分钟安排短暂休息'},
        {'type': 'list', 'text': '• 允许站立或使用坐垫'},
        {'type': 'list', 'text': '• 安排跑腿任务（如送文件）'},
        {'type': 'spacer', 'height': 0.2},

        {'type': 'h1', 'text': '四、行为管理技巧'},
        {'type': 'h2', 'text': '1. 预防策略'},
        {'type': 'list', 'text': '• 提前提醒即将发生的转换'},
        {'type': 'list', 'text': '• 使用视觉提示和手势'},
        {'type': 'list', 'text': '• 建立清晰的课堂规则'},
        {'type': 'h2', 'text': '2. 应对冲动行为'},
        {'type': 'list', 'text': '• 保持冷静，避免情绪化反应'},
        {'type': 'list', 'text': '• 使用"停-想-做"策略'},
        {'type': 'list', 'text': '• 提供冷静角供学生自我调节'},
        {'type': 'h2', 'text': '3. 家校合作'},
        {'type': 'body', 'text': '与家长保持密切沟通，确保策略的一致性。'},
        {'type': 'list', 'text': '• 定期分享学生进步'},
        {'type': 'list', 'text': '• 共同制定行为计划'},
        {'type': 'list', 'text': '• 提供家庭作业调整建议'},
        {'type': 'spacer', 'height': 0.2},

        {'type': 'h1', 'text': '五、注意事项'},
        {'type': 'body', 'text': '每个 ADHD 儿童都是独特的，需要个性化的支持。持续观察和调整策略，找到最适合该学生的方法。必要时，建议家长寻求专业医疗和心理支持。'},
    ]
)

# 3. 视觉支持工具
create_pdf(
    "res007_visual_supports.pdf",
    "视觉支持工具制作与应用",
    [
        {'type': 'h1', 'text': '一、什么是视觉支持'},
        {'type': 'body', 'text': '视觉支持是使用图片、符号、文字等视觉元素来帮助儿童理解信息、遵循常规和表达需求的工具。对于自闭症和其他特殊需要儿童特别有效。'},
        {'type': 'h2', 'text': '视觉支持的优势'},
        {'type': 'list', 'text': '• 信息持久可见，不像口头指令转瞬即逝'},
        {'type': 'list', 'text': '• 减少语言理解的负担'},
        {'type': 'list', 'text': '• 提供结构和可预测性'},
        {'type': 'list', 'text': '• 促进独立性'},
        {'type': 'spacer', 'height': 0.2},

        {'type': 'h1', 'text': '二、视觉时间表'},
        {'type': 'body', 'text': '视觉时间表展示一天或一段时间内的活动顺序，帮助儿童理解"接下来会发生什么"。'},
        {'type': 'h2', 'text': '制作步骤'},
        {'type': 'list', 'text': '1. 列出日常活动（如起床、吃饭、上学）'},
        {'type': 'list', 'text': '2. 为每个活动准备图片或照片'},
        {'type': 'list', 'text': '3. 按时间顺序排列图片'},
        {'type': 'list', 'text': '4. 使用魔术贴便于调整'},
        {'type': 'h2', 'text': '使用方法'},
        {'type': 'list', 'text': '• 每天早上与孩子一起查看时间表'},
        {'type': 'list', 'text': '• 完成一项活动后，移除或打勾'},
        {'type': 'list', 'text': '• 如有变化，提前更新时间表'},
        {'type': 'spacer', 'height': 0.2},

        {'type': 'h1', 'text': '三、社交故事'},
        {'type': 'body', 'text': '社交故事是用简单的文字和图片描述社交情境的短故事，帮助儿童理解社交规则和期望。'},
        {'type': 'h2', 'text': '编写原则'},
        {'type': 'list', 'text': '• 使用第一人称（"我"）'},
        {'type': 'list', 'text': '• 语言简单、具体'},
        {'type': 'list', 'text': '• 描述性句子多于指令性句子'},
        {'type': 'list', 'text': '• 配合相关图片'},
        {'type': 'h2', 'text': '示例主题'},
        {'type': 'list', 'text': '• 如何排队'},
        {'type': 'list', 'text': '• 轮流玩玩具'},
        {'type': 'list', 'text': '• 去看医生'},
        {'type': 'list', 'text': '• 在餐厅用餐'},
        {'type': 'spacer', 'height': 0.2},

        {'type': 'h1', 'text': '四、选择板'},
        {'type': 'body', 'text': '选择板展示多个选项，让儿童通过指认或拿取图片来表达选择。'},
        {'type': 'h2', 'text': '制作材料'},
        {'type': 'list', 'text': '• 硬纸板或泡沫板'},
        {'type': 'list', 'text': '• 图片（照片或绘画）'},
        {'type': 'list', 'text': '• 魔术贴或透明袋'},
        {'type': 'h2', 'text': '应用场景'},
        {'type': 'list', 'text': '• 选择食物：苹果、香蕉、饼干'},
        {'type': 'list', 'text': '• 选择活动：玩积木、看书、画画'},
        {'type': 'list', 'text': '• 选择衣服：红色、蓝色'},
        {'type': 'spacer', 'height': 0.2},

        {'type': 'h1', 'text': '五、提示卡'},
        {'type': 'body', 'text': '提示卡是小型的视觉提示，用于特定情境或行为。'},
        {'type': 'h2', 'text': '常用提示卡'},
        {'type': 'list', 'text': '• "等待"卡：提醒孩子需要等待'},
        {'type': 'list', 'text': '• "安静"卡：提示降低音量'},
        {'type': 'list', 'text': '• "帮助"卡：请求帮助的信号'},
        {'type': 'list', 'text': '• "休息"卡：需要休息的表达'},
        {'type': 'h2', 'text': '使用技巧'},
        {'type': 'list', 'text': '• 随身携带常用提示卡'},
        {'type': 'list', 'text': '• 教导孩子主动使用'},
        {'type': 'list', 'text': '• 及时响应孩子的使用'},
        {'type': 'spacer', 'height': 0.2},

        {'type': 'h1', 'text': '六、制作建议'},
        {'type': 'body', 'text': '制作视觉支持工具时，考虑以下因素：'},
        {'type': 'list', 'text': '• 使用真实照片比卡通图片更易理解'},
        {'type': 'list', 'text': '• 图片大小适中，清晰可见'},
        {'type': 'list', 'text': '• 耐用材料，可重复使用'},
        {'type': 'list', 'text': '• 根据孩子的理解水平调整复杂度'},
        {'type': 'body', 'text': '视觉支持工具需要持续使用才能发挥最大效果。随着孩子能力的提高，逐步减少支持，促进独立。'},
    ]
)

print("\n所有教育资源 PDF 已生成！")

#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成最后一批教育资源 PDF 文件
"""

from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib.enums import TA_CENTER, TA_JUSTIFY
import os

try:
    pdfmetrics.registerFont(TTFont('SimSun', 'C:/Windows/Fonts/simsun.ttc'))
    chinese_font = 'SimSun'
except:
    chinese_font = 'Helvetica'

def create_pdf(filename, title, sections):
    doc = SimpleDocTemplate(filename, pagesize=A4,
                           topMargin=0.75*inch, bottomMargin=0.75*inch,
                           leftMargin=0.75*inch, rightMargin=0.75*inch)
    story = []
    styles = getSampleStyleSheet()

    title_style = ParagraphStyle('CustomTitle', parent=styles['Heading1'],
        fontSize=20, textColor='#2E6CE6', spaceAfter=20, alignment=TA_CENTER,
        fontName=chinese_font if chinese_font != 'Helvetica' else 'Helvetica-Bold', leading=28)

    h1_style = ParagraphStyle('CustomH1', parent=styles['Heading2'],
        fontSize=16, spaceAfter=12, spaceBefore=16,
        fontName=chinese_font if chinese_font != 'Helvetica' else 'Helvetica-Bold',
        textColor='#1A1A1A', leading=22)

    h2_style = ParagraphStyle('CustomH2', parent=styles['Heading3'],
        fontSize=14, spaceAfter=10, spaceBefore=12,
        fontName=chinese_font if chinese_font != 'Helvetica' else 'Helvetica-Bold',
        textColor='#333333', leading=20)

    body_style = ParagraphStyle('CustomBody', parent=styles['BodyText'],
        fontSize=11, spaceAfter=8, alignment=TA_JUSTIFY,
        fontName=chinese_font, leading=18, firstLineIndent=22)

    list_style = ParagraphStyle('CustomList', parent=styles['BodyText'],
        fontSize=11, spaceAfter=6, leftIndent=20, fontName=chinese_font, leading=18)

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

# 7. 评估工具手册
create_pdf(
    "res011_assessment_tools.pdf",
    "特殊教育评估工具使用手册",
    [
        {'type': 'h1', 'text': '一、评估的目的'},
        {'type': 'body', 'text': '评估是特殊教育的基础，帮助我们了解儿童的能力、需求和进步。'},
        {'type': 'h2', 'text': '评估的作用'},
        {'type': 'list', 'text': '• 识别发展迟缓或障碍'},
        {'type': 'list', 'text': '• 确定教育需求'},
        {'type': 'list', 'text': '• 制定个别化教育计划'},
        {'type': 'list', 'text': '• 监测进步和调整干预'},
        {'type': 'spacer'},

        {'type': 'h1', 'text': '二、常用评估工具'},
        {'type': 'h2', 'text': '1. 发展筛查工具'},
        {'type': 'body', 'text': '用于快速识别可能存在发展问题的儿童。'},
        {'type': 'list', 'text': '• ASQ-3（年龄与阶段问卷）：家长填写的筛查工具'},
        {'type': 'list', 'text': '• M-CHAT（自闭症筛查量表）：18-24个月幼儿'},
        {'type': 'list', 'text': '• Denver II（丹佛发育筛查测验）：0-6岁'},
        {'type': 'h2', 'text': '2. 智力评估'},
        {'type': 'list', 'text': '• WISC（韦氏儿童智力量表）：6-16岁'},
        {'type': 'list', 'text': '• WPPSI（韦氏学前智力量表）：2.5-7岁'},
        {'type': 'list', 'text': '• Stanford-Binet：2岁以上'},
        {'type': 'h2', 'text': '3. 适应行为评估'},
        {'type': 'list', 'text': '• Vineland（文兰适应行为量表）'},
        {'type': 'list', 'text': '• ABAS（适应行为评定系统）'},
        {'type': 'h2', 'text': '4. 语言评估'},
        {'type': 'list', 'text': '• PPVT（皮博迪图片词汇测验）'},
        {'type': 'list', 'text': '• CELF（临床语言基础评估）'},
        {'type': 'spacer'},

        {'type': 'h1', 'text': '三、评估过程'},
        {'type': 'h2', 'text': '1. 准备阶段'},
        {'type': 'list', 'text': '• 收集背景信息'},
        {'type': 'list', 'text': '• 选择合适的评估工具'},
        {'type': 'list', 'text': '• 安排舒适的评估环境'},
        {'type': 'h2', 'text': '2. 实施阶段'},
        {'type': 'list', 'text': '• 建立良好关系'},
        {'type': 'list', 'text': '• 按标准程序进行'},
        {'type': 'list', 'text': '• 观察儿童的行为'},
        {'type': 'h2', 'text': '3. 结果解释'},
        {'type': 'list', 'text': '• 分析测试分数'},
        {'type': 'list', 'text': '• 结合观察和背景信息'},
        {'type': 'list', 'text': '• 识别优势和需求'},
        {'type': 'spacer'},

        {'type': 'h1', 'text': '四、评估结果的应用'},
        {'type': 'body', 'text': '评估不是目的，而是制定有效干预计划的手段。'},
        {'type': 'h2', 'text': '制定 IEP'},
        {'type': 'list', 'text': '• 基于评估结果设定目标'},
        {'type': 'list', 'text': '• 确定所需服务和支持'},
        {'type': 'list', 'text': '• 选择适当的教学策略'},
        {'type': 'h2', 'text': '监测进步'},
        {'type': 'list', 'text': '• 定期重新评估'},
        {'type': 'list', 'text': '• 记录技能获得情况'},
        {'type': 'list', 'text': '• 调整干预计划'},
        {'type': 'spacer'},

        {'type': 'h1', 'text': '五、评估的注意事项'},
        {'type': 'list', 'text': '• 使用多种评估方法'},
        {'type': 'list', 'text': '• 在不同情境中观察'},
        {'type': 'list', 'text': '• 考虑文化和语言因素'},
        {'type': 'list', 'text': '• 重视家长的输入'},
        {'type': 'list', 'text': '• 关注儿童的优势'},
        {'type': 'body', 'text': '评估应该是一个持续的过程，而非一次性事件。'},
    ]
)

# 8. 融合课堂教学
create_pdf(
    "res012_inclusive_teaching.pdf",
    "融合课堂教学策略与调整",
    [
        {'type': 'h1', 'text': '一、融合教育的理念'},
        {'type': 'body', 'text': '融合教育认为所有儿童，无论其能力如何，都有权在普通教育环境中学习。教师的任务是调整教学，满足所有学生的需求。'},
        {'type': 'spacer'},

        {'type': 'h1', 'text': '二、通用学习设计（UDL）'},
        {'type': 'body', 'text': 'UDL 是一种教学框架，旨在为所有学习者提供平等的学习机会。'},
        {'type': 'h2', 'text': '三个原则'},
        {'type': 'list', 'text': '1. 多种呈现方式：用不同方式展示信息'},
        {'type': 'list', 'text': '2. 多种表达方式：允许学生用不同方式展示学习'},
        {'type': 'list', 'text': '3. 多种参与方式：提供多样化的学习活动'},
        {'type': 'h2', 'text': '实践示例'},
        {'type': 'list', 'text': '• 使用视觉、听觉和动手材料'},
        {'type': 'list', 'text': '• 提供文字、口头和图片说明'},
        {'type': 'list', 'text': '• 允许写作、绘画或口头报告'},
        {'type': 'spacer'},

        {'type': 'h1', 'text': '三、差异化教学'},
        {'type': 'h2', 'text': '1. 内容差异化'},
        {'type': 'body', 'text': '调整学习材料的难度和复杂度。'},
        {'type': 'list', 'text': '• 提供不同阅读水平的文本'},
        {'type': 'list', 'text': '• 使用分层作业'},
        {'type': 'list', 'text': '• 提供额外的支持材料'},
        {'type': 'h2', 'text': '2. 过程差异化'},
        {'type': 'body', 'text': '调整学习活动和教学方法。'},
        {'type': 'list', 'text': '• 小组教学'},
        {'type': 'list', 'text': '• 同伴辅导'},
        {'type': 'list', 'text': '• 使用学习中心'},
        {'type': 'h2', 'text': '3. 产出差异化'},
        {'type': 'body', 'text': '允许学生用不同方式展示学习成果。'},
        {'type': 'list', 'text': '• 项目、报告或演示'},
        {'type': 'list', 'text': '• 口头、书面或视觉呈现'},
        {'type': 'list', 'text': '• 个人或小组作业'},
        {'type': 'spacer'},

        {'type': 'h1', 'text': '四、课堂调整策略'},
        {'type': 'h2', 'text': '1. 环境调整'},
        {'type': 'list', 'text': '• 优先座位安排'},
        {'type': 'list', 'text': '• 减少干扰'},
        {'type': 'list', 'text': '• 提供安静工作区'},
        {'type': 'h2', 'text': '2. 教学调整'},
        {'type': 'list', 'text': '• 分解任务为小步骤'},
        {'type': 'list', 'text': '• 提供额外时间'},
        {'type': 'list', 'text': '• 使用视觉辅助'},
        {'type': 'list', 'text': '• 重复和强化关键概念'},
        {'type': 'h2', 'text': '3. 评估调整'},
        {'type': 'list', 'text': '• 口头测试代替书面测试'},
        {'type': 'list', 'text': '• 延长测试时间'},
        {'type': 'list', 'text': '• 减少题目数量'},
        {'type': 'list', 'text': '• 允许使用辅助工具'},
        {'type': 'spacer'},

        {'type': 'h1', 'text': '五、促进社交融合'},
        {'type': 'body', 'text': '学业融合只是一部分，社交融合同样重要。'},
        {'type': 'h2', 'text': '策略'},
        {'type': 'list', 'text': '• 合作学习活动'},
        {'type': 'list', 'text': '• 同伴支持系统'},
        {'type': 'list', 'text': '• 全班社交技能教学'},
        {'type': 'list', 'text': '• 庆祝多样性'},
        {'type': 'spacer'},

        {'type': 'h1', 'text': '六、与支持人员合作'},
        {'type': 'body', 'text': '融合教育需要团队合作。'},
        {'type': 'list', 'text': '• 与特教老师共同计划'},
        {'type': 'list', 'text': '• 与治疗师协调'},
        {'type': 'list', 'text': '• 培训助教'},
        {'type': 'list', 'text': '• 与家长沟通'},
        {'type': 'body', 'text': '成功的融合教育让所有学生受益，创造一个接纳和支持的学习环境。'},
    ]
)

print("\nFinal batch of PDFs generated!")

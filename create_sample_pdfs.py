#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成示例 PDF 文件
"""

try:
    from reportlab.lib.pagesizes import letter, A4
    from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
    from reportlab.lib.units import inch
    from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, PageBreak
    from reportlab.pdfbase import pdfmetrics
    from reportlab.pdfbase.ttfonts import TTFont
    from reportlab.lib.enums import TA_CENTER, TA_LEFT

    # 注册中文字体（如果有的话）
    try:
        pdfmetrics.registerFont(TTFont('SimSun', 'simsun.ttc'))
        chinese_font = 'SimSun'
    except:
        chinese_font = 'Helvetica'

    def create_pdf(filename, title, content_lines):
        doc = SimpleDocTemplate(filename, pagesize=A4)
        story = []
        styles = getSampleStyleSheet()

        # 标题样式
        title_style = ParagraphStyle(
            'CustomTitle',
            parent=styles['Heading1'],
            fontSize=24,
            textColor='#2E6CE6',
            spaceAfter=30,
            alignment=TA_CENTER,
            fontName=chinese_font if chinese_font != 'Helvetica' else 'Helvetica-Bold'
        )

        # 正文样式
        body_style = ParagraphStyle(
            'CustomBody',
            parent=styles['BodyText'],
            fontSize=12,
            spaceAfter=12,
            alignment=TA_LEFT,
            fontName=chinese_font
        )

        # 添加标题
        story.append(Paragraph(title, title_style))
        story.append(Spacer(1, 0.5*inch))

        # 添加内容
        for line in content_lines:
            story.append(Paragraph(line, body_style))
            story.append(Spacer(1, 0.2*inch))

        doc.build(story)
        print(f"Created: {filename}")

    # 创建融合教育指南
    create_pdf(
        "res001_inclusive_education_guide.pdf",
        "融合教育政策要点解读",
        [
            "<b>什么是融合教育？</b>",
            "融合教育是指让特殊需要儿童在普通学校的普通班级中，与其他儿童一起接受教育的教育形式。",
            "",
            "<b>核心理念</b>",
            "• 尊重差异，接纳多样性",
            "• 提供适当的支持和调整",
            "• 促进所有学生的全面发展",
            "",
            "<b>实施要点</b>",
            "1. 建立支持系统",
            "2. 调整课程和教学方法",
            "3. 加强教师培训",
            "4. 促进家校合作",
            "",
            "<b>政策支持</b>",
            "国家和地方政府提供政策、资金和专业支持，确保融合教育的有效实施。",
            "",
            "本文档为示例内容，仅供参考。"
        ]
    )

    # 创建 IEP 编写指南
    create_pdf(
        "res002_iep_writing_guide.pdf",
        "个别化教育计划（IEP）编写指南",
        [
            "<b>什么是 IEP？</b>",
            "个别化教育计划（Individualized Education Program）是为特殊需要学生制定的个性化教育方案。",
            "",
            "<b>IEP 的组成部分</b>",
            "1. 学生现状评估",
            "2. 年度教育目标",
            "3. 特殊教育和相关服务",
            "4. 参与普通教育的程度",
            "5. 评估方法和时间表",
            "",
            "<b>编写步骤</b>",
            "• 收集评估数据",
            "• 召开 IEP 会议",
            "• 制定具体目标",
            "• 确定支持服务",
            "• 定期评估和调整",
            "",
            "<b>目标设定原则</b>",
            "目标应该是具体的、可测量的、可实现的、相关的和有时限的（SMART原则）。",
            "",
            "本文档为示例内容，仅供参考。"
        ]
    )

    # 创建自闭症支持策略
    create_pdf(
        "res003_autism_support_strategies.pdf",
        "自闭症儿童教育支持策略",
        [
            "<b>理解自闭症谱系障碍（ASD）</b>",
            "自闭症是一种神经发育障碍，影响社交沟通和行为模式。",
            "",
            "<b>课堂支持策略</b>",
            "1. 结构化教学环境",
            "2. 视觉支持工具",
            "3. 社交技能训练",
            "4. 感觉调节支持",
            "5. 积极行为支持",
            "",
            "<b>沟通支持</b>",
            "• 使用清晰、简洁的语言",
            "• 提供视觉提示",
            "• 允许额外的处理时间",
            "• 使用辅助沟通工具",
            "",
            "<b>行为管理</b>",
            "• 建立清晰的规则和期望",
            "• 使用正面强化",
            "• 提供预警和过渡支持",
            "• 识别和管理触发因素",
            "",
            "本文档为示例内容，仅供参考。"
        ]
    )

    # 创建感统训练活动手册
    create_pdf(
        "res004_sensory_integration_activities.pdf",
        "感觉统合训练活动手册",
        [
            "<b>什么是感觉统合？</b>",
            "感觉统合是大脑组织和处理来自身体和环境的感觉信息的能力。",
            "",
            "<b>常见感觉系统</b>",
            "• 触觉系统",
            "• 前庭系统（平衡）",
            "• 本体感觉系统（身体位置）",
            "• 视觉系统",
            "• 听觉系统",
            "",
            "<b>家庭活动示例</b>",
            "1. 触觉活动：玩沙子、橡皮泥",
            "2. 前庭活动：荡秋千、旋转游戏",
            "3. 本体感觉活动：推拉重物、爬行",
            "4. 精细动作：串珠子、剪纸",
            "",
            "<b>课堂活动示例</b>",
            "• 感觉休息区",
            "• 运动休息时间",
            "• 感觉友好的座位选择",
            "• 手部操作活动",
            "",
            "本文档为示例内容，仅供参考。"
        ]
    )

    print("\n所有示例 PDF 文件创建成功！")

except ImportError:
    print("错误：需要安装 reportlab 库")
    print("请运行：pip install reportlab")

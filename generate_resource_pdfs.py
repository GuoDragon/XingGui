
#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Generate original XingGui PDF resources for res001-res012.

This script is intentionally dependency-light: it reads the canonical resource
titles from app/src/main/assets/data/resources.json and uses a local xelatex
installation to create PDFs with embedded fonts. Body content is app-owned,
educational material tailored to each resource title.
"""

from __future__ import annotations

import json
import shutil
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parent
DATA_FILE = ROOT / "app/src/main/assets/data/resources.json"
PDF_DIR = ROOT / "app/src/main/assets/resources/pdfs"
SOURCE_DIR = ROOT / "app/src/main/assets/resources/pdf_sources"
BUILD_DIR = ROOT / ".tmp/resource_pdf_build"

RESOURCE_CONTENT = {
    "res001": [
        ("核心理念", [
            "融合教育不是把儿童简单放进普通班级，而是用环境调整、课程弹性和同伴支持，让每个儿童都能有尊严地参与学习。",
            "判断一项支持是否有效，应看儿童是否更愿意参与、更能表达需要，并在真实课堂中逐步减少成人提示。",
        ]),
        ("政策落地清单", [
            "为儿童建立支持画像，记录兴趣优势、敏感情境、沟通方式、有效提示和合理便利措施。",
            "采用通用学习设计，在同一目标下提供图片、实物、口语说明、动作示范和同伴示范等多种进入方式。",
            "把支持嵌入日常流程，包括座位安排、活动转换、任务长度、反馈频率、休息方式和家校沟通。",
        ]),
        ("团队协作", [
            "每月至少一次由家长、班主任、特教教师和相关专业人员共同查看观察记录与作品样本。",
            "会议重点不是追责，而是确认哪些支持需要保留、淡化或加强，并写入下一阶段行动清单。",
        ]),
    ],
    "res002": [
        ("IEP 基本结构", [
            "一份可执行的个别化教育计划应包含现有能力、长期目标、短期目标、服务安排、便利措施、评估方法、责任人和复盘日期。",
            "现有能力描述要来自自然情境中的可观察表现，不能只写诊断标签或一次测验分数。",
        ]),
        ("可测量目标写法", [
            "目标建议写清条件、行为和标准：在什么支持下，儿童完成什么行为，达到怎样的可测量水平。",
            "示例：给出视觉流程表后，儿童能在一周五天中至少四天独立完成三步入园流程。",
        ]),
        ("进度记录", [
            "每个目标只选择一种主要记录方式，例如次数、持续时间、等级量表、作品样本或观察记录。",
            "复盘时同时比较基线、当前表现和提示强度，决定是提高标准、调整策略还是延长练习周期。",
        ]),
    ],
    "res003": [
        ("理解支持需求", [
            "自闭症儿童可能在沟通、社交理解、感觉调节和活动转换方面需要明确、稳定且可预期的支持。",
            "选择策略前先观察行为发生前后的线索。同样的行为可能来自焦虑、感觉过载、语言不清或缺少表达工具。",
        ]),
        ("课堂策略", [
            "使用视觉日程，提前预告转换，并为每个活动提供清晰的开始和结束信号。",
            "把任务拆成小步骤，儿童完成一步就给予即时、具体的反馈，而不是等整项任务结束再评价。",
            "根据需要提供安静角、降噪耳机、活动休息或手部小工具，帮助儿童恢复参与状态。",
        ]),
        ("沟通支持", [
            "接纳口语、手势、图片、书写和辅助沟通系统等多种表达方式，不把说话作为唯一合格的沟通形式。",
            "成人应先示范如何表达请求、拒绝、评论和求助，再逐步等待儿童尝试。",
        ]),
    ],
    "res004": [
        ("安全原则", [
            "感觉统合活动的目的在于支持学习和日常参与，不应为了追求强刺激而增加儿童负担。",
            "实施前要确认场地安全、儿童意愿、医疗限制和成人看护，出现不适时立即停止。",
        ]),
        ("本体觉活动", [
            "推墙、动物走、搬软垫、拉弹力带和运送较重的课堂材料，都可以帮助儿童感受身体位置和力量。",
            "每轮活动以三到五分钟为宜，观察儿童是否疲劳、烦躁或过度兴奋，再决定是否继续。",
        ]),
        ("前庭与触觉活动", [
            "平衡路径、轻柔摇摆、跳跃游戏、触觉箱、黏土和布料配对应循序渐进，避免突然提高难度。",
            "对触觉敏感的儿童可先用工具接触材料，再过渡到短时间手部接触。",
        ]),
    ],
    "res005": [
        ("训练重点", [
            "语言发展训练应同时关注理解、表达、轮流、共同注意和功能性沟通，而不只是增加词汇数量。",
            "目标是帮助儿童能请求、拒绝、评论、求助和分享兴趣，让语言真正服务于生活。",
        ]),
        ("家庭情境", [
            "亲子共读时先描述图片，再停顿等待儿童回应，并在儿童已有表达上做一点扩展。",
            "把喜欢的物品放在看得到但拿不到的位置，制造自然请求机会，同时接纳口语、手势、图片或 AAC。",
        ]),
        ("学校情境", [
            "上课前预教关键词，使用主题词板，并提供“我想要”“轮到我”“请帮忙”等句式起点。",
            "教师应记录儿童在哪些活动中最愿意表达，再把高动机活动安排进下一轮训练。",
        ]),
    ],
    "res006": [
        ("课堂环境调整", [
            "ADHD 学生常在持续注意、冲动控制、组织材料和时间管理方面遇到困难，需要外部结构支持。",
            "座位可靠近教师和任务材料，远离门窗与高流量通道，桌面只保留当前任务所需物品。",
        ]),
        ("任务设计", [
            "把长作业拆成五到十分钟的小段，每完成一段就用勾选、简短反馈或计划内微休息作为结束信号。",
            "使用计时器、检查清单、书面步骤和颜色标记，让学生知道先做什么、完成到哪里、作业交到哪里。",
        ]),
        ("行为支持", [
            "对期望行为进行即时且具体的强化，例如开始动笔、举手发言、按清单整理材料。",
            "提前安排合理活动量，如分发材料、站立书写、伸展或短距离跑腿，避免把所有移动都视为问题行为。",
        ]),
    ],
    "res007": [
        ("工具用途", [
            "视觉支持能降低口语理解压力，因为信息可以停留在环境中，儿童能够反复查看。",
            "它帮助儿童理解流程、规则、选择和期待，而不是完全依赖成人不断提醒。",
        ]),
        ("常用工具", [
            "视觉日程用于呈现活动顺序，完成后可以勾选或取下，帮助儿童理解时间推进。",
            "规则卡应写正向行为，例如“眼睛看材料”“手放桌面”，少用只说明禁止事项的表达。",
            "选择板可用于选择活动、奖励、伙伴、休息方式或沟通信息，增加儿童主动性。",
        ]),
        ("淡化支持", [
            "当儿童能稳定完成任务后，可逐步减少图片数量、缩小提示尺寸，或转为自我检查清单。",
            "淡化速度要依据数据决定，不能因为成人希望更快独立而突然撤掉全部支持。",
        ]),
    ],
    "res008": [
        ("早期干预价值", [
            "早期干预通过日常活动促进沟通、游戏、动作、自理和社会参与，重点是让练习自然发生在生活中。",
            "家长不需要变成治疗师，而是作为最了解儿童生活的人，与专业人员共同选择可持续的策略。",
        ]),
        ("家庭练习", [
            "每天选择两个稳定流程，例如点心、穿衣、洗澡或睡前阅读，把目标设计得小而可重复。",
            "先跟随儿童兴趣并模仿，再加入一个新动作或新词，给儿童足够等待时间。",
            "记录有效动机、困难时段和成功策略，便于团队复盘时调整计划。",
        ]),
        ("照护者支持", [
            "干预计划必须可持续。如果某个策略让家庭长期疲惫，应降低频率或与团队重新排序目标。",
            "照护者的休息、情绪和获得支持的渠道，也是儿童发展环境的重要组成部分。",
        ]),
    ],
    "res009": [
        ("课程目标", [
            "社交技能训练要把抽象能力转化为可观察行为，例如发起互动、等待、回应、协商、拒绝和修复冲突。",
            "目标应选自儿童真实社交场景，避免只在训练室中完成而无法迁移。",
        ]),
        ("教学流程", [
            "每次只教一个核心技能，可用图片或视频解释，再示范正确与不合适的例子，随后进行角色扮演。",
            "练习后尽快安排真实活动，如课间、午餐或合作游戏，让儿童在自然情境中使用同一技能。",
        ]),
        ("泛化应用", [
            "学校和家庭应使用一致的提示词，帮助儿童在不同地点识别同一个社交要求。",
            "如果儿童出现回避，先降低社交复杂度，再逐步增加伙伴数量、等待时间或对话轮次。",
        ]),
    ],
    "res010": [
        ("AAC 是什么", [
            "辅助沟通系统包括手势、图片、沟通板、书写、语音输出设备和应用程序，目的是让儿童拥有可用的表达通道。",
            "AAC 不会阻碍口语发展，反而能在口语尚未稳定时减少挫败，帮助儿童更早参与互动。",
        ]),
        ("选择系统", [
            "选择 AAC 时要综合考虑视觉、动作操作、理解能力、语言背景、家庭流程、学校场景和儿童最需要表达的信息。",
            "系统不宜只包含名词，还应包含请求、拒绝、评论、求助、情绪和社交问候等功能性信息。",
        ]),
        ("教学策略", [
            "成人应先示范 AAC 的使用。例如递饼干时指向“想要”和“饼干”，不要求儿童立即完全正确。",
            "在高动机活动中等待儿童尝试，并把不完整的指点、眼神或声音都当作有意义的沟通信号。",
        ]),
    ],
    "res011": [
        ("评估目的", [
            "特殊教育评估用于理解儿童当前能力、参与障碍和支持需求，目的是指导教学，而不是降低期待。",
            "评估结论应能回答下一步教什么、怎样教、由谁支持、多久复盘。",
        ]),
        ("多元资料", [
            "可结合标准化测验、课程本位测量、课堂观察、家庭访谈和作品样本，避免只依赖单一工具。",
            "当语言、文化、焦虑或动作操作影响表现时，更需要在不同情境中验证结果。",
        ]),
        ("结果转化", [
            "评估发现要转化为可教学的支持。例如听觉工作记忆弱，可转化为书面步骤、复述检查和视觉提醒。",
            "报告建议应写成可执行行动，明确负责人、频率和观察指标。",
        ]),
    ],
    "res012": [
        ("通用学习设计", [
            "融合课堂应预先设计多种接收信息、表达学习和保持参与的方式，而不是等儿童失败后再临时补救。",
            "同一学习目标可以通过阅读、图片、实物、讨论、操作或短视频进入，降低单一路径带来的障碍。",
        ]),
        ("差异化教学", [
            "内容差异化可以使用不同阅读难度、图片、实物或预教词汇。",
            "过程差异化可以安排独立练习、小组合作、同伴辅导、学习站和教师指导练习。",
            "成果差异化可允许口头报告、绘画、模型、短写作、录音或演示，只要能体现核心目标。",
        ]),
        ("同伴支持", [
            "同伴支持应是互惠的。教师要轮换伙伴、明确角色，并教全班如何尊重地协作。",
            "不要让同伴长期承担成人职责，成人仍需负责观察、指导和及时调整支持。",
        ]),
    ],
}

LATEX_SPECIALS = {
    "\\": r"\textbackslash{}",
    "&": r"\&",
    "%": r"\%",
    "$": r"\$",
    "#": r"\#",
    "_": r"\_",
    "{": r"\{",
    "}": r"\}",
    "~": r"\textasciitilde{}",
    "^": r"\textasciicircum{}",
}


def latex_escape(text: str) -> str:
    return "".join(LATEX_SPECIALS.get(ch, ch) for ch in text)


def load_resource_items() -> dict[str, dict]:
    items = json.loads(DATA_FILE.read_text(encoding="utf-8"))
    return {item["resourceId"]: item for item in items if item["resourceId"] in RESOURCE_CONTENT}


def build_latex(title: str, sections: list[tuple[str, list[str]]]) -> str:
    lines = [
        r"\documentclass[11pt]{article}",
        r"\usepackage[a4paper,margin=2.2cm]{geometry}",
        r"\usepackage{xcolor}",
        r"\usepackage{fontspec}",
        r"\usepackage{xeCJK}",
        r"\usepackage{enumitem}",
        r"\setmainfont{Times New Roman}",
        r"\setCJKmainfont{SimSun}",
        r"\setlist[itemize]{leftmargin=1.4em,itemsep=0.35em,topsep=0.35em}",
        r"\setlength{\parindent}{0pt}",
        r"\setlength{\parskip}{0.55em}",
        r"\pagestyle{plain}",
        r"\begin{document}",
        r"{\color[HTML]{1F5FD2}\LARGE\bfseries " + latex_escape(title) + r"\par}",
        r"{\color[HTML]{667085}\small 星轨原创教育资源：用于家庭与学校共同制定支持计划。\par}",
        r"\vspace{0.8em}",
    ]
    for heading, paragraphs in sections:
        lines.append(r"{\color[HTML]{111827}\large\bfseries " + latex_escape(heading) + r"\par}")
        lines.append(r"\begin{itemize}")
        for paragraph in paragraphs:
            lines.append(r"\item " + latex_escape(paragraph))
        lines.append(r"\end{itemize}")
    lines.extend([
        r"\vfill",
        r"{\color[HTML]{667085}\footnotesize 本材料为应用内原创教育内容，不能替代医疗、法律或正式评估建议。\par}",
        r"\end{document}",
    ])
    return "\n".join(lines) + "\n"


def write_pdf(resource_id: str, item: dict, sections: list[tuple[str, list[str]]]) -> None:
    xelatex = shutil.which("xelatex")
    if not xelatex:
        raise RuntimeError("xelatex is required to generate PDF resources")

    BUILD_DIR.mkdir(parents=True, exist_ok=True)
    tex_path = BUILD_DIR / f"{resource_id}.tex"
    tex_path.write_text(build_latex(item["title"], sections), encoding="utf-8")
    subprocess.run(
        [
            xelatex,
            "-interaction=nonstopmode",
            "-halt-on-error",
            "-output-directory",
            BUILD_DIR.as_posix(),
            tex_path.as_posix(),
        ],
        check=True,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.STDOUT,
    )
    pdf_path = PDF_DIR / Path(item["assetPath"]).name
    pdf_path.write_bytes((BUILD_DIR / f"{resource_id}.pdf").read_bytes())


def write_source(resource_id: str, item: dict, sections: list[tuple[str, list[str]]]) -> None:
    lines = [f"# {item['title']}", "", "星轨原创教育资源。", ""]
    for heading, paragraphs in sections:
        lines.extend([f"## {heading}", ""])
        for paragraph in paragraphs:
            lines.extend([f"- {paragraph}", ""])
    source_name = f"{resource_id}_{Path(item['assetPath']).stem}.md"
    (SOURCE_DIR / source_name).write_text("\n".join(lines).strip() + "\n", encoding="utf-8")


def main() -> None:
    PDF_DIR.mkdir(parents=True, exist_ok=True)
    SOURCE_DIR.mkdir(parents=True, exist_ok=True)
    items = load_resource_items()
    for resource_id in sorted(RESOURCE_CONTENT):
        item = items[resource_id]
        sections = RESOURCE_CONTENT[resource_id]
        write_pdf(resource_id, item, sections)
        write_source(resource_id, item, sections)
        print(f"generated {Path(item['assetPath']).name}")


if __name__ == "__main__":
    main()

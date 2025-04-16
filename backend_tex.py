import re
import json
from collections import defaultdict
from flask import Flask, request, jsonify
from flask_cors import CORS

app = Flask(__name__)
CORS(app)


class ValidationMessage:
    def __init__(self, code, message, suggestion, content):
        self.code = code
        self.message = message
        self.suggestion = suggestion
        self.content = content

    def to_dict(self):
        return self.__dict__


class ValidationStats:
    def __init__(self):
        self.total_paragraphs = 0
        self.total_errors = 0
        self.error_type_count = defaultdict(int)

    def to_dict(self):
        return {
            "totalParagraphs": self.total_paragraphs,
            "totalErrors": self.total_errors,
            "errorTypeCount": dict(self.error_type_count)
        }


class ValidationResponse:
    def __init__(self, messages, stats):
        self.messages = [m.to_dict() for m in messages]
        self.stats = stats.to_dict()

    def to_json(self):
        return json.dumps(self.__dict__, ensure_ascii=False, indent=2)


def remove_comments(tex_str):
    tex_str = re.sub(r'%.*', '', tex_str)
    tex_str = re.sub(r'\\begin{comment}.*?\\end{comment}', '', tex_str, flags=re.DOTALL)

    return tex_str


def validate_tex(tex_str):
    print(tex_str)
    result = []
    stats = ValidationStats()

    texts_to_check_title = [
        {"words": ["Министерство", "науки", "и", "высшего", "образования", "Российской", "Федерации"],
         "label": "Font1"},
        {"words": ["ФЕДЕРАЛЬНОЕ", "ГОСУДАРСТВЕННОЕ", "АВТОНОМНОЕ", "ОБРАЗОВАТЕЛЬНОЕ", "УЧРЕЖДЕНИЕ", "ВЫСШЕГО",
                   "ОБРАЗОВАНИЯ"], "label": "Font2"},
        {"words": ["НАЦИОНАЛЬНЫЙ", "ИССЛЕДОВАТЕЛЬСКИЙ", "УНИВЕРСИТЕТ", "ИТМО", "ITMO", "University"], "label": "Font3"},
        {"words": ["ВЫПУСКНАЯ", "КВАЛИФИКАЦИОННАЯ", "РАБОТА"], "label": "Title1"},
        {"words": ["GRADUATION", "THESIS"], "label": "Title2"},
        {"words": ["Разработка", "интеллектуальной", "системы", "управления", "складом", "для", "организаций", "малого",
                   "бизнеса"], "label": "Subtitle"}
    ]

    texts_to_check_bold = [
        {"expected": "Обучающийся / Student:", "label": "Student"},
        {"expected": "Факультет / Faculty:", "label": "Faculty"},
        {"expected": "Группа / Group:", "label": "Group"},
        {"expected": "Направление подготовки / Subject area:", "label": "Subject Area"},
        {"expected": "Образовательная программа / Educational program:", "label": "Educational Program"},
        {"expected": "Язык реализации ОП / Language of the educational program:",
         "label": "Language of Educational Program"},
        {"expected": "Квалификация / Degree level:", "label": "Degree Level"},
        {"expected": "Руководитель ВКР / Thesis supervisor:", "label": "Thesis Supervisor"},
    ]

    texts_to_check_section = [
        {"phrase": "СПИСОК СОКРАЩЕНИЙ И УСЛОВНЫХ ОБОЗНАЧЕНИЙ", "label": "Section1"},
        {"phrase": "ТЕРМИНЫ И ОПРЕДЕЛЕНИЯ", "label": "Section2"},
        {"phrase": "ВВЕДЕНИЕ", "label": "Section3"},
        {"phrase": "ЗАКЛЮЧЕНИЕ", "label": "Section4"},
        {"phrase": "СПИСОК ИСПОЛЬЗОВАННЫХ ИСТОЧНИКОВ", "label": "Section5"}
    ]

    # 有没有目录
    if not re.search(r'\\tableofcontents', tex_str, re.MULTILINE):
        result.append(ValidationMessage(
            "MissingTOC",
            "未找到有效的目录命令 '\\tableofcontents'",
            "请确保在文档中包含 '\\tableofcontents' 来生成目录。",
            ""
        ))
        stats.error_type_count["MissingTOC"] += 1

    # 检查目录是否放置在单独的一页上
    toc_match = re.search(r'\\tableofcontents', tex_str)
    if toc_match:
        toc_position = toc_match.start()  # 获取 \tableofcontents 命令的位置
        if not re.search(r'(\\end{titlepage}|\\(newpage|clearpage))', tex_str[:toc_position]):
            result.append(ValidationMessage(
                "TOCNotOnNewPageBefore",
                "目录前未找到有效的分页命令",
                "请确保在 \\tableofcontents 前使用 \\newpage 或 \\clearpage 来将目录放置在单独的一页",
                "Перед полям \\tableofcontents"
            ))
            stats.error_type_count["TOCNotOnNewPageBefore"] += 1

        toc_end_position = toc_match.end()  # 获取 \tableofcontents 结束位置
        if not re.search(r'\\(newpage|clearpage)\s*', tex_str[toc_end_position:]):
            result.append(ValidationMessage(
                "TOCNotOnNewPageAfter",
                "目录后未放置分页命令",
                "请确保在 \\tableofcontents 后使用 \\newpage 或 \\clearpage 来将目录放置在单独的一页。",
                "После поля \\tableofcontents"
            ))
            stats.error_type_count["TOCNotOnNewPageAfter"] += 1

    # 字体
    if not re.search(r'\\setmainfont\{Times New Roman\}', tex_str) and \
            not re.search(r'\\usepackage(\[.*?\])?\{times\}', tex_str):
        result.append(ValidationMessage(
            "FontMismatch",
            "未设置 Times New Roman 字体",
            "建议使用 \\setmainfont{Times New Roman} 或 \\usepackage{times}",
            "\\setmainfont或\\usepackage 设置段"
        ))
        stats.error_type_count["FontMismatch"] += 1

    # 字号
    match = re.search(r'\\documentclass(\[.*?\])?', tex_str)
    if match:
        options = match.group(1) or ""
        if '14pt' not in options:
            result.append(ValidationMessage(
                "FontSizeMismatch",
                f"未设置字号为 14pt，当前设置为：{options}",
                "请设置 documentclass 为 \\documentclass[14pt]{...}",
                "\\documentclass 设置段"
            ))
            stats.error_type_count["FontSizeMismatch"] += 1

    # A4
    if not re.search(r'\\usepackage\[[^\]]*a4paper[^\]]*\]\{geometry\}', tex_str):
        result.append(ValidationMessage(
            "PageSizeMismatch",
            "页面尺寸不是 A4",
            "请在文档中设置 A4 页面尺寸，例如使用 \\usepackage[a4paper]{geometry}",
            "\\usepackage[a4paper]{geometry} 设置段"
        ))
        stats.error_type_count["PageSizeMismatch"] += 1

    # 对齐
    if not re.search(r'\\raggedright', tex_str) and \
            not re.search(r'\\begin\{flushleft\}', tex_str):
        result.append(ValidationMessage(
            "AlignmentError",
            "文档可能未设置左对齐",
            "建议添加 \\raggedright 或使用 \\begin{flushleft} ... \\end{flushleft}",
            "\\raggedright 或 \\begin{flushleft} ... \\end{flushleft} 设置段"
        ))
        stats.error_type_count["AlignmentError"] += 1

    center_blocks = re.findall(r'\\begin{center}(.*?)\\end{center}', tex_str, re.DOTALL)
    center_content = "\n".join(center_blocks)

    center_content = center_content.replace("\\", " ").replace("\n", " ").replace("  ", " ")

    # 居中内容检查（逐个单词）
    for item in texts_to_check_title:
        label = item["label"]
        words = item["words"]
        missing_words = [word for word in words if word not in center_content]

        if missing_words:
            result.append(ValidationMessage(
                "AlignmentError",
                f"{label} 中以下词语未居中对齐：{'，'.join(missing_words)}",  # f"未检测到居中对齐的 {label}",
                "请确保所有词语在 \\begin{center} ... \\end{center} 中。",
                " ".join(words)
            ))
            stats.error_type_count["AlignmentError"] += 1

    # 检查加粗显示和文本内容
    for item in texts_to_check_bold:
        expected_text = item["expected"]
        label = item["label"]

        if expected_text not in tex_str:
            result.append(ValidationMessage(
                "ContentMismatch",
                f"未找到规范文本: '{expected_text}'",
                f"请确保文档中包含以下文本: '{expected_text}'",
                expected_text
            ))
            stats.error_type_count["ContentMismatch"] += 1
        else:
            bold_pattern = r'\\textbf\{' + re.escape(expected_text) + r'\}'
            if not re.search(bold_pattern, tex_str):
                result.append(ValidationMessage(
                    "BoldError",
                    f"文本未加粗: '{expected_text}'",
                    f"请确保 '{expected_text}' 使用 \\textbf{{}} 进行加粗",
                    expected_text
                ))
                stats.error_type_count["BoldError"] += 1

    # 检查无编号标题
    unnumbered_sections = re.findall(r'\\section\*\{([^}]+)\}', tex_str)
    for section in texts_to_check_section:
        # 如果预期的短语没有出现在用户的标题中
        if section["phrase"] not in unnumbered_sections:
            result.append(ValidationMessage(
                "MissingSection",
                f"缺少标题 '{section['phrase']}'",
                f"请确保标题 '{section['phrase']}' 出现在文档中。",
                ""
            ))
            stats.error_type_count["MissingSection"] += 1
    for title in unnumbered_sections:
        if not title.isupper():
            result.append(ValidationMessage(
                "UppercaseTitle",
                f"标题不是全大写",
                "无编号标题应使用全大写。",
                title
            ))
            stats.error_type_count["UppercaseTitle"] += 1
        if not re.search(r'\\addcontentsline\{toc\}\{section\}\{' + re.escape(title) + r'\}', tex_str):
            result.append(ValidationMessage(
                "MissingInTOC",
                f"标题 '{title}' 未在目录中列出",
                "请确保标题被正确列入目录。",
                title
            ))
            stats.error_type_count["MissingInTOC"] += 1

    if len(result) == 0:
        result.append(ValidationMessage(
            "NoErrors",
            "文档符合规范",
            "没有发现任何格式错误。",
            ""
        ))

    stats.total_errors = len(result)
    return ValidationResponse(result, stats)


@app.route("/api/validate/latex", methods=["POST"])
def validate_tex_upload():
    file = request.files.get("file")
    if not file or not file.filename.endswith(".tex"):
        return jsonify({"error": "请上传 .tex 文件"}), 400

    content = file.read().decode("utf-8")
    content = remove_comments(content)

    response = validate_tex(content)
    return jsonify(json.loads(response.to_json()))


if __name__ == '__main__':
    app.run(debug=True)

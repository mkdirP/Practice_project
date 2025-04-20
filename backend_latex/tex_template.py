import re
from ValidationModels import ValidationModels


def remove_comments(tex_str):
    tex_str = re.sub(r'%.*', '', tex_str)
    tex_str = re.sub(r'\\begin{comment}.*?\\end{comment}', '', tex_str, flags=re.DOTALL)

    return tex_str


def validate_tex(tex_str):
    result = []
    stats = ValidationModels.ValidationStats()
    tex_str = remove_comments(tex_str)

    texts_to_check_section = [
        {"phrase": "СПИСОК СОКРАЩЕНИЙ И УСЛОВНЫХ ОБОЗНАЧЕНИЙ", "label": "Section1"},
        {"phrase": "ТЕРМИНЫ И ОПРЕДЕЛЕНИЯ", "label": "Section2"},
        {"phrase": "ВВЕДЕНИЕ", "label": "Section3"},
        {"phrase": "ЗАКЛЮЧЕНИЕ", "label": "Section4"},
        {"phrase": "СПИСОК ИСПОЛЬЗОВАННЫХ ИСТОЧНИКОВ", "label": "Section5"}
    ]

    # 有没有目录
    if not re.search(r'\\tableofcontents', tex_str, re.MULTILINE):
        result.append(ValidationModels.ValidationMessage(
            "MissingTOC",
            "Не найдено допустимой команды каталога. '\\tableofcontents'",
            "Обязательно включите \\tableofcontents в документ, чтобы создать оглавление.",
            ""
        ))
        stats.error_type_count["MissingTOC"] += 1

    # 检查目录是否放置在单独的一页上
    toc_match = re.search(r'\\tableofcontents', tex_str)
    if toc_match:
        toc_position = toc_match.start()  # 获取 \tableofcontents 命令的位置
        if not re.search(r'(\\end{titlepage}|\\(newpage|clearpage))', tex_str[:toc_position]):
            result.append(ValidationModels.ValidationMessage(
                "TOCNotOnNewPageBefore",
                "Перед каталогом не найдено допустимой команды подкачки",
                "Обязательно используйте \\newpage или \\clearpage перед \\tableofcontents, чтобы поместить оглавление на отдельную страницу.",
                "Перед полям \\tableofcontents"
            ))
            stats.error_type_count["TOCNotOnNewPageBefore"] += 1

        after_toc = tex_str[toc_match.end():].strip()
        if not re.match(r'\\(newpage|clearpage)', after_toc, re.IGNORECASE):
            result.append(ValidationModels.ValidationMessage(
                "TOCNotOnNewPageAfter",
                "После каталога не ставится команда разбиения на страницы",
                "Обязательно используйте \\newpage или \\clearpage после \\tableofcontents, чтобы разместить оглавление на отдельной странице.",
                "После поля \\tableofcontents"
            ))
            stats.error_type_count["TOCNotOnNewPageAfter"] += 1

    # 字体
    if not re.search(r'\\setmainfont\{Times New Roman\}', tex_str) and \
            not re.search(r'\\usepackage(\[.*?\])?\{times\}', tex_str):
        result.append(ValidationModels.ValidationMessage(
            "FontMismatch",
            "Шрифт Times New Roman не установлен",
            "Рекомендуется использовать \\setmainfont{Times New Roman} или \\usepackage{times}",
            "Раздел настроек \\setmainfont или \\usepackage"
        ))
        stats.error_type_count["FontMismatch"] += 1

    # 字号
    match = re.search(r'\\documentclass(\[.*?\])?', tex_str)
    if match:
        options = match.group(1) or ""
        if '14pt' not in options:
            result.append(ValidationModels.ValidationMessage(
                "FontSizeMismatch",
                f"Размер шрифта не установлен на 14pt, а на данный момент установлен на:{options}",
                "Пожалуйста, установите \\documentclass[14pt]{...}",
                "Раздел настроек \\documentclass"
            ))
            stats.error_type_count["FontSizeMismatch"] += 1

    # A4
    if not re.search(r'\\usepackage\[[^\]]*a4paper[^\]]*\]\{geometry\}', tex_str):
        result.append(ValidationModels.ValidationMessage(
            "PageSizeMismatch",
            "Размер страницы не А4",
            "Пожалуйста, установите размер страницы А4 в вашем документе, например, используя \\usepackage[a4paper]{geometry}",
            "Раздел настроек \\usepackage[a4paper]{geometry}"
        ))
        stats.error_type_count["PageSizeMismatch"] += 1

    # 对齐
    if not re.search(r'\\raggedright', tex_str) and \
            not re.search(r'\\begin\{flushleft\}', tex_str):
        result.append(ValidationModels.ValidationMessage(
            "AlignmentError",
            "Документ не может быть выровнен по левому краю.",
            "Рекомендуется добавить \\raggedright или использовать \\begin{flushleft} ... \\end{flushleft}",
            "Раздел настроек \\raggedright или \\begin{flushleft} ... \\end{flushleft}"
        ))
        stats.error_type_count["AlignmentError"] += 1

    center_blocks = re.findall(r'\\begin{center}(.*?)\\end{center}', tex_str, re.DOTALL)
    center_content = "\n".join(center_blocks)
    center_content = center_content.replace("\\", " ").replace("\n", " ").replace("  ", " ")

    # 无编号标题
    unnumbered_sections = re.findall(r'\\section\*\{([^}]+)\}', tex_str)
    for section in texts_to_check_section:
        if section["phrase"] not in unnumbered_sections:
            result.append(ValidationModels.ValidationMessage(
                "MissingSection",
                f"Отсутствует заголовок '{section['phrase']}'",
                f"Убедитесь, что в документе присутствует заголовок '{section['phrase']}'.",
                ""
            ))
            stats.error_type_count["MissingSection"] += 1

    # 大写
    for title in unnumbered_sections:
        if not title.isupper():
            result.append(ValidationModels.ValidationMessage(
                "UppercaseTitle",
                f"Название не полностью написано заглавными буквами",
                "Ненумерованные заголовки следует писать заглавными буквами.",
                title
            ))
            stats.error_type_count["UppercaseTitle"] += 1
        if not re.search(r'\\addcontentsline\{toc\}\{section\}\{' + re.escape(title) + r'\}', tex_str):
            result.append(ValidationModels.ValidationMessage(
                "MissingInTOC",
                f"Заголовок '{title}' не указан в каталоге",
                "Пожалуйста, убедитесь, что название указано правильно в содержании.",
                title
            ))
            stats.error_type_count["MissingInTOC"] += 1

    if len(result) == 0:
        result.append(ValidationModels.ValidationMessage(
            "NoErrors",
            "Документация соответствует правилам",
            "Ошибок форматирования не обнаружено.",
            "ОКЕЙ"
        ))

    stats.total_errors = len(result)
    return ValidationModels.ValidationResponse(result, stats)


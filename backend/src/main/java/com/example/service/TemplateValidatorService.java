package com.example.service;

import com.example.tools.ValidationMessage;
import com.example.tools.ValidationResponse;
import com.example.tools.ValidationStats;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@Service
public class TemplateValidatorService {

    private List<String> getBoldTextList() {
        return Arrays.asList(
                "Обучающийся / Student:",
                "Факультет / Faculty:",
                "Группа / Group:",
                "Направление подготовки / Subject area:",
                "Образовательная программа / Educational program:",
                "Язык реализации ОП / Language of the educational program:",
                "Квалификация / Degree level:",
                "Руководитель ВКР / Thesis supervisor:"
        );
    }

    private List<String> getTitleTextList() {
        return Arrays.asList(
                "Министерство науки и высшего образования Российской Федерации",
                "ФЕДЕРАЛЬНОЕ ГОСУДАРСТВЕННОЕ АВТОНОМНОЕ ОБРАЗОВАТЕЛЬНОЕ УЧРЕЖДЕНИЕ ВЫСШЕГО ОБРАЗОВАНИЯ",
                "НАЦИОНАЛЬНЫЙ ИССЛЕДОВАТЕЛЬСКИЙ УНИВЕРСИТЕТ ИТМО ITMO University",
                "ВЫПУСКНАЯ КВАЛИФИКАЦИОННАЯ РАБОТА",
                "GRADUATION THESIS",
                "Разработка интеллектуальной системы управления складом для организаций малого бизнеса"
        );
    }

    private List<String> getSectionTextList() {
        return Arrays.asList(
                "СПИСОК СОКРАЩЕНИЙ И УСЛОВНЫХ ОБОЗНАЧЕНИЙ",
                "ТЕРМИНЫ И ОПРЕДЕЛЕНИЯ",
                "ВВЕДЕНИЕ",
                "ЗАКЛЮЧЕНИЕ",
                "СПИСОК ИСПОЛЬЗОВАННЫХ ИСТОЧНИКОВ"
        );
    }

    private List<Map<String, Object>> textsToCheckTitle = Arrays.asList(
            new HashMap<String, Object>() {{
                put("words", Arrays.asList("Министерство", "науки", "и", "высшего", "образования", "Российской", "Федерации"));
                put("label", "Font1");
            }},
            new HashMap<String, Object>() {{
                put("words", Arrays.asList("ФЕДЕРАЛЬНОЕ", "ГОСУДАРСТВЕННОЕ", "АВТОНОМНОЕ", "ОБРАЗОВАТЕЛЬНОЕ", "УЧРЕЖДЕНИЕ", "ВЫСШЕГО", "ОБРАЗОВАНИЯ"));
                put("label", "Font2");
            }},
            new HashMap<String, Object>() {{
                put("words", Arrays.asList("НАЦИОНАЛЬНЫЙ", "ИССЛЕДОВАТЕЛЬСКИЙ", "УНИВЕРСИТЕТ", "ИТМО", "ITMO", "University"));
                put("label", "Font3");
            }},
            new HashMap<String, Object>() {{
                put("words", Arrays.asList("ВЫПУСКНАЯ", "КВАЛИФИКАЦИОННАЯ", "РАБОТА"));
                put("label", "Title1");
            }},
            new HashMap<String, Object>() {{
                put("words", Arrays.asList("GRADUATION", "THESIS"));
                put("label", "Title2");
            }},
            new HashMap<String, Object>() {{
                put("words", Arrays.asList("Разработка", "интеллектуальной", "системы", "управления", "складом", "для", "организаций", "малого", "бизнеса"));
                put("label", "Subtitle");
            }}
    );


    List<ValidationMessage> result = new ArrayList<>();
    Map<String, Long> errorCountMap = new HashMap<>();

    public ValidationResponse validateTemplate(MultipartFile file) throws IOException {
        result.clear();
        errorCountMap.clear();
        // 初始化结果存储
        Set<String> checkedParagraphs = new HashSet<>();

        // 用于记录已报告的错误
        Set<String> reportedFontErrors = new HashSet<>();
        Set<String> reportedFontSizeErrors = new HashSet<>();
        Set<String> reportedBoldErrors = new HashSet<>();
        Set<String> reportedAlignmentErrors = new HashSet<>();

        int totalParagraphs = 0;

        try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
            // 检查目录
            // containsTableOfContents(doc);

            // 用于存储所有段落文本
            StringBuilder allParagraphText = new StringBuilder();
            for (XWPFParagraph paragraph : doc.getParagraphs()) {
                String paragraphText = paragraph.getText().trim();

                if (paragraphText.isEmpty() || checkedParagraphs.contains(paragraphText)) continue;

                checkedParagraphs.add(paragraphText);
                totalParagraphs++;

                allParagraphText.append(paragraphText).append("\n");

                checkFont(paragraph, paragraphText, result, errorCountMap, reportedFontErrors);
                checkFontSize(paragraph, paragraphText, result, errorCountMap, reportedFontSizeErrors);
                checkBoldText(getBoldTextList(), paragraph, paragraphText, result, errorCountMap, reportedBoldErrors);
                checkBoldText(getSectionTextList(), paragraph, paragraphText, result, errorCountMap, reportedBoldErrors);


                // 只检查 textsToCheckTitle 中的文本
                for (Map<String, Object> entry : textsToCheckTitle) {
                    List<String> wordsToCheck = (List<String>) entry.get("words");

                    if (containsAllWords(paragraphText, wordsToCheck)) {
                        checkAlignment(paragraph, paragraphText, result, errorCountMap, reportedAlignmentErrors);
                    }
                }
            }
            checkIsExitInDocument(allParagraphText.toString(), getBoldTextList(), reportedBoldErrors);
            checkIsExitInDocument(allParagraphText.toString(), getTitleTextList(), reportedBoldErrors);
            checkIsExitInDocument(allParagraphText.toString(), getSectionTextList(), reportedBoldErrors);

            // 目录验证
            isTOCOnNewPage(doc);

            // 统计结果
            ValidationStats stats = new ValidationStats();
            stats.setErrorTypeCount(errorCountMap);
            stats.setTotalParagraphs(totalParagraphs);
            stats.setTotalErrors(result.size());

            // 如果没有错误
            if (result.isEmpty()) {
                result.add(new ValidationMessage(
                        "NoErrors",
                        "Нет ошибок",
                        "Документ был проверен, ошибок не обнаружено.",
                        "OK"
                ));
            }

            return new ValidationResponse(result, stats);

        }
    }

    // 检查段落是否包含所有指定的单词
    private boolean containsAllWords(String paragraphText, List<String> words) {
        for (String word : words) {
            if (!paragraphText.contains(word)) {
                return false;
            }
        }
        return true;
    }

    // 检查文档是否包含目录 -- 难搞
    private void containsTableOfContents(XWPFDocument doc) {

        boolean foundTOC = false;
        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            if (paragraph.getText().contains("СОДЕРЖАНИЕ")) {
                foundTOC = true;
                break;
            }
        }
        if (!foundTOC) {
            result.add(new ValidationMessage(
                    "MissingTOC",
                    "Не найдено допустимой команды каталога.",
                    "文档中必须包含目录",
                    ""
            ));
            errorCountMap.merge("MissingTOC", 1L, Long::sum);
        }
    }


    // 检查目录是否放在新的一页 -- 难搞
    private void isTOCOnNewPage(XWPFDocument doc) {
        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            if (paragraph.getText().contains("\\tableofcontents")) {
                // 确保目录前后有新页标记
                if (paragraph.getAlignment() != ParagraphAlignment.CENTER) {
                    result.add(new ValidationMessage(
                            "TOCNotOnNewPage",
                            "目录没有放在新的一页上",
                            "请确保目录放在新的一页上。",
                            ""
                    ));
                    errorCountMap.merge("TOCNotOnNewPage", 1L, Long::sum);
                }
            }
        }
    }

    // 检查字体
    private void checkFont(XWPFParagraph paragraph, String paragraphText, List<ValidationMessage> result, Map<String, Long> errorCountMap, Set<String> reportedFontErrors) {
        for (XWPFRun run : paragraph.getRuns()) {
            String fontName = run.getFontFamily();
            if (fontName != null && !fontName.equalsIgnoreCase("Times New Roman")) {
                String errorKey = "FontMismatch:" + fontName + ":" + paragraphText;
                if (!reportedFontErrors.contains(errorKey)) {
                    result.add(new ValidationMessage(
                            "FontMismatch",
                            "Обнаружен неправильный шрифт:" + fontName,
                            "Пожалуйста, установите шрифт 'Times New Roman'",
                            paragraphText
                    ));
                    errorCountMap.merge("FontMismatch", 1L, Long::sum);
                    reportedFontErrors.add(errorKey);
                }
            }
        }
    }

    // 字号
    private void checkFontSize(XWPFParagraph paragraph, String paragraphText, List<ValidationMessage> result, Map<String, Long> errorCountMap, Set<String> reportedFontSizeErrors) {
        for (XWPFRun run : paragraph.getRuns()) {
            int fontSize = run.getFontSize();
            if (fontSize != -1 && (fontSize > 14 || fontSize < 12)) {
                String errorKey = "FontSizeMismatch:" + fontSize + ":" + paragraphText;
                if (!reportedFontSizeErrors.contains(errorKey)) {
                    result.add(new ValidationMessage(
                            "FontSizeMismatch",
                            "Обнаружен неправильный размер шрифта:" + fontSize,
                            "Пожалуйста, установите размер шрифта 12-14.",
                            paragraphText
                    ));
                    errorCountMap.merge("FontSizeMismatch", 1L, Long::sum);
                    reportedFontSizeErrors.add(errorKey);
                }
            }
        }
    }

    // 检查文本是否存在
    private void checkIsExitInDocument(String documentText, List<String> list, Set<String> reportedErrors){
        for (String expectedBoldText : list) {
            if (!documentText.contains(expectedBoldText)) {
                String missingErrorKey = "ContentMismatch:" + expectedBoldText + ":" + "";
                if (!reportedErrors.contains(missingErrorKey)) {
                    result.add(new ValidationMessage(
                            "ContentMismatch",
                            "Отсутствует текст:'" + expectedBoldText + "'",
                            "Убедитесь, что документ содержит текст. '" + expectedBoldText + "'",
                            ""
                    ));
                    errorCountMap.merge("ContentMismatch", 1L, Long::sum);
                    reportedErrors.add(missingErrorKey);
                }
            }
        }
    }
    // 检查文本是否加粗和是否左对齐
    private void checkBoldText( List<String> list, XWPFParagraph paragraph, String paragraphText, List<ValidationMessage> result, Map<String, Long> errorCountMap, Set<String> reportedBoldErrors) {

        for (String expectedBoldText : list) {
            if (paragraphText.contains(expectedBoldText)) {
                boolean isBold = false;
                StringBuilder boldText = new StringBuilder();

                // 遍历段落中的每个 XWPFRun 检查是否有加粗的文本
                for (XWPFRun run : paragraph.getRuns()) {
                    if (run.isBold()) {
                        boldText.append(run.text());  // 拼接所有加粗文本
                        System.out.println("加粗文本: " + boldText);
                        isBold = true;
                    }
                }
                // if (isBold) {
                //     System.out.println("加粗文本: " + boldText.toString());
                // }

                // 如果包含了文本，但未加粗，返回错误
                if (!isBold) {
                    String errorKey = "BoldError:" + expectedBoldText + ":" + paragraphText;
                    if (!reportedBoldErrors.contains(errorKey)) {
                        result.add(new ValidationMessage(
                                "BoldError",
                                "Жирный текст неверен, нежирный текст обнаружен",
                                "Пожалуйста, сделайте текст жирным.",
                                paragraphText
                        ));
                        errorCountMap.merge("BoldError", 1L, Long::sum);
                        reportedBoldErrors.add(errorKey);
                    }
                }
                // 检查段落是否是左对齐
                if (paragraph.getAlignment() != ParagraphAlignment.LEFT) {
                    String alignmentErrorKey = "AlignmentError:" + expectedBoldText + ":" + paragraphText;
                    if (!reportedBoldErrors.contains(alignmentErrorKey)) {
                        result.add(new ValidationMessage(
                                "AlignmentError",
                                "Жирный текст должен быть выровнен по левому краю.",
                                "Пожалуйста, выровняйте жирный текст по левому краю.",
                                paragraphText
                        ));
                        errorCountMap.merge("AlignmentError", 1L, Long::sum);
                        reportedBoldErrors.add(alignmentErrorKey);
                    }
                }

            }
        }
    }




    // 检查段落是否居中对齐
    private void checkAlignment(XWPFParagraph paragraph, String paragraphText, List<ValidationMessage> result, Map<String, Long> errorCountMap, Set<String> reportedAlignmentErrors) {
        if (paragraph.getAlignment() != ParagraphAlignment.CENTER) {
            String errorKey = "AlignmentError:" + paragraphText;
            if (!reportedAlignmentErrors.contains(errorKey)) {
                result.add(new ValidationMessage(
                        "AlignmentError",
                        "Текст не отцентрирован",
                        "Пожалуйста, убедитесь, что текст расположен по центру.",
                        paragraphText
                ));
                errorCountMap.merge("AlignmentError", 1L, Long::sum);
                reportedAlignmentErrors.add(errorKey);
            }
        }
    }
}

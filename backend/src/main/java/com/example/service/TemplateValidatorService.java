package com.example.service;

import com.example.tools.ValidationMessage;
import com.example.tools.ValidationResponse;
import com.example.tools.ValidationStats;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;

import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STOnOff;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@Service
public class TemplateValidatorService {

    private List<String> getSectionTextList() {
        return Arrays.asList(
                "СПИСОК СОКРАЩЕНИЙ И УСЛОВНЫХ ОБОЗНАЧЕНИЙ",
                "ТЕРМИНЫ И ОПРЕДЕЛЕНИЯ",
                "ВВЕДЕНИЕ",
                "ЗАКЛЮЧЕНИЕ",
                "СПИСОК ИСПОЛЬЗОВАННЫХ ИСТОЧНИКОВ",
                "ПРИЛОЖЕНИЕ"
        );
    }


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
                checkBoldText(doc, getSectionTextList(), paragraph, paragraphText, result, errorCountMap, reportedBoldErrors);


            }
            checkIsExitInDocument(allParagraphText.toString(), getSectionTextList(), reportedBoldErrors);

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
    private void checkBoldText(XWPFDocument doc, List<String> list, XWPFParagraph paragraph, String paragraphText, List<ValidationMessage> result, Map<String, Long> errorCountMap, Set<String> reportedBoldErrors) {

        for (String expectedBoldText : list) {
            if (paragraphText.contains(expectedBoldText)) {
                StringBuilder boldText = new StringBuilder();

                boolean isBold = isParagraphBold(paragraph, doc);

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
                ParagraphAlignment effectiveAlign = getEffectiveAlignment(paragraph, doc);
                if (effectiveAlign != ParagraphAlignment.LEFT) {
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


    private boolean isStyleBold(XWPFStyle style) {
        if (style != null && style.getCTStyle().getRPr() != null && style.getCTStyle().getRPr().getB() != null) {
            STOnOff.Enum boldVal = style.getCTStyle().getRPr().getB().getVal();
            return boldVal == STOnOff.TRUE || boldVal == null;
        }
        return false;
    }





    private boolean isParagraphBold(XWPFParagraph paragraph, XWPFDocument document) {
        boolean hasRun = false;
        boolean hasAnyBoldRun = false;

        for (XWPFRun run : paragraph.getRuns()) {
            hasRun = true;
            if (run.getCTR() != null && run.getCTR().getRPr() != null && run.getCTR().getRPr().isSetB()) {
                STOnOff.Enum val = run.getCTR().getRPr().getB().getVal();
                if (val == STOnOff.FALSE) {
                    return false; // 明确取消加粗，优先级最高
                } else {
                    hasAnyBoldRun = true;
                }
            }
        }

        // 所有 run 都没设置，检查样式
        if (!hasRun || !hasAnyBoldRun) {
            String styleId = paragraph.getStyle();
            if (styleId != null) {
                XWPFStyle style = document.getStyles().getStyle(styleId);
                return isStyleBold(style);
            }
            return false;
        }

        return true;
    }



    private ParagraphAlignment getEffectiveAlignment(XWPFParagraph paragraph, XWPFDocument doc) {
        // 如果段落本身有设置，直接返回
        if (paragraph.getAlignment() != ParagraphAlignment.LEFT || paragraph.getCTP().getPPr().isSetJc()) {
            return paragraph.getAlignment();
        }

        // 获取段落的样式 ID
        String styleId = paragraph.getStyle();
        if (styleId != null) {
            XWPFStyles styles = doc.getStyles();
            XWPFStyle style = styles.getStyle(styleId);
            if (style != null && style.getCTStyle().getPPr() != null && style.getCTStyle().getPPr().isSetJc()) {
                STJc.Enum jcVal = style.getCTStyle().getPPr().getJc().getVal();
                return ParagraphAlignment.valueOf(jcVal.toString().toUpperCase());
            }
        }

        // 默认返回左对齐
        return ParagraphAlignment.LEFT;
    }




}
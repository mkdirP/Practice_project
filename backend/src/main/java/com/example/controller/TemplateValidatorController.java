package com.example.controller;

import com.example.tools.ValidationMessage;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/validate")
public class TemplateValidatorController {

    // private static final List<String> REQUIRED_SECTIONS = List.of("Введение", "Заключение", "Список литературы");

    @PostMapping
    public ResponseEntity<List<ValidationMessage>> validateTemplate(@RequestParam("file") MultipartFile file) {
        List<ValidationMessage> result = new ArrayList<>();
        Set<String> checkedParagraphs = new HashSet<>();  // 用于保存已经检查过的段落内容
        Set<String> checkedFontSizeErrors = new HashSet<>();  // 用于保存字号不正确错误的去重

        try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
            // 遍历文档的每个段落
            for (XWPFParagraph paragraph : doc.getParagraphs()) {
                String paragraphText = paragraph.getText().trim();  // 获取段落的文本内容

                // 如果段落内容为空，跳过
                if (paragraphText.isEmpty()) {
                    continue;
                }

                // 检查该段落是否已经被检查过
                if (checkedParagraphs.contains(paragraphText)) {
                    continue;  // 如果已经检查过，跳过该段落
                }

                checkedParagraphs.add(paragraphText);  // 将段落文本添加到已检查集合

                // 验证字体和字号
                for (XWPFRun run : paragraph.getRuns()) {
                    String fontName = run.getFontFamily();
                    int fontSize = run.getFontSize();

                    // 字体校验
                    if (fontName != null && !fontName.equalsIgnoreCase("Times New Roman")) {
                        result.add(new ValidationMessage(
                                "FontMismatch",
                                "字体不正确，检测到：" + fontName,
                                "请将字体设置为 Times New Roman",
                                paragraphText
                        ));
                    }

                    // 字号校验
                    if (fontSize > 0 && fontSize != 14) {
                        // 使用字体+字号的组合来做去重，避免相同的字号错误被多次记录
                        String errorKey = "FontSizeMismatch:" + fontSize + ":" + paragraphText;
                        if (!checkedFontSizeErrors.contains(errorKey)) {
                            result.add(new ValidationMessage(
                                    "FontSizeMismatch",
                                    "字号不正确，检测到：" + fontSize,
                                    "请将字号设置为 14",
                                    paragraphText
                            ));
                            checkedFontSizeErrors.add(errorKey);  // 将这个错误标记为已记录
                        }
                    }
                }

                // 对齐方式校验
                ParagraphAlignment alignment = paragraph.getAlignment();
                if (alignment != ParagraphAlignment.LEFT && alignment != ParagraphAlignment.BOTH) {
                    result.add(new ValidationMessage(
                            "AlignmentError",
                            "段落未左对齐，当前为：" + alignment,
                            "请将段落对齐方式设置为左对齐",
                            paragraphText
                    ));
                }
            }

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok(result);
    }


    // @PostMapping
    // public ResponseEntity<List<ValidationMessage>> validateTemplate(@RequestParam("file") MultipartFile file) {
    //     List<ValidationMessage> result = new ArrayList<>();
    //     Set<String> checkedParagraphs = new HashSet<>();  // 用于保存已经检查过的段落内容
    //     Set<String> checkedFontSizeErrors = new HashSet<>();  // 用于保存字号不正确错误的去重
    //
    //     try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
    //         String fullText = doc.getParagraphs().stream()
    //                 .map(XWPFParagraph::getText)
    //                 .collect(Collectors.joining("\n"));
    //
    //         for (String section : REQUIRED_SECTIONS) {
    //             if (!fullText.contains(section)) {
    //                 result.add(new ValidationMessage(
    //                         "MissingSection",
    //                         "缺少章节：" + section,
    //                         "请添加章节标题：" + section,
    //                         ""
    //                 ));
    //             }
    //         }
    //
    //         // ✅ 2. 检查是否包含目录关键词
    //         if (!(fullText.contains("Оглавление") || fullText.contains("Содержание"))) {
    //             result.add(new ValidationMessage(
    //                     "MissingTOC",
    //                     "文档中未检测到目录（Оглавление / Содержание）",
    //                     "请添加目录页",
    //                     ""
    //             ));
    //         }
    //
    //         // ✅ 3. 遍历每个段落，检查字体、字号、对齐方式等
    //         for (XWPFParagraph paragraph : doc.getParagraphs()) {
    //             String paragraphText = paragraph.getText();  // 当前段落的文本
    //             for (XWPFRun run : paragraph.getRuns()) {
    //                 String fontName = run.getFontFamily();
    //                 int fontSize = run.getFontSize();
    //
    //                 // 字体检查
    //                 if (fontName != null && !fontName.equalsIgnoreCase("Times New Roman")) {
    //                     result.add(new ValidationMessage(
    //                             "FontMismatch",
    //                             "字体不正确，检测到：" + fontName,
    //                             "请将字体设置为 Times New Roman",
    //                             paragraphText
    //                     ));
    //                     break;  // 一旦检测到错误，跳出当前 run 的检查
    //                 }
    //
    //                 // 字号检查
    //                 if (fontSize > 0 && fontSize != 14) {
    //                     result.add(new ValidationMessage(
    //                             "FontSizeMismatch",
    //                             "字号不正确，检测到：" + fontSize,
    //                             "请将字号设置为 14",
    //                             paragraphText
    //                     ));
    //                     break;
    //                 }
    //             }
    //
    //             // 对齐方式检查
    //             ParagraphAlignment alignment = paragraph.getAlignment();
    //             if (alignment != ParagraphAlignment.LEFT && alignment != ParagraphAlignment.BOTH) {
    //                 result.add(new ValidationMessage(
    //                         "AlignmentError",
    //                         "段落未左对齐，当前为：" + alignment,
    //                         "请将段落对齐方式设置为左对齐",
    //                         paragraphText
    //                 ));
    //                 break;  // 发现错误，跳出当前段落检查
    //             }
    //         }
    //
    //     } catch (IOException e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    //     }
    //
    //     return ResponseEntity.ok(result);
    // }

}

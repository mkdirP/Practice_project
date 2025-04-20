package com.example.myjavafx.model;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.example.myjavafx.model.ErrorEntry;

import java.io.FileOutputStream;
import java.util.List;

public class PDFExporter {
    public static void exportErrorsToPDF(List<ErrorEntry> errors) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("./report.pdf"));
            document.open();
            document.add(new Paragraph("Отчет об ошибках ВКР шаблона"));
            document.add(new Paragraph(" ")); // 空行

            PdfPTable table = new PdfPTable(4);
            table.setWidths(new int[]{2, 5, 5, 5});
            table.addCell("Код");
            table.addCell("Сообщение");
            table.addCell("Предложение");
            table.addCell("Фрагмент");

            for (ErrorEntry e : errors) {
                table.addCell(e.getCode());
                table.addCell(e.getMessage());
                table.addCell(e.getSuggestion());
                table.addCell(e.getContent());
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

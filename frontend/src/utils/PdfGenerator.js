import { jsPDF } from "jspdf";
import { autoTable } from "jspdf-autotable";
import "../components/ofont.ru_Times New Roman-normal"; // 确保字体文件已加载

export const generatePDFReport = (validationResult) => {
    const doc = new jsPDF();

    doc.setFont("ofont.ru_Times New Roman", "normal");
    doc.setFontSize(12);
    doc.text("Отчет о проверке шаблона ВКР", 20, 20);

    const headers = ["错误类型", "错误信息", "建议", "错误上下文"];
    const tableData = validationResult.map((item) => [
        item.type,
        item.message,
        item.suggestion,
        item.context,
    ]);

    autoTable(doc, {
        head: [headers],
        body: tableData,
        startY: 30,
        styles: {
            font: "ofont.ru_Times New Roman",
            fontStyle: "normal",
            fontSize: 10,
            cellPadding: 3,
        },
    });

    doc.save("validation_report.pdf");
};

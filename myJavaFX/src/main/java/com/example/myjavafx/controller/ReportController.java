package com.example.myjavafx.controller;

import com.example.myjavafx.model.DataStore;
import com.example.myjavafx.model.PDFExporter;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.example.myjavafx.model.ErrorEntry;

import java.util.ArrayList;
import java.util.List;

public class ReportController {
    @FXML private TableView<ErrorEntry> tableView;
    @FXML private TableColumn<ErrorEntry, String> codeColumn;
    @FXML private TableColumn<ErrorEntry, String> messageColumn;
    @FXML private TableColumn<ErrorEntry, String> suggestionColumn;
    @FXML private TableColumn<ErrorEntry, String> contentColumn;
    @FXML private Label exportStatusLabel;

    @FXML
    public void initialize() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        suggestionColumn.setCellValueFactory(new PropertyValueFactory<>("suggestion"));
        contentColumn.setCellValueFactory(new PropertyValueFactory<>("content"));

        loadErrors();
    }

    private void loadErrors() {
        JsonNode root = DataStore.getInstance().getJsonData();
        if (root == null || !root.has("messages")) return;

        List<ErrorEntry> errors = new ArrayList<>();
        for (JsonNode msg : root.get("messages")) {
            errors.add(new ErrorEntry(
                    msg.get("code").asText(),
                    msg.get("message").asText(),
                    msg.get("suggestion").asText(),
                    msg.get("content").asText()
            ));
        }

        ObservableList<ErrorEntry> data = FXCollections.observableArrayList(errors);
        tableView.setItems(data);
    }

    @FXML
    public void exportPdf() {
        try {
            // 执行 PDF 导出操作
            PDFExporter.exportErrorsToPDF(tableView.getItems());

            // 更新导出状态信息
            exportStatusLabel.setText("报告已成功导出为 PDF 文件！");
            exportStatusLabel.setStyle("-fx-text-fill: #4A4A4A;");
        } catch (Exception e) {
            // 更新导出状态信息
            exportStatusLabel.setText("导出 PDF 时出现错误: " + e.getMessage());
            exportStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }


}


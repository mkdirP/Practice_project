package com.example.myjavafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;


public class MainController {
    @FXML
    private StackPane contentPane;

    // @FXML private BorderPane rootLayout;
    private String lastView = "upload";


    @FXML
    public void initialize() {
        showUpload(); // 默认显示上传界面
    }

    public void showUpload() {
        loadView("UploadView.fxml");
    }

    public void showReport() {
        lastView = "report";
        loadView("ReportView.fxml");
    }

    public void showStats() {
        loadView("StatsView.fxml");
    }

    private void loadView(String fxmlPath) {
        try {

            System.out.println("Resource path: " + getClass().getResource("/com/example/myjavafx/" + fxmlPath));

            // 使用相对路径加载 FXML 文件
            Node view = FXMLLoader.load(getClass().getResource("/com/example/myjavafx/" + fxmlPath));
            contentPane.getChildren().setAll(view);
            // rootLayout.setCenter(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void forceReload() {
        // 重新加载当前模块（用于通知报告和统计视图刷新）
        if (lastView.equals("report")) {
            showReport();
        } else if (lastView.equals("stats")) {
            showStats();
        }
    }
}

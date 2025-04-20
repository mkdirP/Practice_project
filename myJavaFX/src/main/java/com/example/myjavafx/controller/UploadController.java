package com.example.myjavafx.controller;

import com.example.myjavafx.model.DataStore;
import com.example.myjavafx.service.FileUploadService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.util.List;

public class UploadController {

    @FXML private Label fileNameLabel;
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final List<String> ALLOWED_EXT = List.of(".docx", ".tex");

    private File currentFile;

    @FXML
    public void initialize() {
        // 从 DataStore 中恢复文件名
        String savedFileName = DataStore.getInstance().getUploadedFileName();
        if (savedFileName != null && !savedFileName.isEmpty()) {
            fileNameLabel.setText("Выбрать файл: " + savedFileName);
        } else {
            fileNameLabel.setText("Файл не выбран");
        }
        statusLabel.setText("");
    }

    @FXML
    public void handleFileChoose() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл документа");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Файлы документации", "*.docx", "*.tex")
        );
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            currentFile = file;
            fileNameLabel.setText("Выбрать файл: " + file.getName());
            DataStore.getInstance().setUploadedFileName(file.getName());  // 保存文件名到 DataStore
            uploadFile(file);
        }
    }

    @FXML
    public void handleReupload() {
        if (currentFile != null) {
            uploadFile(currentFile);
        } else {
            statusLabel.setText("⚠️ Сначала выберите файл.");
        }
    }

    @FXML
    public void handleDelete() {
        currentFile = null;
        fileNameLabel.setText("Файл не выбран");
        statusLabel.setText("✅ Текущее состояние очищено");

        // 清除 DataStore 中的数据
        DataStore.getInstance().setJsonData(null);
        DataStore.getInstance().setUploadedFileName(null);  // 清除保存的文件名
    }

    private void uploadFile(File file) {
        // 类型和大小合法性检查
        String name = file.getName().toLowerCase();
        boolean allowedType = ALLOWED_EXT.stream().anyMatch(name::endsWith);
        if (!allowedType) {
            statusLabel.setText("❌ Неподдерживаемый тип файла！");
            return;
        }
        if (file.length() > MAX_FILE_SIZE) {
            statusLabel.setText("❌ Файл превышает максимальный размер (10MB)");
            return;
        }

        progressBar.setVisible(true);
        statusLabel.setText("⌛ Загрузка...");
        disableButtons(true);

        Task<Void> uploadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                JsonNode response = FileUploadService.uploadAndParse(file);
                DataStore.getInstance().setJsonData(response);  // 假设 DataStore 用于存储上传后的数据
                return null;
            }

            @Override
            protected void succeeded() {
                progressBar.setVisible(false);
                disableButtons(false);
                statusLabel.setText("✅ Загрузка прошла успешно！");
                notifyOtherModules();
            }

            @Override
            protected void failed() {
                progressBar.setVisible(false);
                disableButtons(false);
                statusLabel.setText("❌ Загрузка не удалась：" + getException().getMessage());
            }
        };

        new Thread(uploadTask).start();
    }

    private void disableButtons(boolean disable) {
        // 可选：禁用所有按钮（需要 @FXML 引用按钮）
    }

    private void notifyOtherModules() {
        // 重新加载当前页面，以触发报告/图表模块的初始化
        // 通知主控制器重新载入
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/myjavafx/MainView.fxml"));
            Node root = loader.load();
            MainController mainController = loader.getController();
            mainController.forceReload(); // 你需要加这个方法
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

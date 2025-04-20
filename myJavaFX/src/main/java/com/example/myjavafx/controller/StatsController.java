package com.example.myjavafx.controller;

import com.example.myjavafx.model.DataStore;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;

import java.util.Iterator;
import java.util.Map;

public class StatsController {

    @FXML private PieChart pieChart;

    @FXML
    public void initialize() {
        JsonNode root = DataStore.getInstance().getJsonData();
        if (root == null || !root.has("stats") || !root.get("stats").has("errorTypeCount")) return;

        JsonNode errorTypeCount = root.get("stats").get("errorTypeCount");

        // 转换 fields 为 List，方便使用 stream 操作
        Iterator<Map.Entry<String, JsonNode>> fields = errorTypeCount.fields();

        // 处理 PieChart
        PieChart.Data[] pieData = new PieChart.Data[(int) errorTypeCount.size()];
        int index = 0;
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            PieChart.Data data = new PieChart.Data(entry.getKey(), entry.getValue().asInt());

            // 在饼图的每个部分添加数量标签
            data.setName(entry.getKey() + " (" + entry.getValue().asInt() + ")");
            pieData[index++] = data;
        }

        pieChart.getData().addAll(pieData);
    }
}

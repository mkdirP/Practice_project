package com.example.myjavafx.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.file.Files;
import java.util.UUID;

public class FileUploadService {

    private static final String DOCX_UPLOAD_URL = "http://localhost:8080/api/validate/docx";
    private static final String LATEX_UPLOAD_URL = "http://localhost:5000/api/validate/latex";

    public static JsonNode uploadAndParse(File file) throws IOException, InterruptedException {
        System.out.println("Загрузить файлы: " + file.getAbsolutePath());

        // 根据文件类型选择合适的 URL
        String uploadUrl = getUploadUrl(file);

        String boundary = UUID.randomUUID().toString(); // 生成随机边界
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(buildMultipartFormData(file, boundary))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Код статуса ответа: " + response.statusCode());
        System.out.println("Содержание ответа: " + response.body());

        return com.fasterxml.jackson.databind.json.JsonMapper.builder()
                .findAndAddModules()
                .build()
                .readTree(response.body());
    }

    private static String getUploadUrl(File file) {
        // 根据文件扩展名返回对应的 URL
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".tex")) {
            return LATEX_UPLOAD_URL;
        }
        return DOCX_UPLOAD_URL;
    }

    private static HttpRequest.BodyPublisher buildMultipartFormData(File file, String boundary) throws IOException {
        var byteStream = new ByteArrayOutputStream();
        var writer = new PrintWriter(new OutputStreamWriter(byteStream, "UTF-8"), true);

        // 写入 multipart headers
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"\r\n");
        writer.append("Content-Type: ").append(Files.probeContentType(file.toPath())).append("\r\n");
        writer.append("\r\n").flush();

        // 写入文件内容
        Files.copy(file.toPath(), byteStream);
        byteStream.flush();

        writer.append("\r\n").flush();

        // 结束 boundary
        writer.append("--").append(boundary).append("--").append("\r\n").flush();

        return HttpRequest.BodyPublishers.ofByteArray(byteStream.toByteArray());
    }
}

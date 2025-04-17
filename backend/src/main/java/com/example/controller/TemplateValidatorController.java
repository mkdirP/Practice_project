package com.example.controller;

import com.example.service.TemplateValidatorService;
import com.example.tools.ValidationMessage;
import com.example.tools.ValidationResponse;
import com.example.tools.ValidationStats;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/validate/docx")
public class TemplateValidatorController {

    private final TemplateValidatorService templateValidatorService;

    public TemplateValidatorController(TemplateValidatorService templateValidatorService) {
        this.templateValidatorService = templateValidatorService;
    }

    @PostMapping
    public ValidationResponse validateDocx(@RequestParam("file") MultipartFile file) throws IOException {
        return templateValidatorService.validateTemplate(file);
    }
}


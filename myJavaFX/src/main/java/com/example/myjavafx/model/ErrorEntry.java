package com.example.myjavafx.model;


public class ErrorEntry {
    private String code;
    private String message;
    private String suggestion;
    private String content;

    public ErrorEntry(String code, String message, String suggestion, String content) {
        this.code = code;
        this.message = message;
        this.suggestion = suggestion;
        this.content = content;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public String getSuggestion() { return suggestion; }
    public String getContent() { return content; }
}


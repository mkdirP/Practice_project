package com.example.tools;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidationMessage {
    public String code;
    public String message;
    public String suggestion;
    public String content;
}

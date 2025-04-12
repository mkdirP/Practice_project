package com.example.tools;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResponse {
    private List<ValidationMessage> messages;
    private ValidationStats stats;

}


package com.example.tools;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationStats {
    private Map<String, Long> errorTypeCount = new HashMap<>(); // 错误类型 -> 出现次数
    private int totalParagraphs; // 总段落数
    private int totalErrors; // 总错误数


}

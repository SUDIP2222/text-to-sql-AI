package com.ai.texttosql.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextToSqlResponse {
    private String generatedSql;
    private List<Map<String, Object>> rows;
}

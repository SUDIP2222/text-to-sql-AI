package com.ai.texttosql.schema;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ColumnSchema {
    private final String name;
    private final String description;
}

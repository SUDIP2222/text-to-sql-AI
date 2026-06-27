package com.ai.texttosql.schema;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TableSchema {

    private final String tableName;
    private final String description;

    private final List<ColumnSchema> columns;

}

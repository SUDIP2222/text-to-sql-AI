package com.ai.texttosql.schema;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Relationship {
    private final String fromTable;
    private final String fromColumn;

    private final String toTable;
    private final String toColumn;

}

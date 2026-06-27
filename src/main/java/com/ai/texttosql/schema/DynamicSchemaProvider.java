package com.ai.texttosql.schema;


import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DynamicSchemaProvider implements SchemaProvider {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<TableSchema> getTables() {
        Map<String, List<ColumnSchema>> columnsByTable = loadColumns();
        Map<String, String> tableDescriptions = loadTableDescriptions();

        String sql = """
            SELECT table_name
            FROM information_schema.tables
            WHERE table_schema = 'public'
              AND table_type = 'BASE TABLE'
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String tableName = rs.getString("table_name");
            return new TableSchema(
                    tableName,
                    tableDescriptions.get(tableName),
                    columnsByTable.getOrDefault(tableName, List.of())
            );
        });
    }

    private Map<String, String> loadTableDescriptions() {
        String sql = """
            SELECT c.relname AS table_name, d.description
            FROM pg_class c
            JOIN pg_namespace n ON n.oid = c.relnamespace
            LEFT JOIN pg_description d
              ON d.objoid = c.oid AND d.objsubid = 0
            WHERE n.nspname = 'public'
              AND c.relkind = 'r'
        """;
        Map<String, String> result = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            result.put(
                    rs.getString("table_name"),
                    rs.getString("description")
            );
        });
        return result;
    }

    private Map<String, List<ColumnSchema>> loadColumns() {
        Map<String, String> columnDescriptions = loadColumnDescriptions();

        String sql = """
            SELECT table_name, column_name
            FROM information_schema.columns
            WHERE table_schema = 'public'
        """;

        Map<String, List<ColumnSchema>> result = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            String tableName = rs.getString("table_name");
            String columnName = rs.getString("column_name");
            String key = tableName + "_" + columnName;
            result
                    .computeIfAbsent(rs.getString("table_name"), t -> new ArrayList<>())
                    .add(new ColumnSchema(rs.getString("column_name"),
                            columnDescriptions.get(key)));
        });
        return result;
    }

    private Map<String, String> loadColumnDescriptions() {
        String sql = """
            SELECT
              c.relname AS table_name,
              a.attname AS column_name,
              d.description
            FROM pg_class c
            JOIN pg_namespace n ON n.oid = c.relnamespace
            JOIN pg_attribute a
              ON a.attrelid = c.oid AND a.attnum > 0
            LEFT JOIN pg_description d
              ON d.objoid = c.oid AND d.objsubid = a.attnum
            WHERE n.nspname = 'public'
        """;
        Map<String, String> result = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            String key = rs.getString("table_name") + "_" + rs.getString("column_name");
            result.put(key, rs.getString("description"));
        });
        return result;
    }

    @Override
    public List<Relationship> getRelationships() {
        String sql = """
            SELECT
              tc.table_name AS source_table,
              kcu.column_name AS source_column,
              ccu.table_name AS target_table,
              ccu.column_name AS target_column
            FROM information_schema.table_constraints tc
            JOIN information_schema.key_column_usage kcu
              ON tc.constraint_name = kcu.constraint_name
            JOIN information_schema.constraint_column_usage ccu
              ON ccu.constraint_name = tc.constraint_name
            WHERE tc.constraint_type = 'FOREIGN KEY'
              AND tc.table_schema = 'public'
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Relationship(
                        rs.getString("source_table"),
                        rs.getString("source_column"),
                        rs.getString("target_table"),
                        rs.getString("target_column")
                )
        );
    }

    @Override
    public List<BusinessRule> getBusinessRules() {
        return List.of(
               /* new BusinessRule(
                        "Unless explicitly requested, consider only employees with status = 'ACTIVE'."
                ),
                new BusinessRule(
                        "A department is considered inactive if it has no ACTIVE employees."
                ),
                new BusinessRule(
                        "Do not assume data that is not present in the schema."
                )*/
        );
    }
}

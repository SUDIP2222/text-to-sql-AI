package com.ai.texttosql.service;



import com.ai.texttosql.schema.ColumnSchema;
import com.ai.texttosql.schema.Relationship;
import com.ai.texttosql.schema.SchemaProvider;
import com.ai.texttosql.schema.TableSchema;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class PromptBuilder {

    private final SchemaProvider schemaProvider;

    public PromptBuilder(@Qualifier("cachedSchemaProvider") SchemaProvider schemaProvider) {
        this.schemaProvider = schemaProvider;
    }

    public String buildPrompt(String question) {

        String tables = schemaProvider.getTables().stream()
                .map(this::renderTable)
                .collect(Collectors.joining("\n\n"));

        String relationships = schemaProvider.getRelationships().stream()
                .map(this::renderRelationship)
                .collect(Collectors.joining("\n"));

        String rules = schemaProvider.getBusinessRules().stream()
                .map(rule -> "- " + rule.getRule())
                .collect(Collectors.joining("\n"));

        return """
               You are an expert PostgreSQL developer. Your task is to generate a highly accurate SQL query based on the provided schema, relationships, and business rules.

               ### DATABASE SCHEMA:
               %s

               ### TABLE RELATIONSHIPS (Foreign Keys):
               %s
               
               ### BUSINESS RULES & CONSTRAINTS:
               %s
               
               ### INSTRUCTIONS:
               1. Use ONLY the tables and columns provided in the schema above.
               2. Always use explicit JOINs based on the defined relationships.
               3. Apply business rules (like filtering deleted records) where applicable.
               4. Return ONLY the raw SQL query. No explanations, no markdown, no code blocks.
               5. Ensure the SQL is valid PostgreSQL syntax.

               ### QUESTION:
               %s

               ### SQL QUERY:
               """.formatted(tables, relationships, rules, question);

    }

    private String renderTable(TableSchema table) {
        String columns = table.getColumns().stream()
                .map(column -> "  - " + column.getName() + " (" + column.getDescription().replace("Data type: ", "") + ")")
                .collect(Collectors.joining("\n"));

        return """
                Table: %s
                Columns:
                %s
                """.formatted(
                table.getTableName(),
                columns
        ).trim();
    }

    private String renderColumn(ColumnSchema column) {
        return "- %s: %s".formatted(column.getName(), column.getDescription());
    }

    private String renderRelationship(Relationship relationship) {
        return "- %s.%s = %s.%s".formatted(
                relationship.getFromTable(),
                relationship.getFromColumn(),
                relationship.getToTable(),
                relationship.getToColumn()
        );
    }

}
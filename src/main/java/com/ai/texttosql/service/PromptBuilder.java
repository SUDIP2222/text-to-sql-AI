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
                .collect(Collectors.joining("\n"));

        String relationships = schemaProvider.getRelationships().stream()
                .map(this::renderRelationship)
                .collect(Collectors.joining("\n"));

        String rules = schemaProvider.getBusinessRules().stream()
                .map(rule -> "- " + rule.getRule())
                .collect(Collectors.joining("\n"));

        return """
               You are an assistant that generates SQL queries for a PostgreSQL database. 

               Database schema:
               %s

               Relationship:
               %s
               
               Business rules:
               %s
               
                Instructions:
                   - Return ONLY the SQL query.
                   - Do NOT include explanations.
                   - Do NOT include markdown.
                   - Do NOT include code fences.
                   - The output must be a single SQL statement.

               Generate a SQL query for the following question:
               %s
               """.formatted(tables, relationships, rules, question);

    }

    private String renderTable(TableSchema table) {
        String columns = table.getColumns().stream()
                .map(this::renderColumn)
                .collect(Collectors.joining("\n"));

        return """
                Table: %s
                Description: %s
                Columns:
                %s
                """.formatted(
                table.getTableName(),
                table.getDescription(),
                columns
        ).trim();
    }

    private String renderColumn(ColumnSchema column) {
        return "- %s: %s".formatted(column.getName(), column.getDescription());
    }

    private String renderRelationship(Relationship relationship) {
        return "- %s.%s references %s.%s".formatted(
                relationship.getFromTable(),
                relationship.getFromColumn(),
                relationship.getToTable(),
                relationship.getToColumn()
        );
    }

}
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
               You are a Senior PostgreSQL Architect and expert Developer. Your goal is to generate the most efficient, accurate, and performant SQL query possible based on the provided schema, relationships, and business rules.
               
               ### DATABASE SCHEMA:
               %s
               
               ### TABLE RELATIONSHIPS (Foreign Keys):
               %s
               
               ### BUSINESS RULES & CONSTRAINTS:
               %s
               
               ### PERFORMANCE & EFFICIENCY GUIDELINES:
               1. **SELECT ONLY necessary columns**: Avoid 'SELECT *'. Be specific about the columns required to answer the question.
               2. **Use JOINs correctly**: Use explicit 'INNER JOIN', 'LEFT JOIN', etc., based on the relationships. Join on Indexed columns (usually IDs).
               3. **Filter early**: Apply 'WHERE' clauses to reduce the dataset as early as possible.
               4. **Use appropriate Aggregate functions**: Use 'COUNT(id)', 'SUM()', 'AVG()' efficiently.
               5. **Handling Deleted Records**: Always check if a table has 'is_deleted', 'deleted', or 'is_revoked' columns and filter accordingly (usually 'is_deleted = false').
               6. **Data Types**: Be mindful of UUIDs and timestamps. Use proper PostgreSQL functions like 'NOW()', 'CURRENT_DATE', or 'INTERVAL' when dealing with time.
               7. **Formatting**: Ensure the SQL is clean and readable.
               
               ### INSTRUCTIONS:
               1. Use ONLY the tables and columns provided in the schema above.
               2. Return ONLY the raw SQL query. No explanations, no markdown, no code blocks.
               3. Ensure the SQL is valid PostgreSQL syntax.
               4. If the question implies a specific order or limit, include 'ORDER BY' and 'LIMIT' clauses.
               
               ### EXAMPLES:
               - Question: "Show me all active users registered in the last 7 days."
                 SQL: SELECT id, username, email FROM users WHERE is_active = true AND created_at >= CURRENT_DATE - INTERVAL '7 days' AND is_deleted = false;
               
               - Question: "Total confirmed appointments for each center."
                 SQL: SELECT c.center_name, COUNT(a.id) as total_confirmed FROM appointments a JOIN ivac_centers c ON a.ivac_center_id = c.id WHERE a.status = 'BOOKING_CONFIRMED' GROUP BY c.center_name;

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
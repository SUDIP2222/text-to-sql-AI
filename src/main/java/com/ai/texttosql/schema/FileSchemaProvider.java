package com.ai.texttosql.schema;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FileSchemaProvider implements SchemaProvider {

    @Value("${app.schema.file-path:Schema.txt}")
    private String schemaFilePath;

    @Value("${app.relationship.file-path:forien-key.txt}")
    private String relationshipFilePath;

    @Value("${app.business-rules.file-path:database_business_rules.txt}")
    private String businessRulesFilePath;

    private List<TableSchema> tables = new ArrayList<>();
    private List<Relationship> relationships = new ArrayList<>();
    private List<BusinessRule> businessRules = new ArrayList<>();

    @PostConstruct
    public void init() {
        loadSchema();
        loadRelationships();
        loadBusinessRules();
    }

    private void loadBusinessRules() {
        try {
            if (!Files.exists(Paths.get(businessRulesFilePath))) {
                log.warn("Business rules file not found: {}. Using default rules.", businessRulesFilePath);
                businessRules = getDefaultRules();
                return;
            }

            List<String> lines = Files.readAllLines(Paths.get(businessRulesFilePath));
            businessRules = lines.stream()
                    .map(String::trim)
                    .filter(line -> line.startsWith("- "))
                    .map(line -> new BusinessRule(line.substring(2).trim()))
                    .collect(Collectors.toList());
            
            if (businessRules.isEmpty()) {
                businessRules = getDefaultRules();
            }
            log.info("Loaded {} business rules from {}", businessRules.size(), businessRulesFilePath);
        } catch (IOException e) {
            log.error("Failed to load business rules from file: {}", businessRulesFilePath, e);
            businessRules = getDefaultRules();
        }
    }

    private List<BusinessRule> getDefaultRules() {
        return List.of(
                new BusinessRule("Always filter out deleted records: use 'is_deleted = false' or 'deleted = false' or 'is_revoked = false' depending on the table columns."),
                new BusinessRule("Use 'is_active = true' or 'active = true' for tables that have an active status flag to get valid records."),
                new BusinessRule("For joining tables, strictly follow the Foreign Key relationships provided."),
                new BusinessRule("Use PostgreSQL specific functions if needed (e.g., NOW(), CURRENT_DATE)."),
                new BusinessRule("Handle UUID types correctly, as most ID columns in this schema are UUIDs.")
        );
    }

    private void loadSchema() {
        try {
            if (schemaFilePath.endsWith("Databse_schema.txt")) {
                loadFromFormattedFile();
                return;
            }
            List<String> lines = Files.readAllLines(Paths.get(schemaFilePath));
            Map<String, List<ColumnSchema>> tableColumnsMap = new LinkedHashMap<>();

            for (String line : lines) {
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String tableName = parts[0].trim();
                    String columnName = parts[1].trim();
                    String dataType = parts.length > 2 ? parts[2].trim() : "unknown";

                    tableColumnsMap
                            .computeIfAbsent(tableName, k -> new ArrayList<>())
                            .add(new ColumnSchema(columnName, "Data type: " + dataType));
                }
            }

            tables = tableColumnsMap.entrySet().stream()
                    .map(entry -> new TableSchema(entry.getKey(), "Table " + entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());

            log.info("Loaded {} tables from {}", tables.size(), schemaFilePath);
        } catch (IOException e) {
            log.error("Failed to load schema from file: {}", schemaFilePath, e);
        }
    }

    private void loadFromFormattedFile() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(schemaFilePath));
        String currentTable = null;
        Map<String, List<ColumnSchema>> tableColumnsMap = new LinkedHashMap<>();

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Table:")) {
                currentTable = line.substring(6).trim();
            } else if (line.startsWith("- ") && currentTable != null) {
                String content = line.substring(2).trim();
                int bracketIndex = content.indexOf('(');
                String columnName = bracketIndex != -1 ? content.substring(0, bracketIndex).trim() : content;
                String dataType = bracketIndex != -1 ? content.substring(bracketIndex + 1, content.length() - 1).trim() : "unknown";

                tableColumnsMap
                        .computeIfAbsent(currentTable, k -> new ArrayList<>())
                        .add(new ColumnSchema(columnName, "Data type: " + dataType));
            }
        }
        tables = tableColumnsMap.entrySet().stream()
                .map(entry -> new TableSchema(entry.getKey(), "Table " + entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        log.info("Loaded {} tables from formatted file {}", tables.size(), schemaFilePath);
    }

    private void loadRelationships() {
        try {
            if (!Files.exists(Paths.get(relationshipFilePath))) {
                log.warn("Relationship file not found: {}", relationshipFilePath);
                return;
            }

            List<String> lines = Files.readAllLines(Paths.get(relationshipFilePath));
            Set<String> uniqueRelationships = new HashSet<>();
            relationships = new ArrayList<>();

            for (String line : lines) {
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String fromTable = parts[0].trim();
                    String fromColumn = parts[1].trim();
                    String toTable = parts[2].trim();
                    String toColumn = parts[3].trim();

                    String key = fromTable + "." + fromColumn + "->" + toTable + "." + toColumn;
                    if (uniqueRelationships.add(key)) {
                        relationships.add(new Relationship(fromTable, fromColumn, toTable, toColumn));
                    }
                }
            }
            log.info("Loaded {} unique relationships from {}", relationships.size(), relationshipFilePath);
        } catch (IOException e) {
            log.error("Failed to load relationships from file: {}", relationshipFilePath, e);
        }
    }

    @Override
    public List<TableSchema> getTables() {
        return tables;
    }

    @Override
    public List<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    public List<BusinessRule> getBusinessRules() {
        return businessRules;
    }
}

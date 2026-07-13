package com.ai.texttosql.schema;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FileSchemaProvider implements SchemaProvider {

    private final ResourceLoader resourceLoader;

    public FileSchemaProvider(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Value("${app.schema.file-path:classpath:schema/Schema.txt}")
    private String schemaFilePath;

    @Value("${app.relationship.file-path:classpath:schema/forien-key.txt}")
    private String relationshipFilePath;

    @Value("${app.business-rules.file-path:classpath:schema/database_business_rules.txt}")
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
            Resource resource = resourceLoader.getResource(businessRulesFilePath);
            if (!resource.exists()) {
                log.warn("Business rules file not found: {}. Using default rules.", businessRulesFilePath);
                businessRules = getDefaultRules();
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                businessRules = reader.lines()
                        .map(String::trim)
                        .filter(line -> line.startsWith("- "))
                        .map(line -> new BusinessRule(line.substring(2).trim()))
                        .collect(Collectors.toList());
            }

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
            Resource resource = resourceLoader.getResource(schemaFilePath);
            if (!resource.exists()) {
                log.error("Schema file not found: {}", schemaFilePath);
                return;
            }

            if (schemaFilePath.endsWith("Databse_schema.txt")) {
                loadFromFormattedFile(resource);
                return;
            }

            Map<String, List<ColumnSchema>> tableColumnsMap = new LinkedHashMap<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
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
            }

            tables = tableColumnsMap.entrySet().stream()
                    .map(entry -> new TableSchema(entry.getKey(), "Table " + entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());

            log.info("Loaded {} tables from {}", tables.size(), schemaFilePath);
        } catch (IOException e) {
            log.error("Failed to load schema from file: {}", schemaFilePath, e);
        }
    }

    private void loadFromFormattedFile(Resource resource) throws IOException {
        String currentTable = null;
        Map<String, List<ColumnSchema>> tableColumnsMap = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
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
        }
        tables = tableColumnsMap.entrySet().stream()
                .map(entry -> new TableSchema(entry.getKey(), "Table " + entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        log.info("Loaded {} tables from formatted file {}", tables.size(), schemaFilePath);
    }

    private void loadRelationships() {
        try {
            Resource resource = resourceLoader.getResource(relationshipFilePath);
            if (!resource.exists()) {
                log.warn("Relationship file not found: {}", relationshipFilePath);
                return;
            }

            Set<String> uniqueRelationships = new HashSet<>();
            relationships = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
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

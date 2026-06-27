package com.ai.texttosql.schema;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StaticSchemaProvider implements SchemaProvider {
    @Override
    public List<TableSchema> getTables() {
        return List.of(
                new TableSchema(
                        "departments",
                        "Organizational departments within NexaCorp",
                        List.of(
                                new ColumnSchema("id", "Unique identifier for the department"),
                                new ColumnSchema("name", "Department name"),
                                new ColumnSchema("location", "Primary physical location of the department")
                        )
                ),
                new TableSchema(
                        "employees",
                        "Employees working at NexaCorp",
                        List.of(
                                new ColumnSchema("id", "Unique identifier for the employee"),
                                new ColumnSchema("department_id", "Department the employee belongs to"),
                                new ColumnSchema("first_name", "Employee first name"),
                                new ColumnSchema("last_name", "Employee last name"),
                                new ColumnSchema("role", "Job title or role of the employee"),
                                new ColumnSchema("salary", "Annual salary of the employee"),
                                new ColumnSchema(
                                        "status",
                                        "Employment status. Valid values include ACTIVE and EXITED"
                                )
                        )
                )

        );
    }

    @Override
    public List<Relationship> getRelationships() {
        return List.of(
                new Relationship(
                        "employees",
                        "department_id",
                        "departments",
                        "id"
                )
        );
    }

    @Override
    public List<BusinessRule> getBusinessRules() {
        return List.of(
                new BusinessRule(
                        "Unless explicitly requested, consider only employees with status = 'ACTIVE'."
                ),
                new BusinessRule(
                        "A department is considered inactive if it has no ACTIVE employees."
                ),
                new BusinessRule(
                        "Do not assume data that is not present in the schema."
                )
        );
    }
}

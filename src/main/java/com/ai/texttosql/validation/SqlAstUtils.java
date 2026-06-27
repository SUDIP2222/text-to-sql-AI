package com.ai.texttosql.validation;

import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.HashSet;
import java.util.Set;

public class SqlAstUtils {

    public static Long extractLimit(Select select) {

        Select body = select.getSelectBody();

        if (body instanceof PlainSelect plainSelect) {
            Limit limit = plainSelect.getLimit();
            if (limit != null && limit.getRowCount() != null) {
                return Long.parseLong(limit.getRowCount().toString());
            }
        }

        return null;
    }

    public static Set<String> extractTables(Select select) {
        TablesNamesFinder finder = new TablesNamesFinder();
        return new HashSet<>(finder.getTableList((Statement) select));
    }

    public static Set<String> extractColumns(Select select) {
        Set<String> columns = new HashSet<>();

        select.getSelectBody().accept(new SelectVisitorAdapter() {
            @Override
            public void visit(PlainSelect plainSelect) {
                for (SelectItem item : plainSelect.getSelectItems()) {
                    item.accept(new SelectItemVisitorAdapter() {
                        @Override
                        public void visit(SelectItem expressionItem) {
                            expressionItem.getExpression().accept(new ExpressionVisitorAdapter() {
                                @Override
                                public void visit(Column column) {
                                    columns.add(column.getColumnName());
                                }
                            });
                        }
                    });
                }
            }
        });

        return columns;
    }
}

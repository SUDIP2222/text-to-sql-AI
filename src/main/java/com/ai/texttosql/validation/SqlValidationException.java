package com.ai.texttosql.validation;

public class SqlValidationException extends RuntimeException {
    public SqlValidationException(String message) {
        super(message);
    }
}

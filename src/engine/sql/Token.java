/*
 * Token v1.0
 *
 * Michael Romashov
 * Dec 22, 2023
 */

package engine.sql;

/**
 * Algebraic data type that represents a token in a SQL query. Has the following variants:
 *  - Punctuation
 *  - Statement
 *  - Operator
 *  - BinaryOperator
 *  - SortOrder
 *  - DataType
 *  - Identifier
 *  - Literal.String
 *  - Literal.Integer
 *  - Literal.Boolean
 */
public sealed interface Token {
    enum PunctuationType {
        LEFT_PAREN,
        RIGHT_PAREN,
        COMMA,
        SEMICOLON,
    }
    record Punctuation(PunctuationType type) implements Token {}

    enum StatementType {
        SHOW,
        TABLES,
        CREATE,
        DROP,
        TABLE,
        INSERT,
        INTO,
        VALUES,
        SELECT,
        DELETE,
        FROM,
        UPDATE,
        SET,
        WHERE,
        ORDER,
        BY,
    }
    record Statement(StatementType type) implements Token {}

    enum OperatorType {
        ASSIGN,
        EQUAL,
        NOT_EQUAL,
        LESS,
        LESS_EQUAL,
        GREATER,
        GREATER_EQUAL,
    }
    record Operator(OperatorType type) implements Token {}

    enum BinaryOperatorType {
        AND,
        OR,
    }

    record BinaryOperator(BinaryOperatorType type) implements Token {}

    enum SortOrderType {
        ASC,
        DESC,
    }
    record SortOrder(SortOrderType type) implements Token {}

    enum DataTypeType {
        VARCHAR,
        INTEGER,
        BOOLEAN,
    }
    record DataType(DataTypeType type) implements Token {}

    record Identifier(String ident) implements Token {}

    sealed interface Literal {
        record String(java.lang.String value) implements Token, Literal {}
        record Integer(int value) implements Token, Literal {}
        record Boolean(boolean value) implements Token, Literal {}
    }
}

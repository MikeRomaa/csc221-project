/*
 * DataType v1.0
 *
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
public sealed interface DataType {
    record VarChar(int length) implements DataType {
        @Override
        public String toString() {
            return String.format("varchar(%d)", length);
        }
    }
    record Integer() implements DataType {
        @Override
        public String toString() {
            return "integer";
        }
    }
    record Boolean() implements DataType {
        @Override
        public String toString() {
            return "boolean";
        }
    }
}

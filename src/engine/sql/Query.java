package engine.sql;

import java.util.List;


public sealed interface Query {
    sealed interface DataType {
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

    record ColumnDefinition(String name, DataType type) {}

    sealed interface Expression {
        record Comparison(Token.Identifier ident, Token.Operator op, Token.Literal value) implements Expression {}
        record Binary(Expression lhs, Token.BinaryOperator op, Expression rhs) implements Expression {}
    }

    record OrderBy(
        Token.Identifier column,
        Token.SortOrder sortOrder
    ) {}

    record ShowTables() implements Query {}

    record CreateTable(
        Token.Identifier tableName,
        List<ColumnDefinition> columns
    ) implements Query {}

    record DropTable(
        Token.Identifier tableName
    ) implements Query {}

    record InsertInto(
        Token.Identifier tableName,
        List<Token.Identifier> columns,
        List<Token.Literal> values
    ) implements Query {}

    record Select(
        Token.Identifier tableName,
        List<Token.Identifier> columns,
        Expression filter,
        OrderBy order
    ) implements Query {}

    record DeleteFrom(
        Token.Identifier tableName,
        Expression filter
    ) implements Query {}

    record UpdateSet(
        Token.Identifier tableName,
        List<Token.Identifier> columns,
        List<Token.Literal> values,
        Expression filter
    ) implements Query {}
}

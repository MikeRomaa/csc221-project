package engine.sql;

import java.util.List;


public sealed interface Query {
    interface DataType {
        record VarChar(int length) implements DataType {}
        record Integer() implements DataType {}
        record Boolean() implements DataType {}
    }

    interface Expression {
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
        List<Token.Identifier> columnNames,
        List<Query.DataType> columnTypes
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
}

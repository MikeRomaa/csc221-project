/*
 * Query v1.0
 *
 * Michael Romashov
 * Dec 22, 2023
 */

package engine.sql;

import java.util.List;

/**
 * Algebraic data type that represents a SQL query. Has the following variants.
 *  - ShowTables
 *  - CreateTable
 *  - DropTable
 *  - InsertInto
 *  - Select
 *  - DeleteFrom
 *  - UpdateSet
 */
public sealed interface Query {
    record ColumnDefinition(String name, DataType type) {}

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

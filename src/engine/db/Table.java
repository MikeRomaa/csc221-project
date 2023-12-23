/*
 * Table v1.0
 *
 * Michael Romashov
 * Dec 22, 2023
 */

package engine.db;

import engine.sql.DataType;
import engine.sql.Expression;
import engine.sql.Query;
import engine.sql.Token;

import java.util.*;
import java.util.stream.Stream;

/**
 * Represents the data stored in a single table and provides an interface to retrieve, insert, and update rows.
 */
public class Table {
    private final String name;
    private final List<Query.ColumnDefinition> columns;
    private final List<List<Value>> data;

    // Column indices are used to directly retrieve a datum from a row given the column name
    private final Map<String, Integer> columnIndices;

    public Table(String name, List<Query.ColumnDefinition> columns, List<List<Value>> data) {
        this.name = name;
        this.columns = columns;
        this.data = data;

        this.columnIndices = new HashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            columnIndices.put(columns.get(i).name(), i);
        }
    }

    public String getName() {
        return name;
    }

    public List<Query.ColumnDefinition> getColumns() {
        return columns;
    }

    public Stream<String> getColumnNames() {
        return this.columns
            .stream()
            .map(Query.ColumnDefinition::name);
    }

    public List<List<Value>> getData() {
        return data;
    }

    /**
     * Determines if a row should be included in the result set given a filter {@link Expression}.
     * @param row current row being filtered
     * @param filter expression to filter on
     * @return true if the row should be included, false otherwise
     */
    private boolean recursiveFilter(List<Value> row, Expression filter) {
        return switch (filter) {
            // If the current expression is a comparison, we've reached our base case and can perform the comparison
            case Expression.Comparison comparison -> {
                Value columnValue = row.get(columnIndices.get(comparison.ident().ident()));
                Token.Literal value = comparison.value();

                yield switch (comparison.op().type()) {
                    // SQL lets you use both `=` and `==` for equality comparison
                    case ASSIGN, EQUAL -> columnValue.equals(value);
                    case NOT_EQUAL     -> !columnValue.equals(value);
                    case LESS          -> columnValue.lessThan(value);
                    case LESS_EQUAL    -> !columnValue.greaterThan(value);
                    case GREATER       -> columnValue.greaterThan(value);
                    case GREATER_EQUAL -> !columnValue.lessThan(value);
                };
            }
            // Otherwise, we must recursively evaluate both sides of the expression and compare their outputs
            case Expression.Binary binary -> switch (binary.op().type()) {
                case AND -> recursiveFilter(row, binary.lhs()) && recursiveFilter(row, binary.rhs());
                case OR  -> recursiveFilter(row, binary.lhs()) || recursiveFilter(row, binary.rhs());
            };
        };
    }

    /**
     * Provides direct access to data while allowing filtering and ordering.
     * @param selectColumns columns to be included in the result set
     * @param filter optional filtering settings
     * @param order optional relative ordering settings
     * @return stream of rows containing the requested columns
     */
    public Stream<List<Value>> filterData(List<Token.Identifier> selectColumns, Expression filter, Query.OrderBy order) {
        Stream<List<Value>> selectedData = this.data.stream();

        // Apply filters, if applicable
        if (filter != null) {
            selectedData = selectedData.filter((row) -> recursiveFilter(row, filter));
        }

        // Apply a relative ordering, if applicable
        if (order != null) {
            int orderIndex = this.columnIndices.get(order.column().ident());

            if (order.sortOrder().type() == Token.SortOrderType.ASC) {
                selectedData = selectedData.sorted((a, b) -> a.get(orderIndex).compareTo(b.get(orderIndex)));
            } else {
                selectedData = selectedData.sorted((a, b) -> b.get(orderIndex).compareTo(a.get(orderIndex)));
            }
        }

        // If we're grabbing all columns, we're done.
        if (selectColumns.contains(new Token.Identifier("*"))) {
            return selectedData;
        }

        // Otherwise, we'll keep track of which indices we need to pull from each row.
        List<Integer> indices = new ArrayList<>();

        for (Token.Identifier column : selectColumns) {
            String columnName = column.ident();
            if (!columnIndices.containsKey(columnName)) {
                throw new IllegalArgumentException(String.format("Column '%s' does not exist in table '%s'.", columnName, this.name));
            }
            indices.add(columnIndices.get(columnName));
        }

        // For each row, pick out the indices specified by the `indices` list.
        return selectedData.map((row) -> indices.stream().map(row::get).toList());
    }

    /**
     * Columns that are not provided a value are assigned to "null".
     *
     * @param insertColumns columns to insert into. attempts to into into all columns if null.
     * @param values values to include (should correspond 1 - 1 with `insertColumns`)
     * @throws IllegalArgumentException if a value does not match the column type
     */
    public void insertRow(List<Token.Identifier> insertColumns, List<Token.Literal> values) throws IllegalArgumentException {
        if (insertColumns != null) {
            // Verify that all inserted columns exist in the table.
            for (Token.Identifier column : insertColumns) {
                if (!columnIndices.containsKey(column.ident())) {
                    throw new IllegalArgumentException(String.format("Column '%s' does not exist in table '%s'.", column.ident(), this.name));
                }
            }
        }

        List<Value> row = new ArrayList<>(this.columns.size());

        int currentValueIndex = 0;
        for (Query.ColumnDefinition column : this.columns) {
            // Check if the current column is next in our input data
            if (insertColumns == null || column.name().equals(insertColumns.get(currentValueIndex).ident())) {
                // Check to make sure the value type matches and wrap our literal with the respective value type.
                Value value = switch (column.type()) {
                    case DataType.VarChar(var maxLength) -> switch (values.get(currentValueIndex)) {
                        case Token.Literal.String(var s) -> {
                            if (s.length() > maxLength) {
                                throw new IllegalArgumentException(String.format("String exceeds maximum field length (%s > %s)", s.length(), maxLength));
                            }
                            yield new Value.VarChar(s);
                        }
                        case Token.Literal.Integer ignored -> throw new IllegalArgumentException(String.format("Attempted to write integer value to varchar column '%s'", column.name()));
                        case Token.Literal.Boolean ignored -> throw new IllegalArgumentException(String.format("Attempted to write boolean value to varchar column '%s'", column.name()));
                    };
                    case DataType.Integer() -> switch (values.get(currentValueIndex)) {
                        case Token.Literal.Integer(var i) -> new Value.Integer(i);
                        case Token.Literal.String ignored -> throw new IllegalArgumentException(String.format("Attempted to write string value to integer column '%s'", column.name()));
                        case Token.Literal.Boolean ignored -> throw new IllegalArgumentException(String.format("Attempted to write boolean value to integer column '%s'", column.name()));
                    };
                    case DataType.Boolean() -> switch (values.get(currentValueIndex)) {
                        case Token.Literal.Boolean(var b) -> new Value.Boolean(b);
                        case Token.Literal.String ignored -> throw new IllegalArgumentException(String.format("Attempted to write string value to boolean column '%s'", column.name()));
                        case Token.Literal.Integer ignored -> throw new IllegalArgumentException(String.format("Attempted to write integer value to boolean column '%s'", column.name()));
                    };
                };

                row.add(value);
                currentValueIndex++;
            } else {
                row.add(null);
            }
        }

        this.data.add(row);
    }
}

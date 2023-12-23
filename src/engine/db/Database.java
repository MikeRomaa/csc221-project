/*
 * Database v1.0
 *
 * Michael Romashov
 * Dec 22, 2023
 */

package engine.db;

import engine.sql.Query;
import engine.sql.Token;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

/**
 * Represents the current state of the database and provides an interface for queries to be made through.
 */
public class Database {
    private List<Table> tables;

    public Database() {
        this.tables = new ArrayList<>(1);
    }

    /**
     * Overwrites tables in current database with tables from another database.
     * @param other database to copy tables from.
     */
    public void copyFrom(Database other) {
        this.tables = other.tables;
    }

    /**
     * Retrieves a table with the given name.
     * @param tableName name of table to retrieve.
     * @throws NoSuchElementException if the table does not exist.
     */
    private Table getTable(String tableName) throws NoSuchElementException {
        return this.tables
            .stream()
            .filter((t) -> t.getName().equals(tableName))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException(String.format("Table with name '%s' does not exist.", tableName)));
    }

    public List<Table> getTables() {
        return tables;
    }

    /**
     * Used to make queries with {@link Query.ShowTables}.
     * @return result set with a single column "tables" with table names as individual rows.
     */
    private TableModel showTables() {
        return new DefaultTableModel(
            this.tables
                .stream()
                .map((table) -> new String[]{ table.getName() })
                .toArray(String[][]::new),
            new String[]{"tables"}
        );
    }

    /**
     * Used to make queries with {@link Query.CreateTable}.
     * @return empty result set.
     */
    private TableModel createTable(Query.CreateTable query) throws RuntimeException {
        String tableName = query.tableName().ident();

        if (this.tables
                .stream()
                .anyMatch((table) -> table.getName().equals(tableName))
        ) {
            throw new RuntimeException(String.format("Table with name '%s' already exists.", tableName));
        }

        this.tables.add(
            new Table(
                query.tableName().ident(),
                query.columns(),
                new ArrayList<>()
            )
        );

        return null;
    }

    /**
     * Used to make queries with {@link Query.DropTable}.
     * @return empty result set.
     */
    private TableModel dropTable(Query.DropTable query) throws NoSuchElementException {
        Table table = getTable(query.tableName().ident());
        this.tables.remove(table);

        return null;
    }

    /**
     * Used to make queries with {@link Query.InsertInto}.
     * @return empty result set.
     */
    private TableModel insertInto(Query.InsertInto query) throws NoSuchElementException {
        Table table = getTable(query.tableName().ident());
        table.insertRow(query.columns(), query.values());

        return null;
    }

    /**
     * Used to make queries with {@link Query.Select}.
     * @return result set with requested columns and (optionally filtered & ordered) data.
     */
    private TableModel select(Query.Select query) throws NoSuchElementException {
        Table table = getTable(query.tableName().ident());

        Stream<List<Value>> filteredData = table.filterData(
            query.columns(),
            query.filter(),
            query.order()
        );

        // Determines which column names should be returned in the result set
        Stream<String> columnNames;
        if (query.columns().contains(new Token.Identifier("*"))) {
            columnNames = table.getColumnNames();
        } else {
            columnNames = query.columns()
                .stream()
                .map(Token.Identifier::ident);
        }

        return new DefaultTableModel(
            filteredData
                // Calls .toString() on each datum and casts row to String array
                .map(row -> row.stream().map(Object::toString).toArray(String[]::new))
                // Collects each row into an array of String arrays
                .toArray(String[][]::new),
            columnNames.toArray(String[]::new)
        );
    }

    /**
     * Used to make queries with {@link Query.DeleteFrom}.
     * @return empty result set.
     */
    private TableModel deleteFrom(Query.DeleteFrom query) throws NoSuchElementException {
        Table table = getTable(query.tableName().ident());
        table.deleteRows(query.filter());

        return null;
    }

    /**
     * Used to make queries with {@link Query.UpdateSet}.
     * @return empty result set.
     */
    private TableModel updateSet(Query.UpdateSet query) throws NoSuchElementException {
        Table table = getTable(query.tableName().ident());
        table.updateRows(
            query.columns(),
            query.values(),
            query.filter()
        );

        return null;
    }

    /**
     * Entrypoint for making queries to the database. Performs pattern matching on the {@link Query} record interface.
     * @param query query to be made
     * @return result set to be displayed on the UI
     * @throws NoSuchElementException if a query had invalid arguments
     */
    public TableModel executeQuery(Query query) throws NoSuchElementException {
        return switch (query) {
            case Query.ShowTables  q -> showTables();
            case Query.CreateTable q -> createTable(q);
            case Query.DropTable   q -> dropTable(q);
            case Query.InsertInto  q -> insertInto(q);
            case Query.Select      q -> select(q);
            case Query.DeleteFrom  q -> deleteFrom(q);
            case Query.UpdateSet   q -> updateSet(q);
        };
    }
}

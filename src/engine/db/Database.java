package engine.db;

import engine.sql.Query;
import engine.sql.Token;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

public class Database {
    private final List<Table> tables;

    public Database() {
        this.tables = new ArrayList<>(1);
    }

    private Table getTable(String tableName) throws NoSuchElementException {
        return this.tables
            .stream()
            .filter((t) -> t.getName().equals(tableName))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException(String.format("Table with name '%s' does not exist.", tableName)));
    }

    private TableModel showTables() {
        return new DefaultTableModel(
            this.tables
                .stream()
                .map((table) -> new String[]{ table.getName() })
                .toArray(String[][]::new),
            new String[]{"tables"}
        );
    }

    private TableModel createTable(Query.CreateTable query) throws RuntimeException {
        String tableName = query.tableName().ident();

        if (
            this.tables
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

    private TableModel dropTable(Query.DropTable query) throws NoSuchElementException {
        Table table = getTable(query.tableName().ident());
        this.tables.remove(table);

        return null;
    }

    private TableModel insertInto(Query.InsertInto query) throws NoSuchElementException {
        Table table = getTable(query.tableName().ident());
        table.insertRow(query.columns(), query.values());

        return null;
    }

    private TableModel select(Query.Select query) throws NoSuchElementException {
        Table table = getTable(query.tableName().ident());

        Stream<List<Value>> filteredData = table.getData(query.columns(), query.filter(), query.order());

        Stream<String> columnNames;
        if (query.columns().contains(new Token.Identifier("*"))) {
            columnNames = table.getColumnNames();
        } else {
            columnNames = query.columns().stream().map(Token.Identifier::ident);
        }

        return new DefaultTableModel(
            filteredData
                .map(row -> row.stream().map(Object::toString).toArray(String[]::new))
                .toArray(String[][]::new),
            columnNames.toArray(String[]::new)
        );
    }

    private TableModel deleteFrom(Query.DeleteFrom query) throws NoSuchElementException {
        Table table = getTable(query.tableName().ident());

        return null;
    }

    private TableModel updateSet(Query.UpdateSet query) throws NoSuchElementException {
        Table table = getTable(query.tableName().ident());

        return null;
    }

    public TableModel executeQuery(Query query) throws RuntimeException {
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

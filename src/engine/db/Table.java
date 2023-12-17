package engine.db;

import java.util.ArrayList;
import java.util.List;

public class Table {
    public record Column(String name, DataType type) {}

    private final List<Column> columns;

    public Table() {
        this.columns = new ArrayList<>(0);
    }

    public void addColumn(String name, DataType type) {
        this.columns.add(new Column(name, type));
    }

    public String[] getColumnNames() {
        return this.columns.stream()
            .map(Table.Column::name)
            .toArray(String[]::new);
    }
}

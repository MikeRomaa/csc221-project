package engine.db;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private final List<Table> tables;

    public Database() {
        this.tables = new ArrayList<>(1);

        Table testTable = new Table();
        testTable.addColumn("ID", new DataType.Integer());
        testTable.addColumn("First Name", new DataType.VarChar((byte) 100));
        testTable.addColumn("Last Name", new DataType.VarChar((byte) 100));
        testTable.addColumn("Phone Number", new DataType.Integer());
        testTable.addColumn("Active", new DataType.Boolean());

        this.tables.add(testTable);
    }

    public TableModel asModel() {
        return new DefaultTableModel(
                new String[][]{},
                this.tables.get(0).getColumnNames()
        );
    }
}

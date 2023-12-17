package gui;

import javax.swing.*;
import javax.swing.table.TableModel;

public class App extends JFrame {
    private JPanel appPanel;
    private JEditorPane queryEditor;
    private JButton executeButton;
    private JTable resultsTable;

    public App() {
        setContentPane(appPanel);
        setTitle("Database");
        setSize(1200, 900);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void setTableModel(TableModel model) {
        resultsTable.setModel(model);
    }
}

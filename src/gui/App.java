package gui;

import engine.db.Database;
import engine.sql.Parser;
import engine.sql.Query;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.List;

public class App extends JFrame {
    private JPanel appPanel;
    private JEditorPane queryEditor;
    private JButton executeButton;
    private JTable resultsTable;
    private JLabel resultsLabel;

    public App(Database database) {
        setContentPane(appPanel);
        setTitle("Database");
        setSize(1200, 900);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);

        executeButton.addActionListener((e) -> {
            List<Query> queries;

            try {
                queries = Parser.parse(queryEditor.getText());
            } catch (IllegalArgumentException err) {
                resultsLabel.setForeground(Color.decode("#ff453a"));
                resultsLabel.setText("Unable to parse query.");
                resultsTable.removeAll();
                return;
            }

            TableModel result = null;
            long startTime = System.nanoTime();

            try {
                for (Query query : queries) {
                    result = database.executeQuery(query);
                }
            } catch (Exception err) {
                resultsLabel.setForeground(Color.decode("#ff453a"));
                resultsLabel.setText(String.format("Unable to execute query: %s", err.getMessage()));
                resultsTable.removeAll();
                return;
            }

            long endTime = System.nanoTime();

            resultsLabel.setForeground(Color.decode("#32d74b"));

            if (result != null) {
                resultsLabel.setText(String.format("Returned %d rows in %.3f ms", result.getRowCount(), (endTime - startTime) / 1e6));
                resultsTable.setModel(result);
            } else {
                resultsLabel.setText(String.format("Executed query in %.3f ms", (endTime - startTime) / 1e6));
                resultsTable.removeAll();
            }
        });
    }
}

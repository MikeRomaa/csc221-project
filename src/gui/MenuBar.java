package gui;

import engine.db.Database;
import engine.io.Serde;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MenuBar extends JMenuBar {
    static final ImageIcon exportIcon = new ImageIcon(MenuBar.class.getClassLoader().getResource("images/download.png"));
    static final ImageIcon importIcon = new ImageIcon(MenuBar.class.getClassLoader().getResource("images/upload.png"));

    private final Database database;
    private final JLabel resultsLabel;

    private final JFileChooser fileChooser;

    public MenuBar(Database database, JLabel resultsLabel) {
        this.database = database;
        this.resultsLabel = resultsLabel;

        JMenu fileMenu = new JMenu("File");
        add(fileMenu);

        this.fileChooser = new JFileChooser();
        this.fileChooser.setAcceptAllFileFilterUsed(false);

        FileNameExtensionFilter filter = new FileNameExtensionFilter("SQL Files", "sql");
        this.fileChooser.setFileFilter(filter);

        fileMenu.add(getExportMenuItem());
        fileMenu.add(getImportMenuItem());
    }

    private JMenuItem getExportMenuItem() {
        JMenuItem exportItem = new JMenuItem("Export to SQL", exportIcon);
        exportItem.addActionListener((e) -> {
            if (this.fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = this.fileChooser.getSelectedFile();

                // Make sure we have the correct extension
                if (!file.getPath().endsWith(".sql")) {
                    file = new File(file.getPath() + ".sql");
                }

                try {
                    BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
                    Serde.serialize(fileWriter, this.database);
                    fileWriter.close();
                } catch (IOException err) {
                    this.resultsLabel.setForeground(Color.decode("#ff453a"));
                    this.resultsLabel.setText("Unable to export database.");
                }

                // Display success status message
                this.resultsLabel.setForeground(Color.decode("#32d74b"));
                this.resultsLabel.setText("Successfully exported database.");
            }
        });
        return exportItem;
    }

    private JMenuItem getImportMenuItem() {
        JMenuItem importItem = new JMenuItem("Import from SQL", importIcon);
        importItem.addActionListener((e) -> {
            if (this.fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = this.fileChooser.getSelectedFile();

                try {
                    String contents = Files.readString(Paths.get(file.getPath()));
                    this.database.copyFrom(Serde.deserialize(contents));
                } catch (IOException err) {
                    this.resultsLabel.setForeground(Color.decode("#ff453a"));
                    this.resultsLabel.setText("Unable to import database.");
                }

                // Display success status message
                this.resultsLabel.setForeground(Color.decode("#32d74b"));
                this.resultsLabel.setText("Successfully imported database.");
            }
        });
        return importItem;
    }
}

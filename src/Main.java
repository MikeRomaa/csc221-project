import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import engine.db.Database;
import gui.App;

public class Main {
    public static void main(String[] args) {
        FlatMacDarkLaf.setup();

        App app = new App();
        Database database = new Database();

        app.setTableModel(database.asModel());
    }
}

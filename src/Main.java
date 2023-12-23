import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import engine.db.Database;
import gui.App;

public class Main {
    public static void main(String[] args) {
        // Configures a better looking "look and feel" for us.
        FlatMacDarkLaf.setup();

        // Initialize fresh database and create JFrame app.
        Database database = new Database();
        new App(database);
    }
}

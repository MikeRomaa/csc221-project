import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import engine.db.Database;
import gui.App;

public class Main {
    public static void main(String[] args) {
        FlatMacDarkLaf.setup();

        Database database = new Database();

        new App(database);
    }
}

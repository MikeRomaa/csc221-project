package engine.io;

import engine.db.Database;
import engine.db.Table;
import engine.db.Value;
import engine.sql.Parser;
import engine.sql.Query;

import java.io.*;
import java.util.List;

public class Serde {
    public static void serialize(BufferedWriter out, Database database) {
        PrintWriter writer = new PrintWriter(out);

        for (Table table : database.getTables()) {
            writer.println(String.format("-- BEGIN TABLE '%s'\n", table.getName()));

            writer.println(String.format("CREATE TABLE %s (", table.getName()));

            for (Query.ColumnDefinition column : table.getColumns()) {
                writer.println(String.format("\t%s %s,", column.name(), column.type()));
            }

            writer.println(");\n");

            for (List<Value> row : table.getData()) {
                writer.print(String.format("INSERT INTO %s VALUES (", table.getName()));

                for (int i = 0; i < row.size(); i++) {
                    if (row.get(i) instanceof Value.VarChar(var value)) {
                        writer.print(String.format("'%s'", value));
                    } else {
                        writer.print(row.get(i));
                    }

                    if (i < row.size() - 1) {
                        writer.print(", ");
                    }
                }

                writer.println(");");
            }

            writer.println(String.format("\n-- END TABLE '%s'\n\n", table.getName()));
        }
    }

    public static Database deserialize(String in) {
        Database database = new Database();
        List<Query> queries = Parser.parse(in);

        try {
            for (Query query : queries) {
                database.executeQuery(query);
            }
        } catch (Exception err) {
            throw new RuntimeException(err);
        }

        return database;
    }
}

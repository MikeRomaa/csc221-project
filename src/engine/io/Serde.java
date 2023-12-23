/*
 * Value v1.0
 *
 * Michael Romashov
 * Dec 22, 2023
 */

package engine.io;

import engine.db.Database;
import engine.db.Table;
import engine.db.Value;
import engine.sql.Parser;
import engine.sql.Query;

import java.io.*;
import java.util.List;

/**
 * Provides methods to serialize and deserialize a database by using SQL commands as a representation of the current state.
 * This is similar to what tools like `mysqldump` and `pg_dump` do.
 */
public class Serde {
    /**
     * Serializes a database to equivalent SQL commands.
     * @param out output writer stream where the script contents will go.
     * @param database database to dump data from.
     */
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

    /**
     * Constructs a new database from SQL commands.
     * @param in contents of a SQL script file
     * @return new database.
     */
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

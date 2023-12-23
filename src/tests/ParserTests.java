package tests;

import engine.sql.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class ParserTests {
    @Test
    public void parseShowTables() {
        Assertions.assertDoesNotThrow(
            () -> Assertions.assertIterableEquals(
                Parser.parse("SHOW TABLES;"),
                List.of(
                    new Query.ShowTables()
                )
            )
        );
    }

    @Test
    public void parseCreateTable() {
        Assertions.assertDoesNotThrow(
            () -> Assertions.assertIterableEquals(
                Parser.parse("""
                    CREATE TABLE test (
                        id int,
                        active bool,
                        first_name varchar(100),
                        last_name varchar(100),
                    );
                """),
                List.of(
                    new Query.CreateTable(
                        new Token.Identifier("test"),
                        Arrays.asList(
                            new Query.ColumnDefinition("id", new DataType.Integer()),
                            new Query.ColumnDefinition("active", new DataType.Boolean()),
                            new Query.ColumnDefinition("first_name", new DataType.VarChar(100)),
                            new Query.ColumnDefinition("last_name", new DataType.VarChar(100))
                        )
                    )
                )
            )
        );
    }

    @Test
    public void parseDropTable() {
        Assertions.assertDoesNotThrow(
            () -> Assertions.assertIterableEquals(
                Parser.parse("DROP TABLE test;"),
                List.of(
                    new Query.DropTable(
                        new Token.Identifier("test")
                    )
                )
            )
        );
    }

    @Test
    public void parseInsertInto() {
        Assertions.assertDoesNotThrow(
            () -> Assertions.assertIterableEquals(
                Parser.parse("""
                    INSERT INTO test
                    VALUES (1, true, 'Michael', 'Romashov');
                """),
                List.of(
                    new Query.InsertInto(
                        new Token.Identifier("test"),
                        null,
                        Arrays.asList(
                            new Token.Literal.Integer(1),
                            new Token.Literal.Boolean(true),
                            new Token.Literal.String("Michael"),
                            new Token.Literal.String("Romashov")
                        )
                    )
                )
            )
        );
    }

    @Test
    public void parseInsertIntoColumns() {
        Assertions.assertDoesNotThrow(
            () -> Assertions.assertIterableEquals(
                Parser.parse("""
                    INSERT INTO test (id, active, first_name, last_name)
                    VALUES (1, true, 'Michael', 'Romashov');
                """),
                List.of(
                    new Query.InsertInto(
                        new Token.Identifier("test"),
                        Arrays.asList(
                            new Token.Identifier("id"),
                            new Token.Identifier("active"),
                            new Token.Identifier("first_name"),
                            new Token.Identifier("last_name")
                        ),
                        Arrays.asList(
                            new Token.Literal.Integer(1),
                            new Token.Literal.Boolean(true),
                            new Token.Literal.String("Michael"),
                            new Token.Literal.String("Romashov")
                        )
                    )
                )
            )
        );
    }

    @Test
    public void parseSelect() {
        Assertions.assertDoesNotThrow(
            () -> Assertions.assertIterableEquals(
                Parser.parse("""
                    SELECT id, active, first_name, last_name
                    FROM test;
                """),
                List.of(
                    new Query.Select(
                        new Token.Identifier("test"),
                        Arrays.asList(
                            new Token.Identifier("id"),
                            new Token.Identifier("active"),
                            new Token.Identifier("first_name"),
                            new Token.Identifier("last_name")
                        ),
                        null,
                        null
                    )
                )
            )
        );
    }

    @Test
    public void parseSelectWhere() {
        Assertions.assertDoesNotThrow(
            () -> Assertions.assertIterableEquals(
                Parser.parse("""
                    SELECT id, active, first_name, last_name
                    FROM test
                    WHERE last_name = 'Romashov';
                """),
                List.of(
                    new Query.Select(
                        new Token.Identifier("test"),
                        Arrays.asList(
                            new Token.Identifier("id"),
                            new Token.Identifier("active"),
                            new Token.Identifier("first_name"),
                            new Token.Identifier("last_name")
                        ),
                        new Expression.Comparison(
                            new Token.Identifier("last_name"),
                            new Token.Operator(Token.OperatorType.ASSIGN),
                            new Token.Literal.String("Romashov")
                        ),
                        null
                    )
                )
            )
        );
    }

    @Test
    public void parseSelectWhereOrderBy() {
        Assertions.assertDoesNotThrow(
            () -> Assertions.assertIterableEquals(
                Parser.parse("""
                    SELECT id, active, first_name, last_name
                    FROM test
                    WHERE last_name = 'Romashov'
                    ORDER BY first_name DESC;
                """),
                List.of(
                    new Query.Select(
                        new Token.Identifier("test"),
                        Arrays.asList(
                            new Token.Identifier("id"),
                            new Token.Identifier("active"),
                            new Token.Identifier("first_name"),
                            new Token.Identifier("last_name")
                        ),
                        new Expression.Comparison(
                            new Token.Identifier("last_name"),
                            new Token.Operator(Token.OperatorType.ASSIGN),
                            new Token.Literal.String("Romashov")
                        ),
                        new Query.OrderBy(
                            new Token.Identifier("first_name"),
                            new Token.SortOrder(Token.SortOrderType.DESC)
                        )
                    )
                )
            )
        );
    }

    @Test
    public void parseDeleteFrom() {
        Assertions.assertDoesNotThrow(
            () -> Assertions.assertIterableEquals(
                Parser.parse("""
                    DELETE FROM test
                    WHERE last_name = 'Romashov';
                """),
                List.of(
                    new Query.DeleteFrom(
                        new Token.Identifier("test"),
                        new Expression.Comparison(
                            new Token.Identifier("last_name"),
                            new Token.Operator(Token.OperatorType.ASSIGN),
                            new Token.Literal.String("Romashov")
                        )
                    )
                )
            )
        );
    }

    @Test
    public void parseUpdateSet() {
        Assertions.assertDoesNotThrow(
            () -> Assertions.assertIterableEquals(
                Parser.parse("""
                    UPDATE test
                    SET active = false
                    WHERE last_name = 'Romashov';
                """),
                List.of(
                    new Query.UpdateSet(
                        new Token.Identifier("test"),
                        List.of(
                            new Token.Identifier("active")
                        ),
                        List.of(
                            new Token.Literal.Boolean(false)
                        ),
                        new Expression.Comparison(
                            new Token.Identifier("last_name"),
                            new Token.Operator(Token.OperatorType.ASSIGN),
                            new Token.Literal.String("Romashov")
                        )
                    )
                )
            )
        );
    }

    @Test
    public void parseMultipleQueries() {
        Assertions.assertDoesNotThrow(
            () -> Assertions.assertIterableEquals(
                Parser.parse("""
                    UPDATE test
                    SET active = false
                    WHERE last_name = 'Romashov';

                    SELECT * FROM test;
                """),
                List.of(
                    new Query.UpdateSet(
                        new Token.Identifier("test"),
                        List.of(
                            new Token.Identifier("active")
                        ),
                        List.of(
                            new Token.Literal.Boolean(false)
                        ),
                        new Expression.Comparison(
                            new Token.Identifier("last_name"),
                            new Token.Operator(Token.OperatorType.ASSIGN),
                            new Token.Literal.String("Romashov")
                        )
                    ),
                    new Query.Select(
                        new Token.Identifier("test"),
                        List.of(
                            new Token.Identifier("*")
                        ),
                        null,
                        null
                    )
                )
            )
        );
    }
}

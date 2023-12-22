package tests;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import engine.sql.Token;
import engine.sql.Tokenizer;

public class TokenizerTests {
    @Test
    public void tokenizeShowTables() {
        Assertions.assertDoesNotThrow(
            () -> Assertions.assertIterableEquals(
                Tokenizer.tokenize("SHOW TABLES;"),
                List.of(
                    new Token.Statement(Token.StatementType.SHOW),
                    new Token.Statement(Token.StatementType.TABLES),
                    new Token.Punctuation(Token.PunctuationType.SEMICOLON)
                )
            )
        );
    }

    @Test
    public void tokenizeCreateTable() {
        Assertions.assertDoesNotThrow(
            () -> Assertions.assertIterableEquals(
                Tokenizer.tokenize("""
                    CREATE TABLE test (
                        id int,
                        active bool,
                        first_name varchar(100),
                        last_name varchar(100),
                    );
                """),
                List.of(
                    new Token.Statement(Token.StatementType.CREATE),
                    new Token.Statement(Token.StatementType.TABLE),
                    new Token.Identifier("test"),

                    new Token.Punctuation(Token.PunctuationType.LEFT_PAREN),

                    new Token.Identifier("id"),
                    new Token.DataType(Token.DataTypeType.INTEGER),
                    new Token.Punctuation(Token.PunctuationType.COMMA),

                    new Token.Identifier("active"),
                    new Token.DataType(Token.DataTypeType.BOOLEAN),
                    new Token.Punctuation(Token.PunctuationType.COMMA),

                    new Token.Identifier("first_name"),
                    new Token.DataType(Token.DataTypeType.VARCHAR),
                    new Token.Punctuation(Token.PunctuationType.LEFT_PAREN),
                    new Token.Literal.Integer(100),
                    new Token.Punctuation(Token.PunctuationType.RIGHT_PAREN),
                    new Token.Punctuation(Token.PunctuationType.COMMA),

                    new Token.Identifier("last_name"),
                    new Token.DataType(Token.DataTypeType.VARCHAR),
                    new Token.Punctuation(Token.PunctuationType.LEFT_PAREN),
                    new Token.Literal.Integer(100),
                    new Token.Punctuation(Token.PunctuationType.RIGHT_PAREN),
                    new Token.Punctuation(Token.PunctuationType.COMMA),

                    new Token.Punctuation(Token.PunctuationType.RIGHT_PAREN),

                    new Token.Punctuation(Token.PunctuationType.SEMICOLON)
                )
            )
        );
    }

    @Test
    public void tokenizeSelect() {
        Assertions.assertDoesNotThrow(
            () -> Assertions.assertIterableEquals(
                Tokenizer.tokenize("""
                    SELECT *
                    FROM test
                    WHERE first_name == 'Michael'
                    AND last_name == 'Romashov';
                """),
                List.of(
                    new Token.Statement(Token.StatementType.SELECT),
                    new Token.Identifier("*"),

                    new Token.Statement(Token.StatementType.FROM),
                    new Token.Identifier("test"),

                    new Token.Statement(Token.StatementType.WHERE),
                    new Token.Identifier("first_name"),
                    new Token.Operator(Token.OperatorType.EQUAL),
                    new Token.Literal.String("Michael"),

                    new Token.BinaryOperator(Token.BinaryOperatorType.AND),
                    new Token.Identifier("last_name"),
                    new Token.Operator(Token.OperatorType.EQUAL),
                    new Token.Literal.String("Romashov"),

                    new Token.Punctuation(Token.PunctuationType.SEMICOLON)
                )
            )
        );
    }

    @Test
    public void invalidToken() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> Tokenizer.tokenize("SELECT 'test;")
        );
    }
}

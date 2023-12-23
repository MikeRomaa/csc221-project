package engine.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Tokenizer {
    private record TokenizeResult(int length, Token token) {}

    /**
     * Represents a closure that takes the input string and current index
     * and attempts to consume part of the string.
     */
    private interface TokenizerFn {
        TokenizeResult tokenize(String input, int current);
    }

    /**
     * Represents a closure that takes a string returned by a regex tokenizer
     * and attempts to convert it into a token.
     */
    private interface TokenResolverFn {
        Token resolve(String input);
    }

    /**
     * Creates a tokenizer closure that consumes characters based on the given RegEx pattern.
     * @param regex pattern to determine whether to consume next character
     * @param resolver closure that takes in a string and returns a `Token` or `null`
     */
    private static TokenizerFn generateRegexTokenizer(Pattern regex, TokenResolverFn resolver) {
        return (input, current) -> {
            int end = current;

            if (!regex.matcher(input.subSequence(end, end + 1)).matches()) {
                return null;
            }

            while (end < input.length() && regex.matcher(input.subSequence(end, end + 1)).matches()) {
                end++;
            }

            Token resolvedToken = resolver.resolve(input.substring(current, end));

            if (resolvedToken == null) {
                return null;
            }

            return new TokenizeResult(end - current, resolvedToken);
        };
    }

    /**
     * Consumes one character and yields nothing if the current character is whitespace.
     */
    private static final TokenizerFn skipWhitespace = (input, current) -> (
        Character.isWhitespace(input.charAt(current))
            ? new TokenizeResult(1, null)
            : null
    );

    private static final TokenizerFn skipComments = (input, current) -> (
        input.startsWith("--", current)
            ? new TokenizeResult(input.indexOf('\n', current) - current, null)
            : null
    );

    /**
     * Consumes one character and yields corresponding `Token.Punctuation` object
     * if the current character is one of four valid punctuation symbols { ( ) , ; }.
     */
    private static final TokenizerFn punctuationTokenizer = (input, current) -> {
        Token token = switch (input.charAt(current)) {
            case '(' -> new Token.Punctuation(Token.PunctuationType.LEFT_PAREN);
            case ')' -> new Token.Punctuation(Token.PunctuationType.RIGHT_PAREN);
            case ',' -> new Token.Punctuation(Token.PunctuationType.COMMA);
            case ';' -> new Token.Punctuation(Token.PunctuationType.SEMICOLON);
            default -> null;
        };

        if (token == null) {
            return null;
        }

        return new TokenizeResult(1, token);
    };

    /**
     * Continually consumes characters that are in the set { * < > ! = } and attempts
     * to match the sequence to an operator.
     */
    private static final TokenizerFn operatorTokenizer = generateRegexTokenizer(
        Pattern.compile("[*<>!=]"),
        (input) -> switch (input) {
            case "=" -> new Token.Operator(Token.OperatorType.ASSIGN);
            case "==" -> new Token.Operator(Token.OperatorType.EQUAL);
            case "!=" -> new Token.Operator(Token.OperatorType.NOT_EQUAL);
            case "<" -> new Token.Operator(Token.OperatorType.LESS);
            case "<=" -> new Token.Operator(Token.OperatorType.LESS_EQUAL);
            case ">" -> new Token.Operator(Token.OperatorType.GREATER);
            case ">=" -> new Token.Operator(Token.OperatorType.GREATER_EQUAL);
            default -> null;
        }
    );

    /**
     * Continually consumes alphabetical characters and attempts to match
     * the sequence to a keyword, which could be a statement, data type, or boolean literal.
     */
    private static final TokenizerFn keywordTokenizer = generateRegexTokenizer(
        Pattern.compile("[A-Za-z]"),
        (input) -> switch (input.toUpperCase()) {
            // Statements
            case "SHOW" -> new Token.Statement(Token.StatementType.SHOW);
            case "TABLES" -> new Token.Statement(Token.StatementType.TABLES);
            case "CREATE" -> new Token.Statement(Token.StatementType.CREATE);
            case "DROP" -> new Token.Statement(Token.StatementType.DROP);
            case "TABLE" -> new Token.Statement(Token.StatementType.TABLE);
            case "INSERT" -> new Token.Statement(Token.StatementType.INSERT);
            case "INTO" -> new Token.Statement(Token.StatementType.INTO);
            case "VALUES" -> new Token.Statement(Token.StatementType.VALUES);
            case "SELECT" -> new Token.Statement(Token.StatementType.SELECT);
            case "DELETE" -> new Token.Statement(Token.StatementType.DELETE);
            case "FROM" -> new Token.Statement(Token.StatementType.FROM);
            case "UPDATE" -> new Token.Statement(Token.StatementType.UPDATE);
            case "SET" -> new Token.Statement(Token.StatementType.SET);
            case "WHERE" -> new Token.Statement(Token.StatementType.WHERE);
            case "ORDER" -> new Token.Statement(Token.StatementType.ORDER);
            case "BY" -> new Token.Statement(Token.StatementType.BY);
            // Binary Operators
            case "AND" -> new Token.BinaryOperator(Token.BinaryOperatorType.AND);
            case "OR" -> new Token.BinaryOperator(Token.BinaryOperatorType.OR);
            // Sort Order
            case "ASC" -> new Token.SortOrder(Token.SortOrderType.ASC);
            case "DESC" -> new Token.SortOrder(Token.SortOrderType.DESC);
            // Data Types
            case "VARCHAR" -> new Token.DataType(Token.DataTypeType.VARCHAR);
            case "INTEGER", "INT" -> new Token.DataType(Token.DataTypeType.INTEGER);
            case "BOOLEAN", "BOOL" -> new Token.DataType(Token.DataTypeType.BOOLEAN);
            // Boolean Literals
            case "TRUE" -> new Token.Literal.Boolean(true);
            case "FALSE" -> new Token.Literal.Boolean(false);
            default -> null;
        }
    );

    /**
     * Consumes all characters that lie in between single quotes and yields a string literal.
     */
    private static final TokenizerFn stringTokenizer = (input, current) -> {
        if (input.charAt(current) == '\'') {
            int end = input.indexOf('\'', current + 1);

            if (end > current) {
                return new TokenizeResult(
                    end - current + 1,
                    new Token.Literal.String(input.substring(current + 1, end))
                );
            }
        }

        return null;
    };

    /**
     * Continually consumes numerical characters and yields an integer literal.
     */
    private static final TokenizerFn integerTokenizer = generateRegexTokenizer(
        Pattern.compile("[0-9]"),
        (input) -> new Token.Literal.Integer(Integer.parseInt(input))
    );

    /**
     * Continually consumes alphabetical characters and underscores and yields an identifier.
     */
    private static final TokenizerFn identifierTokenizer = generateRegexTokenizer(
        Pattern.compile("[_A-Za-z]"),
        Token.Identifier::new
    );

    /**
     * Consumes a single wildcard { * } character and yields an identifier.
     */
    private static final TokenizerFn wildcardTokenizer = (input, current) -> {
        if (input.charAt(current) == '*') {
            return new TokenizeResult(
                1,
                new Token.Identifier("*")
            );
        }

        return null;
    };

    private static final TokenizerFn[] tokenizers = {
        skipWhitespace,
        skipComments,
        punctuationTokenizer,
        operatorTokenizer,
        keywordTokenizer,
        stringTokenizer,
        integerTokenizer,
        wildcardTokenizer,
        identifierTokenizer,
    };

    /**
     * Continually loops through all `tokenizers` until either:
     *   1. the input string is empty, in which case the string has been successfully tokenized; or,
     *   2. all tokenizers have been exhausted, in which case the string is not valid and an `IllegalArgumentException` is thrown.
     * @param input string to tokenize
     * @return list of tokens extracted from the input string
     */
    public static List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int consumedChars = 0;

        outer: while (consumedChars < input.length()) {
            for (TokenizerFn tokenizer : tokenizers) {
                TokenizeResult result = tokenizer.tokenize(input, consumedChars);

                if (result != null) {
                    consumedChars += result.length;
                    if (result.token != null) {
                        tokens.add(result.token);
                    }
                    continue outer;
                }
            }

            throw new IllegalArgumentException("Could not tokenize input string.");
        }

        return tokens;
    }
}

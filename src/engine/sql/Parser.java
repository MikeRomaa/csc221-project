/*
 * Parser v1.0
 *
 * Michael Romashov
 * Dec 22, 2023
 */

package engine.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a method to convert a sequence of tokens into a Query variant.
 */
public class Parser {
    private record ParseResult(int length, Query query) {}

    /**
     * Represents a closure that takes the input token sequence and current index
     * and attempts to consume part of the sequence.
     */
    private interface ParserFn {
        ParseResult parse(List<Token> input, int current);
    }

    /**
     * Consumes one token and yields nothing if the current token is a semicolon.
     */
    private static final ParserFn skipSemicolon = (input, current) -> (
        input.get(current) instanceof Token.Punctuation token
            && token.type() == Token.PunctuationType.SEMICOLON
            ? new ParseResult(1, null)
            : null
    );

    /**
     * Attempts to consume tokens to construct {@link Query.ShowTables}.
     */
    private static final ParserFn showTablesParser = (input, current) -> {
        if (input.get(current) instanceof Token.Statement(var t1) && t1 == Token.StatementType.SHOW) {
            current++;
        } else {
            return null;
        }

        if (input.get(current) instanceof Token.Statement(var t2) && t2 == Token.StatementType.TABLES) {
            current++;
        } else {
            return null;
        }

        return new ParseResult(2, new Query.ShowTables());
    };

    /**
     * Attempts to consume tokens to construct {@link Query.CreateTable}.
     */
    private static final ParserFn createTableParser = (input, current) -> {
        int startIndex = current;

        Token.Identifier tableName;
        List<Query.ColumnDefinition> columns = new ArrayList<>();

        if (input.get(current) instanceof Token.Statement(var t1) && t1 == Token.StatementType.CREATE) {
            current++;
        } else {
            return null;
        }

        if (input.get(current) instanceof Token.Statement(var t2) && t2 == Token.StatementType.TABLE) {
            current++;
        } else {
            return null;
        }

        if (input.get(current) instanceof Token.Identifier) {
            tableName = (Token.Identifier) input.get(current);
            current++;
        } else {
            return null;
        }

        if (input.get(current) instanceof Token.Punctuation(var t3) && t3 == Token.PunctuationType.LEFT_PAREN) {
            current++;
        } else {
            return null;
        }

        while (true) {
            String columnName;
            if (input.get(current) instanceof Token.Identifier(var name)) {
                columnName = name;
                current++;
            } else {
                break;
            }

            if (input.get(current) instanceof Token.DataType(var t8)) {
                current++;

                if (t8 == Token.DataTypeType.VARCHAR) {
                    if (input.get(current) instanceof Token.Punctuation(var t9) && t9 == Token.PunctuationType.LEFT_PAREN) {
                        current++;
                    } else {
                        return null;
                    }

                    if (input.get(current) instanceof Token.Literal.Integer(var length)) {
                        columns.add(
                            new Query.ColumnDefinition(
                                columnName,
                                new DataType.VarChar(length)
                            )
                        );
                        current++;
                    } else {
                        return null;
                    }

                    if (input.get(current) instanceof Token.Punctuation(var t10) && t10 == Token.PunctuationType.RIGHT_PAREN) {
                        current++;
                    } else {
                        return null;
                    }
                } else if (t8 == Token.DataTypeType.BOOLEAN) {
                    columns.add(
                        new Query.ColumnDefinition(
                            columnName,
                            new DataType.Boolean()
                        )
                    );
                } else if (t8 == Token.DataTypeType.INTEGER) {
                    columns.add(
                        new Query.ColumnDefinition(
                            columnName,
                            new DataType.Integer()
                        )
                    );
                } else {
                    return null;
                }
            } else {
                return null;
            }

            if (input.get(current) instanceof Token.Punctuation(var t11) && t11 == Token.PunctuationType.COMMA) {
                current++;
            } else {
                break;
            }
        }

        if (input.get(current) instanceof Token.Punctuation(var t12) && t12 == Token.PunctuationType.RIGHT_PAREN) {
            current++;
        } else {
            return null;
        }

        return new ParseResult(
            current - startIndex,
            new Query.CreateTable(tableName, columns)
        );
    };

    /**
     * Attempts to consume tokens to construct {@link Query.DropTable}.
     */
    private static final ParserFn dropTableParser = (input, current) -> {
        Token.Identifier tableName;

        if (input.get(current) instanceof Token.Statement(var t1) && t1 == Token.StatementType.DROP) {
            current++;
        } else {
            return null;
        }

        if (input.get(current) instanceof Token.Statement(var t2) && t2 == Token.StatementType.TABLE) {
            current++;
        } else {
            return null;
        }

        if (input.get(current) instanceof Token.Identifier) {
            tableName = (Token.Identifier) input.get(current);
        } else {
            return null;
        }

        return new ParseResult(
            3,
            new Query.DropTable(tableName)
        );
    };

    /**
     * Attempts to consume tokens to construct {@link Query.InsertInto}.
     */
    private static final ParserFn insertIntoParser = (input, current) -> {
        int startIndex = current;

        Token.Identifier tableName;
        List<Token.Identifier> columns = null;
        List<Token.Literal> values = new ArrayList<>();

        if (input.get(current) instanceof Token.Statement(var t1) && t1 == Token.StatementType.INSERT) {
            current++;
        } else {
            return null;
        }

        if (input.get(current) instanceof Token.Statement(var t2) && t2 == Token.StatementType.INTO) {
            current++;
        } else {
            return null;
        }

        if (input.get(current) instanceof Token.Identifier) {
            tableName = (Token.Identifier) input.get(current);
            current++;
        } else {
            return null;
        }

        if (input.get(current) instanceof Token.Punctuation(var t3) && t3 == Token.PunctuationType.LEFT_PAREN) {
            current++;

            columns = new ArrayList<>();

            while (true) {
                if (input.get(current) instanceof Token.Identifier) {
                    columns.add((Token.Identifier) input.get(current));
                    current++;
                } else {
                    return null;
                }

                if (input.get(current) instanceof Token.Punctuation(var t5) && t5 == Token.PunctuationType.COMMA) {
                    current++;
                } else {
                    break;
                }
            }

            if (input.get(current) instanceof Token.Punctuation(var t6) && t6 == Token.PunctuationType.RIGHT_PAREN) {
                current++;
            } else {
                return null;
            }
        }

        if (input.get(current) instanceof Token.Statement(var t7) && t7 == Token.StatementType.VALUES) {
            current++;
        } else {
            return null;
        }

        if (input.get(current) instanceof Token.Punctuation(var t8) && t8 == Token.PunctuationType.LEFT_PAREN) {
            current++;
        } else {
            return null;
        }

        while (true) {
            if (input.get(current) instanceof Token.Literal) {
                values.add((Token.Literal) input.get(current));
                current++;
            } else {
                return null;
            }

            if (input.get(current) instanceof Token.Punctuation(var t11) && t11 == Token.PunctuationType.COMMA) {
                current++;
            } else {
                break;
            }
        }

        if (input.get(current) instanceof Token.Punctuation(var t12) && t12 == Token.PunctuationType.RIGHT_PAREN) {
            current++;
        } else {
            return null;
        }

        return new ParseResult(
            current - startIndex,
            new Query.InsertInto(tableName, columns, values)
        );
    };

    /**
     * Attempts to consume tokens to construct {@link Query.Select}.
     */
    private static final ParserFn selectParser = (input, current) -> {
        int startIndex = current;

        Token.Identifier tableName;
        List<Token.Identifier> columns = new ArrayList<>();
        Expression filter = null;
        Query.OrderBy order = null;

        if (input.get(current) instanceof Token.Statement(var t1) && t1 == Token.StatementType.SELECT) {
            current++;
        } else {
            return null;
        }

        while (true) {
            if (input.get(current) instanceof Token.Identifier) {
                columns.add((Token.Identifier) input.get(current));
                current++;
            } else {
                return null;
            }

            if (input.get(current) instanceof Token.Punctuation(var t2) && t2 == Token.PunctuationType.COMMA) {
                current++;
            } else {
                break;
            }
        }

        if (input.get(current) instanceof Token.Statement(var t3) && t3 == Token.StatementType.FROM) {
            current++;
        } else {
            return null;
        }

        if (input.get(current) instanceof Token.Identifier) {
            tableName = (Token.Identifier) input.get(current);
            current++;
        } else {
            return null;
        }

        if (input.get(current) instanceof Token.Statement(var t4) && t4 == Token.StatementType.WHERE) {
            current++;

            while (true) {
                Token.BinaryOperator operator = null;

                if (filter != null) {
                    if (input.get(current) instanceof Token.BinaryOperator) {
                        operator = (Token.BinaryOperator) input.get(current);
                        current++;
                    } else {
                        break;
                    }
                }

                Token.Identifier column;
                Token.Operator comparator;
                Token.Literal value;


                if (input.get(current) instanceof Token.Identifier) {
                    column = (Token.Identifier) input.get(current);
                    current++;
                } else {
                    return null;
                }

                if (input.get(current) instanceof Token.Operator) {
                    comparator = (Token.Operator) input.get(current);
                    current++;
                } else {
                    return null;
                }

                if (input.get(current) instanceof Token.Literal) {
                    value = (Token.Literal) input.get(current);
                    current++;
                } else {
                    return null;
                }

                Expression comparison = new Expression.Comparison(column, comparator, value);

                if (operator == null) {
                    filter = comparison;
                } else {
                    filter = new Expression.Binary(filter, operator, comparison);
                }
            }
        }

        if (input.get(current) instanceof Token.Statement(var t5) && t5 == Token.StatementType.ORDER) {
            current++;

            if (input.get(current) instanceof Token.Statement(var t6) && t6 == Token.StatementType.BY) {
                current++;
            } else {
                return null;
            }

            Token.Identifier columnName;
            Token.SortOrder sortOrder = new Token.SortOrder(Token.SortOrderType.ASC);

            if (input.get(current) instanceof Token.Identifier) {
                columnName = (Token.Identifier) input.get(current);
                current++;
            } else {
                return null;
            }

            if (input.get(current) instanceof Token.SortOrder) {
                sortOrder = (Token.SortOrder) input.get(current);
                current++;
            }

            order = new Query.OrderBy(columnName, sortOrder);
        }

        return new ParseResult(
            current - startIndex,
            new Query.Select(tableName, columns, filter, order)
        );
    };

    /**
     * Attempts to consume tokens to construct {@link Query.DeleteFrom}.
     */
    private static final ParserFn deleteFromParser = (input, current) -> {
        int startIndex = current;

        Token.Identifier tableName;
        Expression filter = null;

        if (input.get(current) instanceof Token.Statement(var t1) && t1 == Token.StatementType.DELETE) {
            current++;
        } else {
            return null;
        }

        if (input.get(current) instanceof Token.Statement(var t3) && t3 == Token.StatementType.FROM) {
            current++;
        } else {
            return null;
        }

        if (input.get(current) instanceof Token.Identifier) {
            tableName = (Token.Identifier) input.get(current);
            current++;
        } else {
            return null;
        }

        if (input.get(current) instanceof Token.Statement(var t4) && t4 == Token.StatementType.WHERE) {
            current++;
        } else {
            return null;
        }

        while (true) {
            Token.BinaryOperator operator = null;

            if (filter != null) {
                if (input.get(current) instanceof Token.BinaryOperator) {
                    operator = (Token.BinaryOperator) input.get(current);
                    current++;
                } else {
                    break;
                }
            }

            Token.Identifier column;
            Token.Operator comparator;
            Token.Literal value;


            if (input.get(current) instanceof Token.Identifier) {
                column = (Token.Identifier) input.get(current);
                current++;
            } else {
                return null;
            }

            if (input.get(current) instanceof Token.Operator) {
                comparator = (Token.Operator) input.get(current);
                current++;
            } else {
                return null;
            }

            if (input.get(current) instanceof Token.Literal) {
                value = (Token.Literal) input.get(current);
                current++;
            } else {
                return null;
            }

            Expression comparison = new Expression.Comparison(column, comparator, value);

            if (operator == null) {
                filter = comparison;
            } else {
                filter = new Expression.Binary(filter, operator, comparison);
            }
        }

        return new ParseResult(
            current - startIndex,
            new Query.DeleteFrom(tableName, filter)
        );
    };

    /**
     * Attempts to consume tokens to construct {@link Query.UpdateSet}.
     */
    private static final ParserFn updateSetParser = (input, current) -> {
        int startIndex = current;

        Token.Identifier tableName;
        List<Token.Identifier> columns = new ArrayList<>();
        List<Token.Literal> values = new ArrayList<>();
        Expression filter = null;

        if (input.get(current) instanceof Token.Statement(var t1) && t1 == Token.StatementType.UPDATE) {
            current++;
        } else {
            return null;
        }

        if (input.get(current) instanceof Token.Identifier) {
            tableName = (Token.Identifier) input.get(current);
            current++;
        } else {
            return null;
        }

        if (input.get(current) instanceof Token.Statement(var t2) && t2 == Token.StatementType.SET) {
            current++;
        } else {
            return null;
        }

        while (true) {
            if (input.get(current) instanceof Token.Identifier) {
                columns.add((Token.Identifier) input.get(current));
                current++;
            } else {
                return null;
            }

            if (input.get(current) instanceof Token.Operator(var t3) && t3 == Token.OperatorType.ASSIGN) {
                current++;
            } else {
                return null;
            }

            if (input.get(current) instanceof Token.Literal) {
                values.add((Token.Literal) input.get(current));
                current++;
            } else {
                return null;
            }

            if (input.get(current) instanceof Token.Punctuation(var t4) && t4 == Token.PunctuationType.COMMA) {
                current++;
            } else {
                break;
            }
        }

        if (input.get(current) instanceof Token.Statement(var t5) && t5 == Token.StatementType.WHERE) {
            current++;

            while (true) {
                Token.BinaryOperator operator = null;

                if (filter != null) {
                    if (input.get(current) instanceof Token.BinaryOperator) {
                        operator = (Token.BinaryOperator) input.get(current);
                        current++;
                    } else {
                        break;
                    }
                }

                Token.Identifier column;
                Token.Operator comparator;
                Token.Literal value;


                if (input.get(current) instanceof Token.Identifier) {
                    column = (Token.Identifier) input.get(current);
                    current++;
                } else {
                    return null;
                }

                if (input.get(current) instanceof Token.Operator) {
                    comparator = (Token.Operator) input.get(current);
                    current++;
                } else {
                    return null;
                }

                if (input.get(current) instanceof Token.Literal) {
                    value = (Token.Literal) input.get(current);
                    current++;
                } else {
                    return null;
                }

                Expression comparison = new Expression.Comparison(column, comparator, value);

                if (operator == null) {
                    filter = comparison;
                } else {
                    filter = new Expression.Binary(filter, operator, comparison);
                }
            }
        }

        return new ParseResult(
            current - startIndex,
            new Query.UpdateSet(tableName, columns, values, filter)
        );
    };

    private static final ParserFn[] parsers = {
        skipSemicolon,
        showTablesParser,
        createTableParser,
        dropTableParser,
        insertIntoParser,
        selectParser,
        deleteFromParser,
        updateSetParser,
    };

    /**
     * Continually loops through all `parsers` until either:
     *   1. the input token stream is empty, in which case the string has been successfully parsed; or,
     *   2. all parsers have been exhausted, in which case the string is not valid and an `IllegalArgumentException` is thrown.
     * @param input string to tokenize
     * @return list of tokens extracted from the input string
     */
    public static List<Query> parse(String input) {
        List<Token> tokens = Tokenizer.tokenize(input);

        List<Query> queries = new ArrayList<>();
        int consumedChars = 0;

        outer: while (consumedChars < tokens.size()) {
            for (ParserFn parser : parsers) {
                ParseResult result;

                try {
                    result = parser.parse(tokens, consumedChars);
                } catch (IndexOutOfBoundsException err) {
                    break;
                }

                if (result != null) {
                    consumedChars += result.length;
                    if (result.query != null) {
                        queries.add(result.query);
                    }
                    continue outer;
                }
            }

            // All parsers have been tried and none of them consumed any tokens.
            throw new IllegalArgumentException("Could not parse input string.");
        }

        return queries;
    }
}

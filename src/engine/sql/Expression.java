/*
 * Expression v1.0
 *
 * Michael Romashov
 * Dec 22, 2023
 */

package engine.sql;

/**
 * Algebraic data type that represents a SQL filter expression. Has the following variants:
 *  - Comparison     Compares an identifier to a literal with a comparison operator.
 *  - Binary         Combines two expressions with a binary logical operator.
 */
public sealed interface Expression {
    record Comparison(Token.Identifier ident, Token.Operator op, Token.Literal value) implements Expression {}
    record Binary(Expression lhs, Token.BinaryOperator op, Expression rhs) implements Expression {}
}

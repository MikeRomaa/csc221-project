/*
 * Value v1.0
 *
 * Michael Romashov
 * Dec 22, 2023
 */

package engine.db;

import engine.sql.Token;

/**
 * Algebraic data type that represents a value stored in the database. Also provides methods for comparison.
 * Has the following variants:
 *  - VarChar
 *  - Integer
 *  - Boolean
 */
public sealed interface Value {
    /**
     * Returns true if the value is the same as the literal, false otherwise.
     * @throws IllegalArgumentException if the types of the value and literal do not match.
     */
    boolean equals(Token.Literal rhs) throws IllegalArgumentException;

    /**
     * Returns true if the value is smaller than the literal, false otherwise.
     * @throws IllegalArgumentException if the types of the value and literal do not match.
     */
    boolean lessThan(Token.Literal rhs) throws IllegalArgumentException;

    /**
     * Returns true if the value is greater than the literal, false otherwise.
     * @throws IllegalArgumentException if the types of the value and literal do not match.
     */
    boolean greaterThan(Token.Literal rhs) throws IllegalArgumentException;

    /**
     * Returns a relative order with another value.
     * - if 0, values are equal
     * - if < 0, other value is greater
     * - if > 0, other value is smaller
     * @throws IllegalArgumentException if the types of the value and literal do not match.
     */
    int compareTo(Value rhs) throws IllegalArgumentException;

    record VarChar(String value) implements Value {
        @Override
        public String toString() {
            return value;
        }

        String getValue(Token.Literal rhs) throws IllegalArgumentException {
            return switch (rhs) {
                case Token.Literal.String(var v) -> v;
                case Token.Literal.Integer ignored -> throw new IllegalArgumentException("Attempted to compare varchar column to integer");
                case Token.Literal.Boolean ignored -> throw new IllegalArgumentException("Attempted to compare varchar column to boolean");
            };
        }

        public boolean equals(Token.Literal rhs) throws IllegalArgumentException {
            return value.equals(getValue(rhs));
        }

        public boolean lessThan(Token.Literal rhs) throws IllegalArgumentException {
            return value.compareTo(getValue(rhs)) < 0;
        }

        public boolean greaterThan(Token.Literal rhs) throws IllegalArgumentException {
            return value.compareTo(getValue(rhs)) > 0;
        }

        public int compareTo(Value rhs) {
            return switch (rhs) {
                case Value.VarChar(var v) -> value.compareTo(v);
                case Value.Integer ignored -> throw new IllegalArgumentException("Attempted to compare varchar column to integer column");
                case Value.Boolean ignored -> throw new IllegalArgumentException("Attempted to compare varchar column to boolean column");
            };
        }
    }

    record Integer(int value) implements Value {
        @Override
        public String toString() {
            return String.valueOf(value);
        }

        int getValue(Token.Literal rhs) throws IllegalArgumentException {
            return switch (rhs) {
                case Token.Literal.Integer(var v) -> v;
                case Token.Literal.String ignored -> throw new IllegalArgumentException("Attempted to compare integer column to string");
                case Token.Literal.Boolean ignored -> throw new IllegalArgumentException("Attempted to compare integer column to boolean");
            };
        }

        public boolean equals(Token.Literal rhs) throws IllegalArgumentException {
            return value == getValue(rhs);
        }

        public boolean lessThan(Token.Literal rhs) throws IllegalArgumentException {
            return value < getValue(rhs);
        }

        public boolean greaterThan(Token.Literal rhs) throws IllegalArgumentException {
            return value > getValue(rhs);
        }

        public int compareTo(Value rhs) {
            return switch (rhs) {
                case Value.Integer(var v) -> value - v;
                case Value.VarChar ignored -> throw new IllegalArgumentException("Attempted to compare integer column to varchar column");
                case Value.Boolean ignored -> throw new IllegalArgumentException("Attempted to compare integer column to boolean column");
            };
        }
    }

    record Boolean(boolean value) implements Value {
        @Override
        public String toString() {
            return String.valueOf(value);
        }

        boolean getValue(Token.Literal rhs) throws IllegalArgumentException {
            return switch (rhs) {
                case Token.Literal.Boolean(var v) -> v;
                case Token.Literal.String ignored -> throw new IllegalArgumentException("Attempted to compare boolean column to string");
                case Token.Literal.Integer ignored -> throw new IllegalArgumentException("Attempted to compare boolean column to integer");
            };
        }

        public boolean equals(Token.Literal rhs) throws IllegalArgumentException {
            return value == getValue(rhs);
        }

        public boolean lessThan(Token.Literal rhs) throws IllegalArgumentException {
            return (value ? 1 : 0) < (getValue(rhs) ? 1 : 0);
        }

        public boolean greaterThan(Token.Literal rhs) throws IllegalArgumentException {
            return (value ? 1 : 0) > (getValue(rhs) ? 1 : 0);
        }

        public int compareTo(Value rhs) {
            return switch (rhs) {
                case Value.Boolean(var v) -> (value ? 1 : 0) - (v ? 1 : 0);
                case Value.VarChar ignored -> throw new IllegalArgumentException("Attempted to compare boolean column to varchar column");
                case Value.Integer ignored -> throw new IllegalArgumentException("Attempted to compare boolean column to integer column");
            };
        }
    }
}

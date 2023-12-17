package engine.db;

public sealed interface DataType {
    record VarChar(byte size) implements DataType {}
    record Integer() implements DataType {}
    record Boolean() implements DataType {}
}

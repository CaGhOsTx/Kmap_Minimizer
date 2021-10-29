package booleanalgebra;

public enum TermType {
    MIN_TERMS(1, 0, "."),
    MAX_TERMS(0, 1,"+");

    final int value, complement;
    final String operator;

    TermType(int value, int complement, String operator) {
        this.value = value;
        this.complement = complement;
        this.operator = operator;
    }

    boolean isMIN_TERMS() {
        return this.equals(MIN_TERMS);
    }
}

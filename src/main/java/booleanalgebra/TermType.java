package booleanalgebra;

public enum TermType {
    MIN_TERM('1', '0', '.'),
    MAX_TERM('0', '1','+'),
    DONT_CARE('x', '0', '.');


    final char VALUE, COMPLEMENT, OPERATOR;

    TermType(char VALUE, char COMPLEMENT, char OPERATOR) {
        this.VALUE = VALUE;
        this.COMPLEMENT = COMPLEMENT;
        this.OPERATOR = OPERATOR;
    }

    boolean isMIN_TERMS() {
        return this == MIN_TERM;
    }
}

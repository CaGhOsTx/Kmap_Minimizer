package booleanalgebra;

public enum TermType {
    MIN_TERM('1', '0', '.'),
    MAX_TERM('0', '1','+'),
    DONT_CARE('x', 'x', '.');


    final char value, complement, operator;

    TermType(char value, char complement, char operator) {
        this.value = value;
        this.complement = complement;
        this.operator = operator;
    }

    boolean isMIN_TERMS() {
        return this.equals(MIN_TERM);
    }
}

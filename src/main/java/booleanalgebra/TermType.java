package booleanalgebra;

public enum TermType {
    MIN_TERM("1", "0", "."),
    MAX_TERM("0", "1","+"),
    DONT_CARE("x", "0", "?");


    final String VALUE, COMPLEMENT, OPERATOR;

    TermType(String VALUE, String COMPLEMENT, String OPERATOR) {
        this.VALUE = VALUE;
        this.COMPLEMENT = COMPLEMENT;
        this.OPERATOR = OPERATOR;
    }

    boolean isMIN_TERM() {
        return this == MIN_TERM;
    }
}

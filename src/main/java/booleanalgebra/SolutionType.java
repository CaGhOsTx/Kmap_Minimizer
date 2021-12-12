package booleanalgebra;

import static booleanalgebra.TermType.MAX_TERM;
import static booleanalgebra.TermType.MIN_TERM;

public enum SolutionType {
    SUM_OF_PRODUCTS("1", ".", " + "),
    PRODUCT_OF_SUMS("0", "+", ".");

    final String VALUE;
    final String INNER_DELIMITER, OUTER_DELIMITER;

    SolutionType(String VALUE, String INNER_DELIMITER, String OUTER_DELIMITER) {
        this.VALUE = VALUE;
        this.INNER_DELIMITER = INNER_DELIMITER;
        this.OUTER_DELIMITER = OUTER_DELIMITER;
    }

    String group(String term) {
        if(this == PRODUCT_OF_SUMS && term.length() > 2)
            return "(" + term + ")";
        return term;
    }

    TermType getDerivedTermType(){
        return this == SUM_OF_PRODUCTS ? MIN_TERM : MAX_TERM;
    }

    String getInnerRegex() {
        return "\\" + INNER_DELIMITER;
    }
}

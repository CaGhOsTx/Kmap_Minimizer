package booleanalgebra;

public enum SolutionType {
    SUM_OF_PRODUCTS('1', ".", " + "),
    PRODUCT_OF_SUMS('0', "+", ".");

    final char VALUE;
    final String INNER_DELIMITER, OUTER_DELIMITER;

    SolutionType(char VALUE, String INNER_DELIMITER, String OUTER_DELIMITER) {
        this.VALUE = VALUE;
        this.INNER_DELIMITER = INNER_DELIMITER;
        this.OUTER_DELIMITER = OUTER_DELIMITER;
    }

    String group(String term) {
        if(this == PRODUCT_OF_SUMS)
            return "(" + term + ")";
        return term;
    }

    String getInnerRegex() {
        return "\\" + INNER_DELIMITER;
    }
}

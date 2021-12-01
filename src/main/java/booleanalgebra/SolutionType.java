package booleanalgebra;

public enum SolutionType {
    SUM_OF_PRODUCTS('1'),
    PRODUCT_OF_SUMS('0');

    private char val;

    SolutionType(char val) {
        this.val = val;
    }

    public char getVal() {
        return val;
    }
}

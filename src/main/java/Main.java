import booleanalgebra.KmapBuilder;

import static booleanalgebra.SolutionType.PRODUCT_OF_SUMS;
import static booleanalgebra.SolutionType.SUM_OF_PRODUCTS;
import static booleanalgebra.TermType.MIN_TERM;


public class Main {
    public static void main(String[] args) {
        var halfAdder = KmapBuilder.withVariables("A", "B", "Sum", "Cout")
                .andGrayCodeTerms(MIN_TERM, "0000", "0110", "1101", "1010")
                .build();
        var adder = KmapBuilder.withVariables("A", "B", "Cin", "Sum", "Cout")
                .andGrayCodeTerms(MIN_TERM, "00000", "00110", "01101", "01010", "11001", "11111", "10101", "10010")
                .build();
        System.out.println(halfAdder.toString());
        System.out.println(halfAdder.minimize(SUM_OF_PRODUCTS));
        System.out.println(halfAdder.solveAll(SUM_OF_PRODUCTS));
        System.out.println(halfAdder.minimize(PRODUCT_OF_SUMS));
        System.out.println(halfAdder.solveAll(PRODUCT_OF_SUMS));
    }
}

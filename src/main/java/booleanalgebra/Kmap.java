package booleanalgebra;

import java.util.*;

import static booleanalgebra.KmapBuilder.complement;
import static booleanalgebra.Options.VALUES;
import static booleanalgebra.SolutionType.PRODUCT_OF_SUMS;
import static booleanalgebra.SolutionType.SUM_OF_PRODUCTS;
import static java.util.stream.Collectors.*;

public final class Kmap {

    final List<String> ROW_VARIABLES, COLUMN_VARIABLES, ROW_GRAY_CODE, COLUMN_GRAY_CODE;
    final Node[][] MAP;
    Set<Node> minTerms, maxTerms;
    private KmapSolver minSolver, maxSolver;

    Kmap (String[] ROW_VARIABLES, String[] COLUMN_VARIABLES, String[] ROW_GRAY_CODE, String[] COLUMN_GRAY_CODE, Node[][] MAP, Set<Node> minTerms, Set<Node> maxTerms) {
        this.ROW_VARIABLES = new ArrayList<>(Arrays.asList(ROW_VARIABLES));
        this.COLUMN_VARIABLES = new ArrayList<>(Arrays.asList(COLUMN_VARIABLES));
        this.ROW_GRAY_CODE = new ArrayList<>(Arrays.asList(ROW_GRAY_CODE));
        this.COLUMN_GRAY_CODE = new ArrayList<>(Arrays.asList(COLUMN_GRAY_CODE));
        this.MAP = MAP;
        this.minTerms = minTerms;
        this.maxTerms = maxTerms;
    }


    public String minimize(SolutionType solutionType) {
        return initialiseSolver(solutionType).getSolution().stream()
                .map(solutionType::group)
                .collect(joining(solutionType.OUTER_DELIMITER));
    }

    public String solveFor(String variable, SolutionType solutionType) {
        return variable + " = " + initialiseSolver(solutionType).getSolution().stream()
                .filter(term -> term.contains(variable))
                .map(term -> Arrays.stream(term.split(solutionType.getInnerRegex()))
                        .filter(var -> COLUMN_VARIABLES.stream()
                                .anyMatch(var::contains))
                        .collect(joining(solutionType.INNER_DELIMITER)))
                .map(solutionType::group)
                .collect(joining(solutionType.OUTER_DELIMITER)) + "\n";
    }
    public String solveAll(SolutionType solutionType) {
        return ROW_VARIABLES.stream().map(variable -> solveFor(variable, solutionType)).collect(joining());
    }

    private KmapSolver initialiseSolver(SolutionType solutionType) {
        if(solutionType == SUM_OF_PRODUCTS) {
            if (minSolver == null)
                minSolver = new KmapSolver(this, solutionType, minTerms);
            return minSolver;
        }
        else if(maxSolver == null) maxSolver = new KmapSolver(this, solutionType, maxTerms);
        return maxSolver;
    }

    public String toString(Options... options) {
        return Arrays.stream(options).map(o -> new KmapFormatter(this).toString(o)).collect(joining("\n"));
    }

    @Override
    public String toString() {
        return new KmapFormatter(this).toString(VALUES);
    }

}
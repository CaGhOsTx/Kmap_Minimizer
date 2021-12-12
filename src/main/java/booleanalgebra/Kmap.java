package booleanalgebra;

import java.util.*;

import static booleanalgebra.Options.VALUES;
import static booleanalgebra.SolutionType.PRODUCT_OF_SUMS;
import static booleanalgebra.SolutionType.SUM_OF_PRODUCTS;
import static java.util.stream.Collectors.*;

public final class Kmap {
    final List<Variable> ROW_VARIABLES, COLUMN_VARIABLES;
    GrayCode ROW_GRAY_CODE, COLUMN_GRAY_CODE;
    final Node[][] MAP;
    Set<Node> minTerms, maxTerms;
    int minLen, maxLen;
    private KmapSolver minSolver, maxSolver;

    Kmap (List<Variable> ROW_VARIABLES, List<Variable> COLUMN_VARIABLES, GrayCode ROW_GRAY_CODE, GrayCode COLUMN_GRAY_CODE, Node[][] MAP, Set<Node> minTerms, Set<Node> maxTerms) {
        this.ROW_VARIABLES = ROW_VARIABLES;
        this.COLUMN_VARIABLES = COLUMN_VARIABLES;
        this.ROW_GRAY_CODE = ROW_GRAY_CODE;
        this.COLUMN_GRAY_CODE = COLUMN_GRAY_CODE;
        this.MAP = MAP;
        this.minTerms = minTerms;
        this.maxTerms = maxTerms;
        minLen = minTerms.size();
        maxLen = maxTerms.size();
    }

    private Variable get(String s) {
        for(var v : ROW_VARIABLES)
            if(s.equals(v.get()))
                return v;
        for(var v : COLUMN_VARIABLES)
            if(s.equals(v.get()))
                return v;
        throw new IllegalStateException("Kmap does not contain this variable");
    }

    int verticalLength() {
        return MAP.length;
    }

    int horizontalLength() {
        return MAP[0].length;
    }

    public String minimize() {
        return minimize((minLen < maxLen) ? SUM_OF_PRODUCTS : PRODUCT_OF_SUMS);
    }

    public String solveAll() {
        return solveAll((minLen < maxLen) ? SUM_OF_PRODUCTS : PRODUCT_OF_SUMS);
    }

    public String minimize(SolutionType solutionType) {
        var solution = initialiseSolver(solutionType).getMinimizedSolution().stream()
                .map(term -> term.get(solutionType))
                .map(solutionType::group)
                .collect(joining(solutionType.OUTER_DELIMITER));
        return "Minimized form(" + solutionType + "): " + solution;
    }

    public String solveFor(Variable variable, SolutionType solutionType) {
        var sb = new StringBuilder(variable.get()).append(" = ");
        if(solutionType == SUM_OF_PRODUCTS && minTerms.isEmpty())
            return sb.append(0).toString();
        else if(solutionType == PRODUCT_OF_SUMS && maxTerms.isEmpty())
            return sb.append(1).toString();
        var minimized = initialiseSolver(solutionType).getMinimizedSolution();
        if(minimized.isEmpty())
            return sb.append(solutionType.VALUE).toString();
        var outputSolution = minimized.stream()
                .filter(term -> term.containsPair(variable, solutionType == SUM_OF_PRODUCTS))
                .map(term -> term.getWithout(ROW_VARIABLES, solutionType))
                .filter(term -> !term.isEmpty())
                .map(solutionType::group)
                .collect(joining(solutionType.OUTER_DELIMITER));
        return variable + " = " + (outputSolution.isEmpty() ? "undefined" : outputSolution);
    }

    public String solveAll(SolutionType solutionType) {
        return "Solutions:\n" + ROW_VARIABLES.stream().map(variable -> solveFor(variable, solutionType)).collect(joining("\n"));
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
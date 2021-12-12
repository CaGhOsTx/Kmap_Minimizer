package booleanalgebra;

import java.util.*;
import java.util.stream.Collectors;

import static booleanalgebra.GrayCode.toDecimal;
import static booleanalgebra.TermType.MAX_TERM;
import static booleanalgebra.TermType.MIN_TERM;
import static booleanalgebra.Variable.toVariables;
import static java.lang.System.arraycopy;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toCollection;

public class KmapBuilder {
    final List<Variable> rowVariables;
    final List<Variable> columnVariables;
    private final GrayCode rowGrayCode, columnGrayCode;
    private final Map<TermType, Set<Integer>> TERMS = new HashMap<>();
    private final Set<Node> minTerms, maxTerms;

    private KmapBuilder(String[] rowVariables, String[] columnVariables) {
        this.rowVariables = toVariables(rowVariables);
        this.columnVariables = toVariables(columnVariables);
        minTerms = new HashSet<>();
        maxTerms = new HashSet<>();
        rowGrayCode = GrayCode.of(rowVariables.length);
        columnGrayCode = GrayCode.of(columnVariables.length);
    }

    public static KmapBuilder withNumberOfVariables(int numberOfVariables) {
        boolean greaterThanAlphabet = numberOfVariables > 26;
        char temp = greaterThanAlphabet ? 'X' : 'A';
        int suffix = 0;
        String[] vars = new String[numberOfVariables - 1];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = greaterThanAlphabet ? temp + " " + ++suffix : String.valueOf(++temp);
        }
        return withVariables(greaterThanAlphabet ? "X0" : "A", vars);
    }

    public static KmapBuilder withVariables(String var1, String... vars) {
        if(isNull(vars))
            throw new IllegalStateException("vars cannot be null");
        String[] columnVariables = initialiseColumnVariables(var1, vars),
                rowVariables = initializeRowVariables(vars, columnVariables);
        return new KmapBuilder(rowVariables, columnVariables);
    }

    private static String[] initialiseColumnVariables(String var, String[] vars) {
        String[] columnVariables = new String[(vars.length >> 1) + 1];
        columnVariables[0] = var;
        arraycopy(vars, 0, columnVariables, 1, columnVariables.length - 1);
        return columnVariables;
    }

    private static String[] initializeRowVariables(String[] vars, String[] columnVariables) {
        String[] rowVars = new String[vars.length - columnVariables.length + 1];
        arraycopy(vars, columnVariables.length - 1, rowVars, 0, rowVars.length);
        return rowVars;
    }

    public KmapBuilder andTerms(String... terms) {
        var termType = determineTermType(terms);
        TERMS.put(termType, Arrays.stream(terms)
                .map(term -> getGrayCodeFromTerm(term, termType))
                .map(this::getGrayCodeIndex)
                .collect(Collectors.toSet()));
        return this;
    }

    public KmapBuilder andGrayCodeTerms(TermType termType, String... gcTerms) {
        TERMS.put(termType, Arrays.stream(gcTerms)
                .map(this::getGrayCodeIndex)
                .collect(Collectors.toSet()));
        return this;
    }

    private int getGrayCodeIndex(String grayCode) {
        String tmpCGC = grayCode.substring(0, columnVariables.size());
        String tmpRGC = grayCode.substring(tmpCGC.length());
        int column = columnGrayCode.getIndex(tmpCGC);
        int row = rowGrayCode.getIndex(tmpRGC);
        return getIndex(row, column);
    }

    private String getGrayCodeFromTerm(String term, TermType t) {
        return Arrays.stream(getVariablesFromTerm(term, t))
                .map(var -> t.isMIN_TERM() ? var : complement(var).toString())
                .map(var -> grayCodeOf(var, t))
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    private String[] getVariablesFromTerm(String term, TermType t) { return term.split("[" + t.OPERATOR + "]"); }

    private int grayCodeOf(String term, TermType t) {
        if(t.isMIN_TERM())
            return Integer.parseInt(isPositive(term) ? t.VALUE : t.COMPLEMENT);
        return Integer.parseInt(isPositive(term) ? t.COMPLEMENT : t.VALUE);
    }

    private boolean isPositive(String term) {
        return term.contains(String.valueOf('\u0305'));
    }

    private TermType determineTermType(String[] terms) {
        return terms[0].contains(".") ? MIN_TERM : MAX_TERM;
    }

    public KmapBuilder andTermsAt(TermType termType, int... indexes) {
        CheckForOutOfBounds(indexes);
        TERMS.put(termType, Arrays.stream(indexes).boxed().collect(toCollection(HashSet::new)));
        return this;
    }

    private void CheckForOutOfBounds(int[] indexes) {
        if(Arrays.stream(indexes).anyMatch(this::withinRangeOfMap))
            throw new IllegalStateException("given indexes are out of bounds!");
    }

    private boolean withinRangeOfMap(int i) {
        return i < 0 || i > (int) Math.pow(2, rowVariables.size() + columnVariables.size());
    }

    private Node[][] generateMap() {
        Node[][] map = new Node[(int) Math.pow(2, rowVariables.size())][(int) Math.pow(2, columnVariables.size())];
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++)
                map[i][j] = createNode(i, j);
        }
        return map;
    }

    private Node createNode(int row, int column) {
        int index = getIndex(row, column);
        var termType = determineTermType(index);
        Term term = generateTerm(row, column, termType);
        Node n = new Node(row, column, index, term);
        addNodeToAppropriateTermSet(n);
        return n;
    }

    private void addNodeToAppropriateTermSet(Node n) {
        if(n.getTerm().getType().isMIN_TERM())
            minTerms.add(n);
        else if(n.getValue().equals("0"))
            maxTerms.add(n);
    }

    private TermType determineTermType(int index) {
        for(var tt : TERMS.keySet())
            if(TERMS.get(tt).contains(index))
                return tt;
        return TERMS.containsKey(MIN_TERM) ? MAX_TERM : MIN_TERM;
    }

    private Term generateTerm(int row, int column, TermType termType) {
        return new Term(termType, createTerm(row, column));
    }

    private HashMap<Variable, Boolean> createTerm(int row, int column) {
        var bitSet = new LinkedHashMap<Variable, Boolean>(rowVariables.size() + columnVariables.size());
        String rgc = rowGrayCode.get(row), cgc = columnGrayCode.get(column);
        for (int i = 0; i < columnVariables.size(); i++)
            bitSet.put(columnVariables.get(i), cgc.charAt(i) == '1');
        for (int i = 0; i < rowVariables.size(); i++)
            bitSet.put(rowVariables.get(i), rgc.charAt(i) == '1');
        return bitSet;
    }

    static StringBuilder complement(String term) {
        char delimiter = term.contains("+") ? '+' : '.';
        var sb = new StringBuilder();
        var variables = term.split("[.+]");
        for (var variable : variables) {
            if(variable.contains("\u0305"))
                sb.append(variable.replace("\u0305", ""));
            else {
                for(char ch : variable.toCharArray())
                    sb.append(ch).append("\u0305");
            }
            sb.append(delimiter);
        }
        return sb.deleteCharAt(sb.length() - 1);
    }

    private int getIndex(int i, int j) {
        return toDecimal(rowGrayCode.get(i), 0) + toDecimal(columnGrayCode.get(j), columnVariables.size());
    }

    public Kmap build() {
        return new Kmap(
                rowVariables,
                columnVariables,
                rowGrayCode,
                columnGrayCode,
                generateMap(),
                minTerms,
                maxTerms
        );
    }
}

package booleanalgebra;

import java.util.*;
import java.util.stream.Collectors;

import static booleanalgebra.TermType.*;
import static java.lang.System.arraycopy;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toCollection;

public class KmapBuilder {
    private  final String[] rowVariables, columnVariables, rowGrayCode, columnGrayCode;
    private final Map<TermType, Set<Integer>> TERMS = new HashMap<>();
    private final Set<Node> minTerms, maxTerms;

    private KmapBuilder(String[] rowVariables, String[] columnVariables) {
        this.rowVariables = rowVariables;
        this.columnVariables = columnVariables;
        minTerms = new HashSet<>();
        maxTerms = new HashSet<>();
        rowGrayCode = grayCode(rowVariables.length);
        columnGrayCode = grayCode(columnVariables.length);
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
        this.TERMS.put(termType, Arrays.stream(terms)
                .map(term -> getGrayCodeFromTerm(term, termType))
                .map(this::getGrayCodeIndex)
                .collect(Collectors.toSet()));
        return this;
    }

    public KmapBuilder andGrayCodeTerms(TermType termType, String... gcTerms) {
        this.TERMS.put(termType, Arrays.stream(gcTerms)
                .map(this::getGrayCodeIndex)
                .collect(Collectors.toSet()));
        return this;
    }

    private int getGrayCodeIndex(String grayCode) {
        String tmpCGC = grayCode.substring(0, columnVariables.length);
        String tmpRGC = grayCode.substring(tmpCGC.length());
        int column = getIndexFromGrayCode(tmpCGC, columnGrayCode);
        int row = getIndexFromGrayCode(tmpRGC, rowGrayCode);
        return getIndex(row, column);
    }

    private int getIndexFromGrayCode(String grayCode, String[] grayCodeArray) {
        int index = -1;
        for (int i = 0; i < grayCodeArray.length; i++) {
            if (grayCodeArray[i].equals(grayCode))
                index = i;
        }
        if(index < 0)
            throw new IllegalStateException("Invalid input format! Too few, or too many variables");
        return index;
    }

    private String getGrayCodeFromTerm(String term, TermType t) {
        return Arrays.stream(getVariablesFromTerm(term, t))
                .map(var -> t.isMIN_TERMS() ? var : complement(var).toString())
                .map(var -> grayCodeOf(var, t))
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    private String[] getVariablesFromTerm(String term, TermType t) { return term.split("[" + t.OPERATOR + "]"); }

    private int grayCodeOf(String term, TermType t) {
        if(t.isMIN_TERMS())
            return isPositive(term) ? t.VALUE : t.COMPLEMENT;
        return isPositive(term) ? t.COMPLEMENT : t.VALUE;
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
        return i < 0 || i > (int) Math.pow(2, rowVariables.length + columnVariables.length);
    }

    private Node[][] generateMap() {
        Node[][] map = new Node[(int) Math.pow(2, rowVariables.length)][(int) Math.pow(2, columnVariables.length)];
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++)
                map[i][j] = createNode(i, j);
        }
        return map;
    }

    private Node createNode(int row, int column) {
        int index = getIndex(row, column);
        var termType = determineTermType(index);
        String term = generateTerm(row, column, termType.OPERATOR);
        Node n = new Node(row, column, index, termType.VALUE, term);
        addNodeToAppropriateTermSet(n);
        return n;
    }

    private void addNodeToAppropriateTermSet(Node n) {
        if(n.getValue() == '1')
            minTerms.add(n);
        else if(n.getValue() == '0')
            maxTerms.add(n);
    }

    private TermType determineTermType(int index) {
        for(var tt : TERMS.keySet())
            if(TERMS.get(tt).contains(index))
                return tt;
        return TERMS.containsKey(MIN_TERM) ? MAX_TERM : MIN_TERM;
    }

    private String generateTerm(int row, int column, char operator) {
        var sb = generatePartialSubstring(column, columnGrayCode, columnVariables, operator)
                .append(generatePartialSubstring(row, rowGrayCode, rowVariables, operator));
        return sb.substring(0, sb.length() - 1);
    }

    private StringBuilder generatePartialSubstring(int index, String[] grayCode, String[] variables, char operator) {
        var sb = new StringBuilder();
        for (int i = 0; i < grayCode[index].length(); i++)
            sb.append(isNegative(i, grayCode[index]) ? complement(variables[i]) : variables[i])
                    .append(operator);
        return sb;
    }

    private boolean isNegative(int i, String s) {
        return s.charAt(i) == '0';
    }

    static StringBuilder complement(String term) {
        var sb = new StringBuilder();
        for (int i = 0; i < term.length(); i++)
            sb.append(term.charAt(i)).append('\u0305');
        return sb;
    }

    private int getIndex(int i, int j) {
        return toBinary(rowGrayCode[i], 0) + toBinary(columnGrayCode[j], rowVariables.length);
    }

    private static int pow2(int val) {
        if(val > 0)
            return pow2(--val) << 1;
        return 1;
    }

    private static int toBinary(String s, int start) {
        int res = 0;
        for (int i = s.length() - 1, binary = pow2(start); i >= 0; i--, binary <<= 1)
            res += s.charAt(i) == '1' ? binary : 0;
        return res;
    }

    private static String[] grayCode(int n) {
        if(n <= 0)
            throw new IllegalStateException("n cannot be non-positive");
        if(n > 1) {
            String[] arr = grayCode(--n), temp = new String[arr.length << 1];
            for (int i = 0, j = 0, length = arr.length; i < length; i++, j = j != length - 1 ? j + 1 : 0) {
                temp[i] = "0" + arr[j];
                temp[length + i] = "1" + arr[length - j - 1];
            }
            return temp;
        }
        return new String[] {"0", "1"};
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

package booleanalgebra;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static booleanalgebra.TermType.MAX_TERM;
import static booleanalgebra.TermType.MIN_TERM;
import static java.lang.System.arraycopy;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toCollection;

public class KmapBuilder {
    private  final String[] rowVariables, columnVariables, rowGrayCode, columnGrayCode;
    private Set<Integer> terms = new HashSet<>();
    private final Set<Node> minTerms;
    private TermType termType;


    private KmapBuilder(String[] rowVariables, String[] columnVariables) {
        this.rowVariables = rowVariables;
        this.columnVariables = columnVariables;
        this.minTerms = new HashSet<>();
        rowGrayCode = grayCode(rowVariables.length);
        columnGrayCode = grayCode(columnVariables.length);
    }

    public static KmapBuilder withNumberOfVariables(int numberOfVariables) {
        char temp = 'A';
        String[] vars = new String[numberOfVariables - 1];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = String.valueOf(++temp);
        }
        return withVariables("A", vars);
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

    public FinalizedBuilder andTerms(String... terms) {
        termType = determineTermType(terms);
        Arrays.stream(terms)
                .map(this::getGrayCodeFromTerm)
                .map(this::getGrayCodeIndex)
                .collect(toCollection(() -> this.terms));
        return new FinalizedBuilder();
    }

    public FinalizedBuilder andGrayCodeTerms(TermType termType, String... gcTerms) {
        this.termType = termType;
        Arrays.stream(gcTerms)
                .map(this::getGrayCodeIndex)
                .collect(toCollection(() -> this.terms));
        return new FinalizedBuilder();
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

    private String getGrayCodeFromTerm(String term) {
        return Arrays.stream(getVariablesFromTerm(term))
                .map(var -> termType.isMIN_TERMS() ? var : complement(var))
                .map(this::grayCodeOf)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    private String[] getVariablesFromTerm(String term) { return term.split("[" + termType.operator + "]"); }

    private String complement(String s) {
        return s.contains("\u0305") ? String.valueOf(s.charAt(0)) : s + '\u0305';
    }

    private int grayCodeOf(String term) {
        if(termType.isMIN_TERMS())
            return isPositive(term) ? termType.value : termType.complement;
        return isPositive(term) ? termType.complement : termType.value;
    }

    private boolean isPositive(String term) {
        return term.charAt(term.length() - 1) == '\u0305';
    }

    private TermType determineTermType(String[] terms) {
        return terms[0].contains(".") ? MIN_TERM : MAX_TERM;
    }

    public FinalizedBuilder andTermsAt(TermType termType, int... indexes) {
        CheckForOutOfBounds(indexes);
        this.termType = termType;
        terms = Arrays.stream(indexes).boxed().collect(toCollection(HashSet::new));
        return new FinalizedBuilder();
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
        String term = generateTerm(row, column, termType.operator);
        Node n = new Node(row, column, index, terms.contains(index) ? termType.value : termType.complement, term);
        if(n.getValue() == '1')
            minTerms.add(n);
        return n;
    }

    private String generateTerm(int row, int column, char operator) {
        var sb = generatePartialSubstring(column, columnGrayCode, columnVariables, operator)
                .append(generatePartialSubstring(row, rowGrayCode, rowVariables, operator));
        return sb.substring(0, sb.length() - 1);
    }

    private StringBuilder generatePartialSubstring(int index, String[] grayCode, String[] variables, char operator) {
        var sb = new StringBuilder();
        for (int i = 0; i < grayCode[index].length(); i++) {
            sb.append(variables[i]);
            if (grayCode[index].charAt(i) == '0') sb.append('\u0305');
            sb.append(operator);
        }
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

    public class FinalizedBuilder {
        private FinalizedBuilder() {}
        public Kmap build() {
            return new Kmap(
                    rowVariables,
                    columnVariables,
                    rowGrayCode,
                    columnGrayCode,
                    generateMap(),
                    minTerms
            );
        }
    }
}

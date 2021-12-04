package booleanalgebra;

import java.util.List;

import static booleanalgebra.Options.VALUES;

class KmapFormatter {
    Kmap kmap;
    String rowPadding;
    String[] columnPadding, noPadding = new String[] {"",""};
    StringBuilder sb = new StringBuilder();
    int lengthOfRowAndColumn;
    String delimiter = " | ";
    Options nodeField;

    KmapFormatter(Kmap kmap) {
        this.kmap = kmap;
    }


    private StringBuilder appendVariableList() {
        sb = appendVariables(kmap.ROW_VARIABLES);
        sb.append("\\");
        sb = appendVariables(kmap.COLUMN_VARIABLES);
        rowPadding = rowPadding();
        columnPadding = setColumnPaddingForGrayCode();
        lengthOfRowAndColumn = lengthOfRowAndColumn();
        sb.append(delimiter);
        return sb;
    }

    private StringBuilder appendVariables(List<String> columnVariables) {
        for (String s : columnVariables)
            sb.append(s);
        return sb;
    }

    private String[] setColumnPaddingForGrayCode() {
        return columnGrayCodeIsLongerThanCellData() ? noPadding : columnPadding();
    }

    public String toString() {
        return toString(VALUES);
    }

    private String rowPadding() {
        return " ".repeat(sb.length() - kmap.ROW_GRAY_CODE.get(0).length());
    }

    private StringBuilder appendBar() {
        return sb.append("\n")
                .append(rowPadding)
                .append("-".repeat(lengthOfRowAndColumn))
                .append("\n");
    }

    private int lengthOfRowAndColumn() {
        return kmap.COLUMN_GRAY_CODE.get(0).length()
                * kmap.COLUMN_GRAY_CODE.size()
                + delimiter.length() * kmap.COLUMN_GRAY_CODE.size()
                + kmap.COLUMN_GRAY_CODE.size()
                * (columnPadding[0].length() + columnPadding[1].length())
                + kmap.ROW_GRAY_CODE.get(0).length();
    }

    private String[] columnPadding() {
        int length = Math.abs(kmap.COLUMN_VARIABLES.size()
                - nodeField.from(kmap.MAP[0][0]).length()
                + getOverLineCount());
        int left = isOdd(length) ? (length >> 1) + 1 : length >> 1;
        int right = length - left;
        return new String[] {
                " ".repeat(left),
                " ".repeat(right)
        };
    }

    private boolean isOdd(int length) {
        return (length & 1) == 1;
    }

    private int getOverLineCount() {
        int count = 0;
        for(char c : nodeField.from(kmap.MAP[0][0]).toCharArray()) {
            count += c == '\u0305' ? 1 : 0;
        }
        return count;
    }

    private String center(String reference, String s) {
        if(isNotANumber(s.charAt(0)))
            return columnPadding[0] + s + columnPadding[1];
        int offset = columnPadding[0].length() - (s.length() - reference.length());
        return " ".repeat(offset) + s + columnPadding[1];
    }

    private boolean isNotANumber(char c) {
        return c < 48 || c > 57;
    }

    String toString(Options options) {
        this.nodeField = options;
        sb = appendVariableList();
        sb = appendColumnGrayCode();
        sb = appendBar();
        columnPadding = columnGrayCodeIsLongerThanCellData() ? columnPadding() : noPadding;
        sb = appendMainData();
        return sb.toString();
    }

    private StringBuilder appendMainData() {
        for (int i = 0; i < kmap.MAP.length; i++) {
            sb = appendCurrentRowGrayCode(i);
            for(Node node : kmap.MAP[i])
                sb = appendNode(node);
            sb = removeLastDelimiter();
            sb = appendBar();
        }
        return sb;
    }

    private StringBuilder removeLastDelimiter() {
        return sb.delete(sb.length() - delimiter.length(), sb.length());
    }

    private StringBuilder appendNode(Node node) {
        return sb.append(center(nodeField.from(kmap.MAP[0][0]), removeZero(node))).append(delimiter);
    }

    private String removeZero(Node node) {
        String field = nodeField.from(node);
        if(nodeField.equals(VALUES) && field.equals("0"))
            field = " ";
        return field;
    }

    private StringBuilder appendCurrentRowGrayCode(int i) {
        return sb.append(rowPadding).append(kmap.ROW_GRAY_CODE.get(i)).append(delimiter);
    }

    private StringBuilder appendColumnGrayCode() {
        for(String s : kmap.COLUMN_GRAY_CODE)
            sb.append(center(kmap.COLUMN_GRAY_CODE.get(0), s)).append(delimiter);
        return removeLastDelimiter();
    }

    private boolean columnGrayCodeIsLongerThanCellData() {
        return kmap.COLUMN_VARIABLES.size() > nodeField.from(kmap.MAP[0][0]).length();
    }
}

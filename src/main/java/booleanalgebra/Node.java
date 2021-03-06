package booleanalgebra;

import java.util.Objects;

final class Node {
    private final String implicant;
    private final int index, row, column;
    private final char value;

    Node(int row, int column, int index, char value, String implicant) {
        this.index = index;
        this.value = value;
        this.implicant = implicant;
        this.row = row;
        this.column = column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Node) {
            Node node = (Node) o;
            return index == node.index
                    && value == node.value
                    && row == node.row
                    && column == node.column
                    && implicant.equals(node.implicant);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.valueOf(index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(implicant, index, value, row, column);
    }

    int getRow() {return row;}

    int getColumn() {return column;}

    String getImplicant() {
        return implicant;
    }

    int getIndex() {
        return index;
    }

    char getValue() {
        return value;
    }

    int[] getRCMatrix() {
        return new int[] {row, column};
    }
}
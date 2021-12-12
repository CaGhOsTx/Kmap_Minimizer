package booleanalgebra;

import java.util.Objects;

final class Node {
    private final Term term;
    private final int index, row, column;

    Node(int row, int column, int index, Term term) {
        this.index = index;
        this.term = term;
        this.row = row;
        this.column = column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Node) {
            Node node = (Node) o;
            return index == node.index
                    && row == node.row
                    && column == node.column
                    && term.equals(node.term);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.valueOf(index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(term, index, row, column);
    }

    Term getTerm() {
        return term;
    }

    int getIndex() {
        return index;
    }

    String getValue() {
        return term.getType().VALUE;
    }

    int[] getRCMatrix() {
        return new int[] {row, column};
    }
}
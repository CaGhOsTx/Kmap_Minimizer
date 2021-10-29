package booleanalgebra;

import java.util.function.Function;

public enum Options {
    TERMS(Node::getImplicant),
    VALUES(node -> String.valueOf(node.getValue())),
    INDEXES(node -> String.valueOf(node.getIndex()));

    private final Function<Node, String> f;

    Options(Function<Node, String> f) {
        this.f = f;
    }

    String from(Node n) {
        return f.apply(n);
    }

}
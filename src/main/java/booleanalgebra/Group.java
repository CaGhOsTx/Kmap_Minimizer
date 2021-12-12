package booleanalgebra;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Group {
    private final Queue<Term> TERMS;

    private Group (Queue<Term> terms) {
        TERMS = terms;
    }

    static Group of(Queue<Term> nodes) {
        if(nodes.isEmpty())
            throw new IllegalStateException("Group must contain at least one element");
        return new Group(nodes);
    }
    Term reduce() {
        return new Term(requireNonNull(TERMS.peek()).getType(), nxor());
    }

    Map<Variable, Boolean> nxor() {
        Map<Variable, Boolean> reduced = new LinkedHashMap<>(requireNonNull(TERMS.poll()).getBitSet());
        while(!TERMS.isEmpty()) {
            var tmpBitSet = TERMS.poll().getBitSet();
            for(var v : tmpBitSet.keySet()) {
                if(reduced.containsKey(v) && !reduced.get(v).equals(tmpBitSet.get(v)))
                    reduced.remove(v);
            }
        }
        return reduced;
    }
}

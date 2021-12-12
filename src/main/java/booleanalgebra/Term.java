package booleanalgebra;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

final class Term {
    private Map<Variable, Boolean> bitSet;
    private final TermType type;

    public Term(TermType type, Map<Variable, Boolean> bitSet) {
        this.bitSet = bitSet;
        this.type = type;
    }

    boolean containsPair(Variable v, boolean b) {
        return bitSet.containsKey(v) && bitSet.get(v).equals(b);
    }

    public Map<Variable, Boolean> getBitSet () {
        return bitSet;
    }

    void complement() {
        bitSet = bitSet.keySet().stream().collect(toMap(k -> k, k -> !bitSet.get(k)));
    }

    String getWithout(List<Variable> variables, SolutionType type) {
        return bitSet.keySet().stream()
                .filter(v -> !variables.contains(v))
                .map(v -> v.get(bitSet.get(v)))
                .collect(joining(type.getDerivedTermType().OPERATOR));
    }

    public TermType getType() {
        return type;
    }

    public String get(SolutionType type) {
        return bitSet.keySet().stream()
                .map(v -> v.get(bitSet.get(v)))
                .collect(joining(type.getDerivedTermType().OPERATOR));
    }

    @Override
    public String toString() {
        return bitSet.keySet().stream()
                .map(v -> v.get(bitSet.get(v)))
                .collect(joining(type.OPERATOR));
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bitSet, type);
    }
}

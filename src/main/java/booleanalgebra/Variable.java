package booleanalgebra;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.unmodifiableList;

final class Variable {
    private final String VALUE, COMPLEMENT;
    static final char overline = '\u0305';

    public Variable(String variable) {
        this.VALUE = variable.intern();
        this.COMPLEMENT = complement(variable).intern();
    }

    private String complement(String value) {
        var sb = new StringBuilder();
        for(char c : value.toCharArray())
            sb.append(c).append(overline);
        return sb.toString();
    }

    public String get() {
        return get(true);
    }

    public String get(boolean b) {
        return b ? VALUE : COMPLEMENT;
    }

    public String toString() {
        return VALUE;
    }

    public static List<Variable> toVariables(String... variables) {
        var list = new ArrayList<Variable>(variables.length);
        for(var v : variables)
            list.add(new Variable(v));
        return unmodifiableList(list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(VALUE, COMPLEMENT);
    }
}

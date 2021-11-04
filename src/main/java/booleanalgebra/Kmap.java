package booleanalgebra;

import java.util.*;
import java.util.stream.Collectors;

import static booleanalgebra.Options.VALUES;
import static java.util.stream.Collectors.*;

public final class Kmap {

    final String[] rowVariables, columnVariables, rowGrayCode, columnGrayCode;
    final Node[][] map;
    Set<Node> minTerms;

    Kmap (String[] rowVariables, String[] columnVariables, String[] rowGrayCode, String[] columnGrayCode, Node[][] map, Set<Node> minTerms) {
        this.rowVariables = rowVariables;
        this.columnVariables = columnVariables;
        this.rowGrayCode = rowGrayCode;
        this.columnGrayCode = columnGrayCode;
        this.map = map;
        this.minTerms = minTerms;
    }

    private List<Set<String>> getGroups() {
        List<Set<String>> groups = new ArrayList<>();
        while(minTerms.iterator().hasNext()) {
            var n = minTerms.iterator().next();
            groups.add(findMaxGroup(n.getRow(), n.getColumn()));
            minTerms = removeGroupedMinTerms(groups);
        }
        return groups;
    }

    private Set<Node> removeGroupedMinTerms(List<Set<String>> groups) {
        return minTerms.stream()
                .filter(node -> !groups.get(groups.size() - 1).contains(node.getImplicant()))
                .collect(toSet());
    }

    public String minimize() {
        return getGroups().stream().map(this::solve).collect(Collectors.joining(" + "));
    }

    private String solve(Set<String> implicants) {
        if(implicants.size() == 1)
            return implicants.iterator().next();
        var sb = new StringBuilder();
        var vars = splitIntoVariables(implicants);
        for(int i = 0; i < vars[0].length; i++) {
            boolean initialState = vars[0][i].length() == 1, hasChanged = false;
            for (String[] var : vars) {
                hasChanged = initialState ^ var[i].length() == 1;
                if (hasChanged)
                    break;
            }
            if(!hasChanged)
                sb.append(vars[0][i]).append('.');
        }
        return sb.isEmpty() ? "" : sb.substring(0, sb.length() - 1);
    }

    private String[][] splitIntoVariables(Set<String> implicants) {
        return implicants.stream().map(impl -> impl.split("\\.")).toArray(String[][]::new);
    }

    private Set<String> findMaxGroup(int i, int j) {
        Queue<Set<String>> setOfPossibleGroups = new PriorityQueue<>(Comparator.<Set<String>, Integer>comparing(Set::size).reversed());
        for(var direction : Direction.values())
            setOfPossibleGroups.add(new HashSet<>(traverseFrom(i, j, direction)));
        return setOfPossibleGroups.poll();
    }

    private Deque<String> traverseFrom(int i, int j, Direction direction) {
        Deque<String> deque = new LinkedList<>();
        while(map[i][j].getValue() != 0) {
            if(deque.contains(map[i][j].getImplicant())) break;
            deque.add(map[i][j].getImplicant());
            if(direction.isHorizontal())
                i = direction.apply(i, map.length);
            else
                j = direction.apply(j, map[i].length);
        }
        return returnDequeWithPow2Elements(deque);
    }

    private Deque<String> returnDequeWithPow2Elements(Deque<String> deque) {
        while(!isApPowerOf2(deque.size()))
            deque.removeLast();
        return deque;
    }

    private boolean isApPowerOf2(int groupLength) {
        int i = 1;
        while(i <= groupLength) {
            if(i == groupLength)
                return true;
            i <<= 1;
        }
        return false;
    }

    public String toString(Options... options) {
        return Arrays.stream(options).map(o -> new KmapFormatter(this).toString(o)).collect(joining("\n"));
    }

    @Override
    public String toString() {
        return new KmapFormatter(this).toString(VALUES);
    }

}
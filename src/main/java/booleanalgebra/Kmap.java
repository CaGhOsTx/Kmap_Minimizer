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
            groups.add(findMaxGroup(minTerms.iterator().next()));
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
        return String.join(".", getResultantExpression(splitIntoVariables(implicants)));
    }

    private List<String> getResultantExpression(String[][] variables) {
        var expression = new ArrayList<String>();
        for(int i = 0; i < variables[0].length; i++) {
            boolean initialState = variableIsPositive(variables[0][i].length()), hasChanged = false;
            for (String[] varRow : variables) {
                if (hasChanged = nextVariableIsInverted(varRow[i], initialState))
                    break;
            }
            if(!hasChanged)
                expression.add(variables[0][i]);
        }
        return expression;
    }

    private boolean nextVariableIsInverted(String variable, boolean initialState) {
        return initialState ^ variableIsPositive(variable.length());
    }

    private boolean variableIsPositive(int variableStringLength) {
        return variableStringLength == 1;
    }

    private String[][] splitIntoVariables(Set<String> implicants) {
        return implicants.stream().map(impl -> impl.split("\\.")).toArray(String[][]::new);
    }

    private Set<String> findMaxGroup(Node n) {
        Queue<Set<String>> groups = new PriorityQueue<>(Comparator.<Set<String>, Integer>comparing(Set::size).reversed());
        for(var direction : Direction.values())
            groups.add(groupFromNode(n, direction));
        return groups.poll();
    }

    private HashSet<String> groupFromNode(Node n, Direction direction) {
        return new HashSet<>(traverseFrom(n.getRow(), n.getColumn(), direction));
    }

    private Deque<String> traverseFrom(int row, int column, Direction direction) {
        Deque<String> deque = new LinkedList<>();
        while(map[row][column].getValue() == 1) {
            if(deque.contains(map[row][column].getImplicant())) break;
            deque.add(map[row][column].getImplicant());
            if(isEligibleForCircularTraversal(deque)) {
                List<String> group = traverseCircular(row, column, deque.size(), direction);
                if(!group.isEmpty()) {
                    deque.addAll(group);
                    return deque;
                }
            }
            if(direction.isHorizontal())
                column = direction.apply(column, map[row].length);
            else
                row = direction.apply(row, map.length);
        }
        return trimToPow2(deque);
    }

    private boolean isEligibleForCircularTraversal(Deque<String> deque) {
        return deque.size() != 1 && isApPowerOf2(deque.size());
    }

    private List<String> traverseCircular(int i, int j, int size, Direction initialDirection) {
        List<String> list = new LinkedList<>();
        Direction direction = initialDirection.iterator().next();
        size--;
        int count = 0;
        while (size > 0) {
            if(direction.isHorizontal())
                j = direction.apply(j, map[i].length);
            else
                i = direction.apply(i, map.length);
            if(map[i][j].getValue() == 1)
                list.add(map[i][j].getImplicant());
            else
                return Collections.emptyList();
            if(++count == size)
                direction = direction.iterator().next();
            if(count == size << 1) {
                count = 0;
                size--;
            }
        }
        return list;
    }

    private Deque<String> trimToPow2(Deque<String> deque) {
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
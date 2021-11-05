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
        var sb = new StringBuilder();
        var vars = splitIntoVariables(implicants);
        for(int i = 0; i < vars[0].length; i++) {
            boolean initialState = vars[0][i].length() == 1, hasChanged = false;
            for (String[] varRow : vars) {
                hasChanged = initialState ^ varRow[i].length() == 1;
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
        while(map[row][column].getValue() != 0) {
            if(deque.contains(map[row][column].getImplicant())) break;
            deque.add(map[row][column].getImplicant());
            if(isApPowerOf2(deque.size())) {
                List<String> group = tryCircular(row, column, deque.size(), direction);
                if(!group.isEmpty()) {
                    deque.addAll(group);
                    return deque;
                }
            }
            if(direction.isHorizontal())
                row = direction.apply(row, map.length);
            else
                column = direction.apply(column, map[row].length);
        }
        return trimToPow2(deque);
    }

    private List<String> tryCircular(int i, int j, int range, Direction initialDirection) {
        List<String> list = new LinkedList<>();
        Direction direction = initialDirection.iterator().next();
        System.out.println(direction);
        int count = 0;
        while (map[i][j].getValue() != 0 && range > 0) {
            if(direction.isHorizontal())
                i = direction.apply(i, map.length);
            else
                j = direction.apply(j, map[i].length);
            System.out.println(i + " : " + j);
            list.add(map[i][j].getImplicant());
            if(++count == 2) {
                range--;
                count = 0;
                direction = direction.iterator().next();
                System.out.println(direction);
            }
        }
        return range == 0 ? list : Collections.emptyList();
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
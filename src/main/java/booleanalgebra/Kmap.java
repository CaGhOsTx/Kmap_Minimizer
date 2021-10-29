package booleanalgebra;

import java.util.*;
import java.util.stream.IntStream;

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

    public String getCombinationsAndRespectiveResults() {
        var sb = new StringBuilder();
        var pq = getCombinations();
        int i = 0;
        while(!pq.isEmpty()) {
            i++;
            var set = pq.poll();
            for(var s : set) {
                var exp = s.toString();
                var res = solve(new ArrayList<>(s));
                sb.append(String.format("row %d: %s = %s%n", i,
                        exp.replaceAll(", ", " + ").substring(1, exp.length()),
                        res));
            }
        }
        return sb.toString();
    }

    private Queue<Set<Set<String>>> getCombinations() {
        var compareBySize = Comparator.<Set<Set<String>>, Integer>comparing(Set::size).reversed();
        Queue<Set<Set<String>>> queue = new PriorityQueue<>(compareBySize);
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j].getValue() == 1)
                    queue.add(getSetOfAllGroupsFromImplicant(i, j, queue));
            }
        }
        return removeEmptySetsFromQueue(compareBySize, queue);
    }

    private Queue<Set<Set<String>>> removeEmptySetsFromQueue(Comparator<Set<Set<String>>> compareBySize, Queue<Set<Set<String>>> queue) {
        return queue.stream()
                .filter(sets -> !sets.isEmpty())
                .collect(toCollection(() -> new PriorityQueue<>(compareBySize)));
    }

    private Set<Set<String>> getSetOfAllGroupsFromImplicant(int row, int column, Queue<Set<Set<String>>> queue) {
        return findGroupsFrom(row, column).stream()
                .collect(groupingBy(Set::size, toSet()))
                .entrySet().stream().max(Map.Entry.comparingByKey())
                .orElseThrow().getValue().stream()
                .filter(group -> isNotSubsetInTheQueue(queue, group))
                .collect(toSet());
    }

    private boolean isNotSubsetInTheQueue(Queue<Set<Set<String>>> queue, Set<String> possibleGroup) {
        return queue.stream().flatMap(Set::stream)
                .noneMatch(group -> possibleGroup.stream()
                        .filter(group::contains)
                        .count() >= possibleGroup.size()
                );
    }

    public String minimize() {
        var sb = new StringBuilder();
        var resultantCombinations = getResultantCombinations();
        for (int i = 0; i < resultantCombinations.size(); i++) {
            var formattedString = resultantCombinations.get(i).toString()
                    .replaceAll(", ", " + ");
            sb.append(String.format("Combination %d: %s%n", i + 1,
                    formattedString.substring(1, formattedString.length() - 1)));
        }
        return sb.toString();
    }

    private String solve(List<String> list) {
        if(list.size() == 1)
            return list.get(0);
        var sb = new StringBuilder();
        var vars = splitIntoVariables(list);
        for(int i = 0; i < vars[0].length; i++) {
            boolean initialState = vars[0][i].length() == 1, hasChanged = false;
            for (String[] var : vars) {
                hasChanged = initialState  ^ var[i].length() == 1;
                if (hasChanged)
                    break;
            }
            if(!hasChanged)
                sb.append(vars[0][i]).append('.');
        }
        return sb.isEmpty() ? "" : sb.substring(0, sb.length() - 1);
    }

    private String[][] splitIntoVariables(List<String> implicants) {
        String[][] variables = new String[implicants.size()][rowVariables.length + columnVariables.length];
        for (int i = 0; i < implicants.size(); i++)
            variables[i] = implicants.get(i).split("\\.");
        return variables;
    }

    private List<ArrayList<String>> getResultantCombinations() {
        var queue = getCombinations();
        int numberOfCombinations = queue.stream().map(Set::size).reduce((i,j) -> i * j).orElseThrow();
        List<ArrayList<String>> resultGroup = constructBuffer(numberOfCombinations);
        while(!queue.isEmpty()) {
            var group = queue.poll().stream().map(ArrayList::new).map(this::solve).collect(toList());
            int i = 0;
            do {
                for (int j = 0; j < group.size(); i++, j++) {
                    resultGroup.get(i).add(group.get(j));
                }
            }while(i < numberOfCombinations);
        }
        return resultGroup;
    }

    private List<ArrayList<String>> constructBuffer(int numberOfCombinations) {
        return IntStream.range(0, numberOfCombinations).mapToObj(ArrayList<String>::new).collect(toList());
    }

    private Set<Set<String>> findGroupsFrom (int i, int j) {
        Set<Set<String>> setOfPossibleGroups = new HashSet<>();
        for(var direction : Direction.values())
            setOfPossibleGroups.add(new HashSet<>(traverseFrom(i, j, direction)));
        return setOfPossibleGroups;
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

    public String toString(Options options) {
        return new KmapFormatter(this).toString(options);
    }

    @Override
    public String toString() {
        return new KmapFormatter(this).toString(VALUES);
    }

}
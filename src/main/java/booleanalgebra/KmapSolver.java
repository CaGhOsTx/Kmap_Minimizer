package booleanalgebra;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

final class KmapSolver {
    private final SolutionType solutionType;
    private final Node[][] MAP;
    private final int[] BOUNDARIES;
    private Set<Node> terms;
    private final List<String> solution;

    public KmapSolver(Kmap kmap, SolutionType solutionType, Set<Node> terms) {
        MAP = kmap.MAP;
        BOUNDARIES = new int[] {MAP.length, MAP[0].length};
        this.solutionType = solutionType;
        this.terms = terms;
        var groupStream = getGroups().stream();
        if(solutionType == SolutionType.PRODUCT_OF_SUMS)
            groupStream = groupStream.map(q -> q.stream().map(KmapBuilder::complement)
                            .map(StringBuilder::toString)
                            .collect(Collectors.toCollection(ArrayDeque::new)));
        solution = groupStream.map(group -> reduce(solutionType, group))
                .collect(toList());
    }

    public List<String> getSolution() {
        return solution;
    }

    private Deque<Queue<String>> getGroups() {
        Deque<Queue<Node>> groups = new ArrayDeque<>();
        while (terms.iterator().hasNext()) {
            groups.add(findMaxGroup(terms.iterator().next()));
            terms = removeGroupedMinTerms(terms, groups);
        }
        return mapFromNodeToImplicant(groups);
    }

    private Deque<Queue<String>> mapFromNodeToImplicant(Deque<Queue<Node>> groups) {
        return groups.stream()
                .map(q -> q.stream()
                        .map(Node::getImplicant)
                        .collect(Collectors.toCollection(ArrayDeque::new)))
                .collect(Collectors.toCollection(ArrayDeque::new));
    }

    private Set<Node> removeGroupedMinTerms(Set<Node> terms, Deque<Queue<Node>> groups) {
        return terms.stream()
                .filter(node -> !groups.getLast().contains(node))
                .collect(toSet());
    }

    private String reduce(SolutionType solutionType, Queue<String> implicants) {
        if (implicants.size() == 1)
            return implicants.poll();
        return String.join(solutionType.INNER_DELIMITER, getResultantExpression(splitIntoVariables(implicants)));
    }

    private List<String> getResultantExpression(String[][] variables) {
        var expression = new ArrayList<String>();
        for (int i = 0; i < variables[0].length; i++) {
            boolean initialState = variableIsPositive(variables[0][i]), hasChanged = false;
            for (String[] varRow : variables) {
                hasChanged = nextVariableIsInverted(varRow[i], initialState);
                if (hasChanged)
                    break;
            }
            if (!hasChanged)
                expression.add(variables[0][i]);
        }
        return expression;
    }

    private boolean nextVariableIsInverted(String variable, boolean initialState) {
        return initialState ^ variableIsPositive(variable);
    }

    private boolean variableIsPositive(String variable) {
        return variable.charAt(variable.length() - 1) == '\u0305';
    }

    private String[][] splitIntoVariables(Queue<String> implicants) {
        return implicants.stream().map(impl -> impl.split(solutionType.getInnerRegex())).toArray(String[][]::new);
    }

    private Queue<Node> findMaxGroup(Node n) {
        Queue<Queue<Node>> groups = new PriorityQueue<>(Comparator.<Queue<Node>, Integer>comparing(Queue::size).reversed());
        for (var direction : Direction.values())
            groups.add(groupFromNode(n, direction));
        return groups.poll();
    }

    Deque<Node> trimToPow2(Deque<Node> deque, int lengthOfBase) {
        int length = lengthOfBase + deque.size();
        while (!isAPowerOf2(length)) {
            deque.removeLast();
            length--;
        }
        return deque;
    }

    private Deque<Node> groupFromNode(Node n, Direction direction) {
        Deque<Node> group = traverseFrom(n.getRCMatrix(), direction);
        if (group.size() > 1)
            group.addAll(lookForLargerGroups(direction, group.size(), group.getLast().getRCMatrix(), direction.next()));
        return group;
    }

    private Deque<Node> lookForLargerGroups(Direction original, int limit, int[] RCMatrix, Direction side) {
        Deque<Node> buffer = new ArrayDeque<>();
        side.advance(RCMatrix, BOUNDARIES);
        int i = 0;
        for (var direction = original.opposite(); isNotOverlappingAndValid(RCMatrix, i, direction); direction = direction.opposite()) {
            var possibleGroup = traverseFrom(RCMatrix, direction, limit);
            if (possibleGroup.size() != limit) break;
            buffer.addAll(possibleGroup);
            side.advance(RCMatrix, BOUNDARIES);
        }
        return trimToPow2(buffer, limit);
    }

    private boolean isNotOverlappingAndValid(int[] RCMatrix, int i, Direction direction) {
        return i < (direction.isHorizontal() ? MAP[0].length : MAP.length) && isValid(MAP[RCMatrix[0]][RCMatrix[1]]);
    }

    private Deque<Node> traverseFrom(int[] rcMatrix, Direction direction) {
        return traverseFrom(rcMatrix, direction, direction.isHorizontal() ? MAP[0].length : MAP.length);
    }

    private Deque<Node> traverseFrom(int[] rcMatrix, Direction direction, int limit) {
        Deque<Node> group = new ArrayDeque<>(), buffer = new ArrayDeque<>();
        for (int i = 0; i < limit && isValid(MAP[rcMatrix[0]][rcMatrix[1]]); i++) {
            if (MAP[rcMatrix[0]][rcMatrix[1]] == group.peekFirst()) break;
            buffer.add(MAP[rcMatrix[0]][rcMatrix[1]]);
            if (isAPowerOf2(buffer.size() + group.size()))
                unloadBuffer(group, buffer);
            direction.advance(rcMatrix, BOUNDARIES);
        }
        return group;
    }

    private boolean isValid(Node node) {
        return node.getValue() == solutionType.VALUE || node.getValue() == 'x';
    }

    private void unloadBuffer(Queue<Node> max, Queue<Node> buffer) {
        max.addAll(buffer);
        buffer.clear();
    }

    private boolean isAPowerOf2(int groupLength) {
        return groupLength > 0 && (groupLength & (groupLength - 1)) == 0;
    }
}

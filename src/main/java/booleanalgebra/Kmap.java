package booleanalgebra;

import java.util.*;
import java.util.stream.Collectors;

import static booleanalgebra.Options.VALUES;
import static java.util.stream.Collectors.*;

public final class Kmap {

    final String[] ROW_VARIABLES, COLUMNS_VARIABLES, ROW_GRAY_CODE, COLUMN_GRAY_CODE;
    final Node[][] MAP;
    Set<Node> minTerms, maxTerms;
    private final int[] BOUNDARIES;

    Kmap (String[] ROW_VARIABLES, String[] COLUMNS_VARIABLES, String[] ROW_GRAY_CODE, String[] COLUMN_GRAY_CODE, Node[][] MAP, Set<Node> minTerms, Set<Node> maxTerms) {
        this.ROW_VARIABLES = ROW_VARIABLES;
        this.COLUMNS_VARIABLES = COLUMNS_VARIABLES;
        this.ROW_GRAY_CODE = ROW_GRAY_CODE;
        this.COLUMN_GRAY_CODE = COLUMN_GRAY_CODE;
        this.MAP = MAP;
        this.minTerms = minTerms;
        this.maxTerms = maxTerms;
        BOUNDARIES = new int[] {MAP.length, MAP[0].length};
    }
    private Deque<Queue<String>> getGroups() {
        Deque<Queue<Node>> groups = new ArrayDeque<>();
        while(minTerms.iterator().hasNext()) {
            groups.add(findMaxGroup(minTerms.iterator().next()));
            minTerms = removeGroupedMinTerms(groups);
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

    private Set<Node> removeGroupedMinTerms(Deque<Queue<Node>> groups) {
        return minTerms.stream()
                .filter(node -> !groups.getLast().contains(node))
                .collect(toSet());
    }

    public String minimize() {
        String solution = getGroups().stream().map(this::solve).collect(joining(" + "));
        return solution.isBlank() ? "No solution!" : solution;
    }

    private String solve(Queue<String> implicants) {
        if(implicants.size() == 1)
            return implicants.poll();
        return String.join(".", getResultantExpression(splitIntoVariables(implicants)));
    }

    private List<String> getResultantExpression(String[][] variables) {
        var expression = new ArrayList<String>();
        for(int i = 0; i < variables[0].length; i++) {
            boolean initialState = variableIsPositive(variables[0][i]), hasChanged = false;
            for (String[] varRow : variables) {
                hasChanged = nextVariableIsInverted(varRow[i], initialState);
                if (hasChanged)
                    break;
            }
            if(!hasChanged)
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
        return implicants.stream().map(impl -> impl.split("\\.")).toArray(String[][]::new);
    }

    private Queue<Node> findMaxGroup(Node n) {
        Queue<Queue<Node>> groups = new PriorityQueue<>(Comparator.<Queue<Node>, Integer>comparing(Queue::size).reversed());
        for(var direction : Direction.values())
            groups.add(groupFromNode(n, direction));
        return groups.poll();
    }

    Deque<Node> trimToPow2(Deque<Node> deque, int lengthOfBase) {
        int length = lengthOfBase + deque.size();
        while(!isAPowerOf2(length)) {
            deque.removeLast();
            length--;
        }
        return deque;
    }

    private Deque<Node> groupFromNode(Node n, Direction direction) {
        Deque<Node> group = traverseFrom(n.getRCMatrix(), direction, 0);
        if(group.size() > 1)
            group.addAll(lookForLargerGroups(direction, group.size(), group.getLast().getRCMatrix(), direction.next()));
        return group;
    }

    private Deque<Node> lookForLargerGroups(Direction original, int limit, int[] RCMatrix, Direction side) {
        Deque<Node> buffer = new ArrayDeque<>();
        side.advance(RCMatrix, BOUNDARIES);
        int i = 0;
        for (var direction = original.opposite(); i < (direction.isHorizontal() ? MAP[0].length : MAP.length) - 1 && isValid(MAP[RCMatrix[0]][RCMatrix[1]]); direction = direction.opposite()) {
            var possibleGroup = traverseFrom(RCMatrix, direction, limit);
            if(possibleGroup.size() != limit) break;
            buffer.addAll(possibleGroup);
            side.advance(RCMatrix, BOUNDARIES);
        }
        return trimToPow2(buffer, limit);
    }

    private Deque<Node> traverseFrom(int[] rcMatrix, Direction direction, int limit) {
        Deque<Node> group = new ArrayDeque<>(), buffer = new ArrayDeque<>();
        for(int i = 0; (limit == 0 || i < limit) && isValid(MAP[rcMatrix[0]][rcMatrix[1]]); i++) {
            if(MAP[rcMatrix[0]][rcMatrix[1]] == group.peekFirst()) break;
            buffer.add(MAP[rcMatrix[0]][rcMatrix[1]]);
            if(isAPowerOf2(buffer.size() + group.size()))
                unloadBuffer(group, buffer);
            direction.advance(rcMatrix, BOUNDARIES);
        }
        return group;
    }

    private boolean isValid(Node node) {
        return node.getValue() == '1' || node.getValue() == 'x';
    }

    private void unloadBuffer(Queue<Node> max, Queue<Node> buffer) {
        max.addAll(buffer);
        buffer.clear();
    }

    private boolean isAPowerOf2(int groupLength) {
        return groupLength > 0 && (groupLength & (groupLength - 1)) == 0;
    }

    public String toString(Options... options) {
        return Arrays.stream(options).map(o -> new KmapFormatter(this).toString(o)).collect(joining("\n"));
    }

    @Override
    public String toString() {
        return new KmapFormatter(this).toString(VALUES);
    }

}
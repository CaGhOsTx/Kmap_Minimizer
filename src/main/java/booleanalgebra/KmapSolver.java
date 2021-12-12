package booleanalgebra;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

final class KmapSolver {
    private final SolutionType solutionType;
    private final Kmap KMAP;
    private final int[] BOUNDARIES;
    private Set<Node> terms;
    private List<Term> solution;

    public KmapSolver(Kmap kmap, SolutionType solutionType, Set<Node> terms) {
        KMAP = kmap;
        BOUNDARIES = new int[] {kmap.verticalLength(), kmap.horizontalLength()};
        this.solutionType = solutionType;
        this.terms = terms;
    }

    public List<Term> getMinimizedSolution() {
        if(solution != null)
            return solution;
        return solution = getGroups().stream().map(Group::reduce).collect(toList());
    }

    private List<Group> getGroups() {
        Deque<Queue<Node>> groups = new ArrayDeque<>();
        var iterator = terms.iterator();
        while (iterator.hasNext()) {
            groups.add(findMaxGroup(iterator.next()));
            terms = removeGroupedMinTerms(terms, groups);
            System.out.println(terms.size());
            iterator = terms.iterator();
        }
        return toGroup(groups);
    }

    private List<Group> toGroup(Deque<Queue<Node>> groups) {
        List<Group> tmp = new ArrayList<>();
        for(var q : groups) {
            for(var n : q)
                if(solutionType == SolutionType.PRODUCT_OF_SUMS)
                    n.getTerm().complement();
            tmp.add(Group.of(q.stream().map(Node::getTerm).collect(Collectors.toCollection(ArrayDeque::new))));
        }
        return tmp;
    }

    private Set<Node> removeGroupedMinTerms(Set<Node> terms, Deque<Queue<Node>> groups) {
        return terms.stream()
                .filter(node -> !groups.getLast().contains(node))
                .collect(toSet());
    }

    private Queue<Node> findMaxGroup(Node n) {
        Queue<Queue<Node>> groups = new PriorityQueue<>(Comparator.<Queue<Node>, Integer>comparing(Queue::size).reversed());
        for (var direction : Direction.values())
            groups.add(groupFromNode(n, direction));
        return groups.poll();
    }

    private Deque<Node> trimToPow2(Deque<Node> deque, int lengthOfBase) {
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
        var originalMatrix = new int[] {RCMatrix[0], RCMatrix[1]};
        side.advance(RCMatrix, BOUNDARIES);
        int i = 0;
        for (var direction = original.opposite();isValid(KMAP.MAP[RCMatrix[0]][RCMatrix[1]]) && equals(originalMatrix, RCMatrix); direction = direction.opposite()) {
            var possibleGroup = traverseFrom(RCMatrix, direction, limit);
            if (possibleGroup.size() != limit) break;
            buffer.addAll(possibleGroup);
            side.advance(RCMatrix, BOUNDARIES);
        }
        return trimToPow2(buffer, limit);
    }

    private static boolean equals(int[] a, int[] b) {
        if(a.length != b.length)
            return false;
        for (int i = 0; i < a.length ; i++)
            if(a[i] != b[i]) return false;
        return true;
    }

    private Deque<Node> traverseFrom(int[] rcMatrix, Direction direction) {
        return traverseFrom(rcMatrix, direction, direction.isHorizontal() ? BOUNDARIES[1] : BOUNDARIES[0]);
    }

    private Deque<Node> traverseFrom(int[] rcMatrix, Direction direction, int limit) {
        Deque<Node> group = new ArrayDeque<>(), buffer = new ArrayDeque<>();
        for (int i = 0; i < limit && isValid(KMAP.MAP[rcMatrix[0]][rcMatrix[1]]); i++) {
            if (KMAP.MAP[rcMatrix[0]][rcMatrix[1]] == group.peekFirst()) break;
            buffer.add(KMAP.MAP[rcMatrix[0]][rcMatrix[1]]);
            if (isAPowerOf2(buffer.size() + group.size()))
                unloadBuffer(group, buffer);
            direction.advance(rcMatrix, BOUNDARIES);
        }
        return group;
    }

    private boolean isValid(Node node) {
        return node.getValue().equals(solutionType.VALUE) || node.getValue().equals("x");
    }

    private void unloadBuffer(Queue<Node> max, Queue<Node> buffer) {
        max.addAll(buffer);
        buffer.clear();
    }

    private boolean isAPowerOf2(int groupLength) {
        return groupLength > 0 && (groupLength & (groupLength - 1)) == 0;
    }
}

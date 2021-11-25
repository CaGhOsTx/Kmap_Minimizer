package booleanalgebra;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class KmapTest {

    @Test
    void minTermsSetTest1() {
        Assertions.assertEquals(
                KmapBuilder.withNumberOfVariables(4).andTermsAt(TermType.MIN_TERM, 0,4,5,9).build().minTerms.stream()
                .map(Node::getIndex).collect(Collectors.toSet()),
                Set.of(0,4,5,9));
    }

    @Test
    void minTermsSetTest2() {
        var indexes = Set.of(0,4,5,9);
        Assertions.assertEquals(
                KmapBuilder.withNumberOfVariables(4).andTermsAt(TermType.MAX_TERM, 0,4,5,9).build().minTerms.stream()
                        .map(Node::getIndex).collect(Collectors.toSet()),
                IntStream.range(0, 16).filter(i -> !indexes.contains(i)).boxed().collect(Collectors.toSet()));
    }
}

import booleanalgebra.KmapBuilder;
import booleanalgebra.Kmap;
import booleanalgebra.Options;

import java.math.BigInteger;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static booleanalgebra.TermType.MAX_TERMS;
import static booleanalgebra.TermType.MIN_TERMS;
import static java.math.BigInteger.ONE;


public class Main {
    public static void main(String[] args) {
        int n = 4;
        Kmap kmap = KmapBuilder.withNumberOfVariables(4).andTermsAt(MIN_TERMS, IntStream.generate(() -> ThreadLocalRandom.current().nextInt(16)).limit(10).toArray()).build();
        System.out.println(kmap.toString(Options.values()));
        System.out.println("Solution:");
        System.out.println(kmap.minimize());
    }
}

import booleanalgebra.Kmap;
import booleanalgebra.KmapBuilder;
import booleanalgebra.Options;

import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static booleanalgebra.TermType.MIN_TERMS;


public class Main {
    public static void main(String[] args) {
        int n = 4;
        System.out.println(String.join(".", Collections.emptyList()));
        var f = IntStream.generate(() -> ThreadLocalRandom.current().nextInt(16));
        Kmap kmap = KmapBuilder.withNumberOfVariables(n)
                .andTermsAt(MIN_TERMS, 5,13,7,15,1,0,2,9,4)
                .build();
        System.out.println(kmap.toString(Options.values()));
        System.out.println("Solution:");
        System.out.println(kmap.minimize());
    }
}

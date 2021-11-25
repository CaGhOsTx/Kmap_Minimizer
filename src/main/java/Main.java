import booleanalgebra.Direction;
import booleanalgebra.Kmap;
import booleanalgebra.KmapBuilder;
import booleanalgebra.Options;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static booleanalgebra.TermType.MIN_TERM;


public class Main {
    public static void main(String[] args) {

        int n = 4;
        System.out.println(String.join(".", Collections.emptyList()));
        var f = IntStream.generate(() -> ThreadLocalRandom.current().nextInt(16));
        Kmap kmap = KmapBuilder.withNumberOfVariables(n)
                .andTermsAt(MIN_TERM, 0,1,2,3,4,5,6,7,8,9,11,12,13,14,15)
                .build();
        System.out.println(kmap.toString(Options.values()));
        System.out.println("Solution:");
        System.out.println(kmap.minimize());
    }
}

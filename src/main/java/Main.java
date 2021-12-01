import booleanalgebra.Direction;
import booleanalgebra.Kmap;
import booleanalgebra.KmapBuilder;
import booleanalgebra.Options;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static booleanalgebra.TermType.DONT_CARE;
import static booleanalgebra.TermType.MIN_TERM;


public class Main {
    public static void main(String[] args) {

        int n = 4;
        System.out.println(String.join(".", Collections.emptyList()));
        var f = IntStream.generate(() -> ThreadLocalRandom.current().nextInt(16));
        Kmap kmap = KmapBuilder.withVariables("A", "B", "sum", "Cin")
                .andTermsAt(MIN_TERM, 4,5,6)
                .andTermsAt(DONT_CARE, 0,1,2,3)
                .build();
        System.out.println(kmap.toString(Options.values()));
        System.out.println("Solution:");
        System.out.println(kmap.minimize());
    }
}

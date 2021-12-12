import booleanalgebra.KmapBuilder;
import booleanalgebra.Options;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static booleanalgebra.TermType.MIN_TERM;

public class Main {
    public static void main(String[] args) {
        Runtime r = Runtime.getRuntime();
        long memBef = r.totalMemory() - r.freeMemory();
        var halfAdder = KmapBuilder.withNumberOfVariables(20)
                .andTermsAt(MIN_TERM, IntStream.generate(() -> ThreadLocalRandom.current().nextInt(20000)).distinct().limit(1000).toArray())
                .build();
//        var adder = KmapBuilder.withVariables("A", "B", "Cin", "Sum", "Cout")
//                .andGrayCodeTerms(MIN_TERM, "00000", "00110", "01101", "01010", "11001", "11111", "10101", "10010")
//                .andTermsAt(DONT_CARE, 0,1,2,3,4)
//                .build();
        long memAft = r.totalMemory() - r.freeMemory();
        System.out.println((memAft - memBef) / 1_000_000f);

    }
}

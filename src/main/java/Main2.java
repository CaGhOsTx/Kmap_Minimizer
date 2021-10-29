import java.math.BigInteger;
import java.util.Optional;
import java.util.Scanner;
import static java.math.BigInteger.ONE;

public class Main2 {
    public static void main(String[] args) {
        BigInteger n = new BigInteger(new Scanner(System.in).next());
        Optional<BigInteger> lo = Optional.empty(), hi = Optional.empty();
        for(BigInteger l = n, r = n.add(ONE); lo.isEmpty() || hi.isEmpty(); l = l.subtract(ONE), r = r.add(ONE)) {
            if(lo.isEmpty() && l.isProbablePrime(50)) lo = Optional.of(l);
            if(hi.isEmpty() && l.isProbablePrime(50)) hi = Optional.of(r);
        }
        System.out.println(hi.orElseThrow().subtract(lo.orElseThrow()));
    }
}


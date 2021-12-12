package booleanalgebra;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class GrayCode implements Iterable<String>{
    private final Map<String, Integer> grayCodeIndexMap;
    private final Map<Integer, String> inverse;

    private GrayCode(int n) {
        var grayCode = grayCode(n);
        grayCodeIndexMap = new LinkedHashMap<>(grayCode.length);
        inverse = new LinkedHashMap<>(grayCode.length);
        for (int i = 0; i < grayCode.length; i++) {
            grayCodeIndexMap.put(grayCode[i], i);
            inverse.put(i, grayCode[i]);
        }
    }

    int size() {
        return grayCodeIndexMap.size();
    }

    static GrayCode of(int n) {
        if(n < 1)
            throw new IllegalStateException("Gray code must represent at least one variable!");
        return new GrayCode(n);
    }

    String get(int index) {
        return inverse.get(index);
    }

    int getIndex(String gc) {
        var index = grayCodeIndexMap.get(gc);
        if(index == null)
            throw new IllegalStateException("Invalid input format! Too many variables");
        return index;
    }

    private static String[] grayCode(int n) {
        if(n <= 0)
            throw new IllegalStateException("n cannot be non-positive");
        if(n > 1) {
            String[] arr = grayCode(--n), temp = new String[arr.length << 1];
            for (int i = 0, j = 0, length = arr.length; i < length; i++, j = j != length - 1 ? j + 1 : 0) {
                temp[i] = "0" + arr[j];
                temp[length + i] = "1" + arr[length - j - 1];
            }
            return temp;
        }
        return new String[] {"0", "1"};
    }

    private static int pow2(int val) {
        if(val > 0)
            return pow2(--val) << 1;
        return 1;
    }

    static int toDecimal(String s, int start) {
        int res = 0;
        for (int i = s.length() - 1, binary = pow2(start); i >= 0; i--, binary <<= 1)
            res += s.charAt(i) == '1' ? binary : 0;
        return res;
    }

    @Override
    public Iterator<String> iterator() {
        return grayCodeIndexMap.keySet().iterator();
    }

    @Override
    public String toString() {
        return String.join(" ", grayCodeIndexMap.keySet());
    }
}

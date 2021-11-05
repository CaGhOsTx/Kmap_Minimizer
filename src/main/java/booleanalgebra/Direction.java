package booleanalgebra;

import java.util.Iterator;
import java.util.function.ToIntBiFunction;

enum Direction implements Iterable<Direction>{
    UP((i, length) -> i == 0 ? length - 1 : i - 1),
    RIGHT((i, length) -> i == length - 1 ? 0 : i + 1),
    DOWN((i, length) -> i == length - 1 ? 0 : i + 1),
    LEFT((i, length) -> i == 0 ? length - 1 : i - 1);

    ToIntBiFunction<Integer, Integer> f;
    private static int currentIndex = 0;

    Direction(ToIntBiFunction<Integer, Integer> f) {
        this.f = f;
    }

    boolean isHorizontal() {
        return this.equals(RIGHT) || this.equals(LEFT);
    }

    int apply(int i, int length) {
        return f.applyAsInt(i, length);
    }

    @Override
    public Iterator<Direction> iterator() {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Direction next() {
                return values()[currentIndex == values().length ? 0 : currentIndex++];
            }
        };
    }
}

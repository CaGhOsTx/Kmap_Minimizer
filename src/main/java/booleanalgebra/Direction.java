package booleanalgebra;

import java.util.Iterator;
import java.util.function.ToIntBiFunction;

enum Direction implements Iterable<Direction>{
    UP((i, length) -> i == 0 ? length - 1 : i - 1, 0),
    RIGHT((i, length) -> i == length - 1 ? 0 : i + 1, 1),
    DOWN((i, length) -> i == length - 1 ? 0 : i + 1, 2),
    LEFT((i, length) -> i == 0 ? length - 1 : i - 1, 3);

    ToIntBiFunction<Integer, Integer> f;
    int index;

    Direction(ToIntBiFunction<Integer, Integer> f, int index) {
        this.f = f;
        this.index = index;
    }

    boolean isHorizontal() {
        return this.equals(RIGHT) || this.equals(LEFT);
    }

    int apply(int i, int length) {
        return f.applyAsInt(i, length);
    }

    @Override
    public Iterator<Direction> iterator() {
        int index = this.index;
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Direction next() {
                return values()[index == values().length - 1 ? 0 : index + 1];
            }
        };
    }
}

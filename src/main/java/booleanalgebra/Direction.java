package booleanalgebra;

import java.util.function.ToIntBiFunction;

enum Direction {
    UP((i, length) -> i == 0 ? length - 1 : i - 1),
    RIGHT((i, length) -> i == length - 1 ? 0 : i + 1),
    DOWN((i, length) -> i == length - 1 ? 0 : i + 1),
    LEFT((i, length) -> i == 0 ? length - 1 : i - 1);

    ToIntBiFunction<Integer, Integer> f;

    Direction(ToIntBiFunction<Integer, Integer> f) {
        this.f = f;
    }

    boolean isHorizontal() {
        return this.equals(RIGHT) || this.equals(LEFT);
    }

    int apply(int i, int length) {
        return f.applyAsInt(i, length);
    }
}

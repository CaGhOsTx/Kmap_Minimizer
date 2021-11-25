package booleanalgebra;

import java.util.function.ToIntBiFunction;

public enum Direction{
    UP((i, length) -> i == 0 ? length - 1 : i - 1, 0),
    RIGHT((i, length) -> i == length - 1 ? 0 : i + 1, 1),
    DOWN((i, length) -> i == length - 1 ? 0 : i + 1, 2),
    LEFT((i, length) -> i == 0 ? length - 1 : i - 1, 3);

    ToIntBiFunction<Integer, Integer> fIterator;
    int index;

    Direction(ToIntBiFunction<Integer, Integer> fIterator, int index) {
        this.fIterator = fIterator;
        this.index = index;
    }

    public Direction opposite() {
        return next().next();
    }

    public Direction previous() {
        return values()[index == 0 ? values().length - 1 : index - 1];
    }

    public Direction next() {
        return values()[index == values().length - 1 ? 0 : index + 1];
    }

    boolean isHorizontal() {
        return this == RIGHT || this == LEFT;
    }

    public void advance(int[] rcMatrix, int[] length) {
        if(isHorizontal())
            rcMatrix[1] = fIterator.applyAsInt(rcMatrix[1], length[1]);
        else
            rcMatrix[0] = fIterator.applyAsInt(rcMatrix[0], length[0]);
    }
}


package model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Move {

    public enum Action {LEFT, RIGHT, UP, DOWN};

    public final int fromIndex;
    public final Action action;
    private final Board fromBoard;

    public Move(Board fromBoard, Action action) {
        switch (action) {
            case RIGHT: fromIndex = fromBoard.blankIndex - 1; break;
            case LEFT: fromIndex = fromBoard.blankIndex + 1; break;
            case DOWN: fromIndex = fromBoard.blankIndex - fromBoard.size; break;
            case UP: fromIndex = fromBoard.blankIndex + fromBoard.size; break;
            default: fromIndex = -1;
        }
        this.action = action;
        this.fromBoard = fromBoard;
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        Move other = (Move) obj;
        return fromIndex == other.fromIndex && action == other.action;
    }

    @NonNull
    @Override
    public String toString() {
        int row  = fromBoard.getRow((fromIndex));
        int col = fromBoard.getCol(fromIndex);
        return "(" +col+ ", "+ row+") "+ action;
    }
}

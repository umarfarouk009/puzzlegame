package model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Board {

    public static final char BORDER = '|';
    public static final char BLANK = '0';

    public final String board;
    public final int size;
    public final int blankIndex;
    public final List<Move> validMoves;

    public Board(String boardString) {
        String[] rows = boardString.split(" ");
        StringBuilder result = new StringBuilder();
        result.append(BORDER).append(rows[0].replaceAll(".", String.valueOf(BORDER))).append(BORDER);
        for(String row: rows)
            result.append(BORDER).append(row).append(BORDER);
        result.append(BORDER).append(rows[0].replaceAll(".", String.valueOf(BORDER))).append(BORDER);
        board = result.toString();
        size = rows.length + 2;
        blankIndex = board.indexOf(BLANK);
        validMoves = getValidMoves();
    }

    public Board(Board oldBoard, Move move){
        char[] temp = oldBoard.board.toCharArray();
        temp[oldBoard.blankIndex] = oldBoard.board.charAt(move.fromIndex);
        temp[move.fromIndex] = oldBoard.board.charAt(oldBoard.blankIndex);
        board = new String(temp);
        size = oldBoard.size;
        blankIndex = move.fromIndex;
        validMoves = getValidMoves();
    }

    public List<Move> getValidMoves(){
        List<Move> moves = new ArrayList<Move>();
        if (board.charAt(blankIndex - 1) != BORDER) moves.add(new Move(this, Move.Action.RIGHT));
        if (board.charAt(blankIndex + 1) != BORDER) moves.add(new Move(this, Move.Action.LEFT));
        if (board.charAt(blankIndex - size) != BORDER) moves.add(new Move(this, Move.Action.DOWN));
        if (board.charAt(blankIndex + size) != BORDER) moves.add(new Move(this, Move.Action.UP));
        return moves;
    }

    public Move.Action getAction(int row, int col){
        int delta  = blankIndex - getIndex(row, col);
        if(delta == size) return Move.Action.DOWN;
        if(delta == -size) return Move.Action.UP;
        if(delta == 1) return Move.Action.RIGHT;
        if(delta == -1) return Move.Action.LEFT;
        return null;
    }

    public int getIndex(int row, int col) {
        return (row + 1) * size + col + 1;
    }

    public int getRow(int boardIndex) {
        return boardIndex / size - 1;
    }

    public int getCol(int boardIndex) {
        return boardIndex % size - 1;
    }

    public char getChar(int row, int col) {
        return board.charAt(getIndex(row, col));
    }

    public int getSize() {
        // square of board without borders
        return size - 2;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int res = 1;
        res = prime * res + ((board == null) ? 0 : board.hashCode());
        return res;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        assert obj != null;
        String o = ((Board) obj).board;
        return board.equals((o));
    }

    @NonNull
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int r = 0; r<getSize(); ++r){
            for (int c =0; c<getSize(); ++c){
                if(c == 0 && r != 0)
                    sb.append(' ');
                sb.append(getChar(r,c));
            }
        }
        return sb.toString();
    }
}

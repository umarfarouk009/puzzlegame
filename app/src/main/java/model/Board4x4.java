package model;

import java.util.ArrayList;
import java.util.List;


public class Board4x4 {

    public static final char BORDER = '|';
    public static final char BLANK = '0';

    public final String board_InString;
    public final int boardSize;
    public final int blankIndex;
    public final List<Move> validMoves;

    public Board4x4(String boardString) {
        String[] rows  = boardString.split("-");
        StringBuilder res = new StringBuilder();
        res.append(BORDER).append(rows[0].replaceAll(".", String.valueOf(BORDER))).append(BORDER);
        for(String row: rows)
            res.append(BORDER).append(row).append(BORDER);
        res.append(BORDER).append(rows[0].replaceAll(".", String.valueOf(BORDER))).append(BORDER);
        board_InString = res.toString();
        boardSize = rows.length + 2;
        blankIndex = board_InString.indexOf(BLANK);
        validMoves = getValidMoves();
    }

    public List<Move> getValidMoves(){
        List<Move> validMoves = new ArrayList<>();

        return validMoves;
    }
}

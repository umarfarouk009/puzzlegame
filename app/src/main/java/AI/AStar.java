package AI;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import model.Board;
import model.Move;
import model.Path;

public class AStar implements Solver{

    private final Board goal;
    private final Heuristic heuristic;

    public AStar(Board board, Heuristic heuristic) {
        this.goal = board;
        this.heuristic = heuristic;
    }

    private Map<Board, Move> successors(Board board) {
        Map<Board, Move> result = new HashMap<Board, Move>();
        for (Move nextMove : board.validMoves) {
            Board nextBoard = new Board(board, nextMove);
            result.put(nextBoard, nextMove);
        }
        return result;
    }

    @Override
    public Path shortestPath(Board start) {
        Set<Board> visited = new HashSet<Board>();
        Queue<Path> paths = new PriorityQueue<Path>();
        paths.add(new Path(start));

        while (!paths.isEmpty()) {
            Path path = paths.remove();
            Board last = path.getLastBoard();
            if (last.equals(goal)) return path;
            if (!visited.contains(last)) {
                visited.add(last);
                for (Map.Entry<Board, Move> successor : successors(last).entrySet()) {
                    Board nextBoard = successor.getKey();
                    Move nextMove = successor.getValue();
                    Path newPath = new Path(path).add(nextMove, nextBoard);
                    newPath.estTotalCost += heuristic.heuristic(nextBoard);
                    paths.add(newPath);
                }
            }
        }
        return null;
    }

    @Override
    public Board generateBoard() {
        Board current = goal;
        int nMoves = (int) (Math.random() * 1000);
        Set<Board> visited = new HashSet<Board>();
        for (int i = 0; i < nMoves; ++i) {
            visited.add(current);
            Set<Board> succ = successors(current).keySet();
            succ.removeAll(visited);
            Object[] nextBoards = succ.toArray();
            if (nextBoards.length == 0) return current;
            current = (Board) nextBoards[(int) (Math.random() * (nextBoards.length-1))];
        }
        return current;
    }

    @Override
    public List<Board> generateBoards(int numBoards) {
        List<Board> boards = new ArrayList<Board>();
        for (int i = 0; i < numBoards; ++i) {
            boards.add(generateBoard());
        }
        return boards;
    }
}

package AI;

import java.util.List;

import model.Board;
import model.Path;

public interface Solver {
    public Path shortestPath(Board start);
    public Board generateBoard();
    public List<Board> generateBoards(int numBoards);
}

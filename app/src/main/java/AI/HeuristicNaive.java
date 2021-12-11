package AI;

import model.Board;

public class HeuristicNaive extends Heuristic {

    public HeuristicNaive(Board goal) {
        super(goal);
    }

    @Override
    public int heuristic(Board current) {
        return 0;
    }

}

package model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Path implements Comparable<Path> {

    private final List<Object> path;
    private int pathCost;
    public int estTotalCost;


    public Path(Board start) {
        path = new ArrayList<Object>();
        path.add(start);
        pathCost = 0;
        estTotalCost = pathCost;
    }


    public Path(Path oldPath) {
        path = new ArrayList<Object>(oldPath.path);
        pathCost = oldPath.pathCost;
        estTotalCost = pathCost;
    }

    public Path add(Move nextMove, Board nextBoard){
        path.add(nextMove);
        path.add(nextBoard);
        ++pathCost;
        estTotalCost = pathCost;
        return this;
    }

    public Move removeFirstMove(){
        path.remove(0);
        Move first = (Move) path.remove(0);
        --pathCost;
        estTotalCost = pathCost;
        return first;
    }

    public int getNumOFMoves(){
        return pathCost;
    }

    public Board getLastBoard() {
        return (Board) path.get(path.size() - 1);
    }

    public int getNumOfMoves() {
        return pathCost;
    }

    @Override
    public int compareTo(Path o) {
        if(estTotalCost < o.estTotalCost) return -1;
        else if (estTotalCost > o.estTotalCost) return 1;
        else
            return 0;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        Path other = (Path) obj;
        return pathCost == other.pathCost && path.equals(other.path);
    }

    @NonNull
    @Override
    public String toString() {
        return "Cost ("+pathCost+", "+estTotalCost+") "+path;
    }
}

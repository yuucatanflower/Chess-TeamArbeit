package model;

import java.util.List;

public interface IMoveStrategy {
    List<Position> getMoves(Piece piece, Board board);
}

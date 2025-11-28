package model;

import java.util.List;

public class PawnMoveStrategy implements IMoveStrategy {
    @Override
    public List<Position> getMoves(Piece piece, Board board) {
        return List.of();
    }
}

package model;

import java.util.List;

public class KingMoveStrategy implements IMoveStrategy{
    @Override
    public List<Position> getMoves(Piece piece, Board board) {
        return List.of();
    }
}

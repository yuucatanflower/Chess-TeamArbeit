package model.strategies;

import model.Board;
import model.Piece;
import model.coreData.Position;

import java.util.List;

public class QueenMoveStrategy implements IMoveStrategy {
    @Override
    public List<Position> getMoves(Piece piece, Board board) {
        return List.of();
    }
}

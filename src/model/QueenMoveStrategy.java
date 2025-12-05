package model;

import java.util.ArrayList;
import java.util.List;

public class QueenMoveStrategy implements IMoveStrategy {

    @Override
    public List<Position> getMoves(Piece piece, Board board) {
        List<Position> validMoves = new ArrayList<>();

        // queen moves like rook + bishop
        RookMoveStrategy rookStrategy = new RookMoveStrategy();
        BishopMoveStrategy bishopStrategy = new BishopMoveStrategy();

        // rook moves
        validMoves.addAll(rookStrategy.getMoves(piece, board));

        // bishop moves
        validMoves.addAll(bishopStrategy.getMoves(piece, board));

        return validMoves;
    }
}
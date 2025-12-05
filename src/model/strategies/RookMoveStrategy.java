package model.strategies;

import model.*;
import model.coreData.Color;
import model.coreData.Position;

import java.util.ArrayList;
import java.util.List;

public class RookMoveStrategy implements IMoveStrategy {

    @Override
    public List<Position> getMoves(Piece piece, Board board) {
        List<Position> validMoves = new ArrayList<>();
        Position currentPos = piece.getPosition();
        Color currentColor = piece.getColor();

        int[][] rookMoves = {
                {0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5}, {0, 6}, {0, 7}, //right

                {0, -1}, {0, -2}, {0, -3}, {0, -4}, {0, -5}, {0, -6}, {0, -7}, //left

                {1, 0}, {2, 0}, {3, 0}, {4, 0}, {5, 0}, {6, 0}, {7, 0}, //down

                {-1, 0}, {-2, 0}, {-3, 0}, {-4, 0}, {-5, 0}, {-6, 0}, {-7, 0} //up
        };

        for (int[] move : rookMoves) {
            int newRow = currentPos.row() + move[0];
            int newCol = currentPos.col() + move[1];

            if (newRow < 0 || newRow > 7 || newCol < 0 || newCol > 7) {
                continue;
            } // if move goes outside board then skip

            Position targetPos = new Position(newRow, newCol);
            Piece targetPiece = board.getPieceAt(targetPos);

            if (targetPiece == null) {
                validMoves.add(targetPos);
            } else {
                if (targetPiece.getColor() != currentColor) {
                    validMoves.add(targetPos);
                }
            }
        }

        return validMoves;
    }
}



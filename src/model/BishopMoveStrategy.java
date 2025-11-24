package model;

import java.util.ArrayList;
import java.util.List;

public class BishopMoveStrategy implements IMoveStrategy {

    @Override
    public List<Position> getMoves(Piece piece, Board board) {
        List<Position> validMoves = new ArrayList<>();
        Position currentPos = piece.getPosition();
        Color currentColor = piece.getColor();

        // All possible moves for bishop
        // TODO maybe change to for loop (with use of direction)
        int[][] bishopMoves = {
                // ↗ (row -, col +)
                {-1, 1},
                {-2, 2},
                {-3, 3},
                {-4, 4},
                {-5, 5},
                {-6, 6},
                {-7, 7},

                // ↘ (row +, col +)
                {1, 1},
                {2, 2},
                {3, 3},
                {4, 4},
                {5, 5},
                {6, 6},
                {7, 7},

                // ↙ (row +, col -)
                {1, -1},
                {2, -2},
                {3, -3},
                {4, -4},
                {5, -5},
                {6, -6},
                {7, -7},

                // ↖ (row -, col -)
                {-1, -1},
                {-2, -2},
                {-3, -3},
                {-4, -4},
                {-5, -5},
                {-6, -6},
                {-7, -7}
        };

        for (int[] move : bishopMoves) {
            int newRow = currentPos.row() + move[0];
            int newCol = currentPos.col() + move[1];

            Position targetPos = new Position(newRow, newCol);

            // 1. Check if it's on the board
            if (newRow >= 0 && newRow <= 7 && newCol >= 0 && newCol <= 7) {

                // 2. Check the square's contents
                Piece targetPiece = board.getPieceAt(targetPos);

                if (targetPiece == null) {
                    // Square is empty, it's a valid move
                    validMoves.add(targetPos);
                } else {
                    // Square is occupied, check if it's an enemy
                    if (targetPiece.getColor() != currentColor) {
                        validMoves.add(targetPos); // Valid capture
                    }
                    // Can't move to a square occupied by a friendly piece
                }
            }
            // If it's off the board, just ignore it and check the next move
        }
        return validMoves;
    }
}

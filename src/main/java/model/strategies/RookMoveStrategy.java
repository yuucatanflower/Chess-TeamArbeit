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

        int[][] directions = {
                {-1, 0}, // Up
                {1, 0},  // Down
                {0, -1}, // Left
                {0, 1}   // Right
        };

        for (int[] move : directions) {
            int newRow = currentPos.row() + move[0];
            int newCol = currentPos.col() + move[1];

            // Check if it's on the board
            while (isOnTheBoard(newRow, newCol)) {
                Position targetPos = new Position(newRow, newCol);
                // Check the square's contents
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
                    break;
                }
                // Move one step further in the SAME direction
                newRow += move[0];
                newCol += move[1];
            }
        }
        return validMoves;
    }

    private boolean isOnTheBoard(int row, int col) {
        return row >= 0 && row <= 7 && col >= 0 && col <= 7;
    }
}



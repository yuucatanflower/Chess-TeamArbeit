package com.easteurope.chess.strategies;

import com.easteurope.chess.model.Board;
import com.easteurope.chess.model.Piece;
import com.easteurope.chess.model.coreData.Color;
import com.easteurope.chess.model.coreData.Position;

import java.util.ArrayList;
import java.util.List;

public class BishopMoveStrategy implements IMoveStrategy {

    @Override
    public List<Position> getMoves(Piece piece, Board board) {
        List<Position> validMoves = new ArrayList<>();
        Position currentPos = piece.getPosition();
        Color currentColor = piece.getColor();

        int[][] directions = {
                {-1, 1},  // Up-Right
                {1, 1},   // Down-Right
                {1, -1},  // Down-Left
                {-1, -1}  // Up-Left
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

    public boolean isOnTheBoard(int newRow, int newCol) {
        return newRow >= 0 && newRow <= 7 && newCol >= 0 && newCol <= 7;
    }
}

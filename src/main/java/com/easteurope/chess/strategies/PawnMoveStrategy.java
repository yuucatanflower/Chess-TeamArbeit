package com.easteurope.chess.strategies;

import com.easteurope.chess.model.Board;
import com.easteurope.chess.model.Piece;
import com.easteurope.chess.model.coreData.Color;
import com.easteurope.chess.model.coreData.Position;

import java.util.ArrayList;
import java.util.List;

public class PawnMoveStrategy implements IMoveStrategy {
    @Override
    public List<Position> getMoves(Piece piece, Board board) {
        List<Position> validMoves = new ArrayList<>();
        Position currentPos = piece.getPosition();
        Color currentColor = piece.getColor();
        int direction;
        int startingRow;


        // direction based on color and is pawn on starting row?
        if (currentColor == Color.WHITE) {
            // White
            direction = -1;
            startingRow = 6;
        } else {
            // Black
            direction = 1;
            startingRow = 1;
        }

        // move single step forward
        int currentRow = currentPos.row();
        int currentCol = currentPos.col();

        int newRow = currentRow + direction;
        Position newPos = new Position(newRow, currentCol);

        // check bounds -> add valid move
        if (isOnTheBoard(newRow, currentCol)) {
            if (board.getPieceAt(newPos) == null) {
                validMoves.add(newPos);

                // double step forward
                int doubleStepForward = currentRow + direction * 2;
                Position doubleStepForwardPos = new Position(doubleStepForward, currentCol);

                // is it on the starting row? Are the next 2 fields empty?
                if (startingRow == currentRow && board.getPieceAt(doubleStepForwardPos) == null && board.getPieceAt(newPos) == null) {
                    validMoves.add(new Position(doubleStepForward, currentCol));
                }
            }
        }

        // diagonal capture
        int[][] whiteCaptureDirections = {
                {-1, 1},  // Up-Right
                {-1, -1}  // Up-Left
        };
        int[][] blackCaptureDirections = {
                {1, 1},   // Down-Right
                {1, -1},  // Down-Left
        };

        int[][] currentCaptureDirections;
        if (currentColor == Color.WHITE) {
            currentCaptureDirections = whiteCaptureDirections;
        } else {
            currentCaptureDirections = blackCaptureDirections;
        }

        for (int[] move : currentCaptureDirections) {
            int newCaptureRow = currentPos.row() + move[0];
            int newCaptureCol = currentPos.col() + move[1];

            Position targetPos = new Position(newCaptureRow, newCaptureCol);

            if (isOnTheBoard(newCaptureRow, newCaptureCol)) {
                Piece targetPiece = board.getPieceAt(targetPos);

                if (targetPiece != null && targetPiece.getColor() != currentColor) {
                    validMoves.add(targetPos); // valid capture
                }
            }
        }
        return validMoves;
    }

    // check if it's on the board
    public boolean isOnTheBoard(int newRow, int newCol) {
        return newRow >= 0 && newRow <= 7 && newCol >= 0 && newCol <= 7;
    }
}

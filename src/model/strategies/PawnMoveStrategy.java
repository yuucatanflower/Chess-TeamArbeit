package model.strategies;

import model.Board;
import model.Piece;
import model.coreData.Color;
import model.coreData.Position;

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
        if (isOnTheBoard(newRow, currentCol)){
            if (board.getPieceAt(newPos) == null){
                validMoves.add(newPos);

                // TODO double step forward
            }
        }

        // TODO diagonal captures

        return validMoves;
    }

    // Check if it's on the board
    public boolean isOnTheBoard(int newRow, int newCol) {
        return newRow >= 0 && newRow <= 7 && newCol >= 0 && newCol <= 7;
    }

}

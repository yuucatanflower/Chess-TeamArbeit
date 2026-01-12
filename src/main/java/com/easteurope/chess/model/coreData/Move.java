package com.easteurope.chess.model.coreData;

import com.easteurope.chess.model.Piece;

public record Move(
        Piece movedPiece, //piece that makes a move
        Position from, // position from where it moves
        Position to, // position to which it moves
        Piece capturedPiece, // piece that is captured (if captured , null if not)
        boolean isFirstMove // was it the pieces first move?
) {
    public Move {
        if (movedPiece == null || from == null || to == null || from.equals(to)) {
            throw new IllegalArgumentException("Move cannot have null piece or positions, and must actually move.");
        }
    }

    @Override
    public String toString() {
        String separator = (capturedPiece == null) ? " - " : " x "; // if no pieces were captured puts " - " between positions ( e.g. e2-e4) , "x" for takes
        return movedPiece.getType() + " " + from.toAlgebraicNotation() + separator + to.toAlgebraicNotation();
    }
}

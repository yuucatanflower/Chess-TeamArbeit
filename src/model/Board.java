package model;

public class Board {
    Piece[][] grid;

    public Board() {
        this.grid = new Piece[8][8];
    }

    // ACCESSORS / HELPERS

    public Piece getPieceAt(Position pos) {
        // check if empty?

        return grid[pos.row()][pos.col()];
    } //returns piece at given position

    public void setPieceAt(Position pos, Piece piece) {
        grid[pos.row()][pos.col()] = piece;
    } // sets piece on a position using Position object

    public void setPieceAt(String algebraicNotation, Piece piece) {
       Position pos = Position.fromAlgebraicNotation(algebraicNotation);
       this.setPieceAt(pos, piece);
    } // sets piece on a position , overloaded using a string input with algebraic notation

    public boolean isValidPos(Position pos) {
        return pos.row() >= 0 && pos.row() < 8 && pos.col() >= 0 && pos.col() < 8;
    } // checks if the position is valid

    //TODO max \/ \/ \/

    //MOVE MANAGEMENT

    public Piece executeMove(Position from , Position to) {
        Piece piece = getPieceAt(from);
        Piece target = getPieceAt(to);

        setPieceAt(to, piece);
        setPieceAt(from, null); // update the grid

        if(piece !=null ){
            piece.internal_setPosition(to); // update pieces "memory"
            piece.internal_setHasMoved(true);
        }
        return target; // return captured piece so GameState (later) can save it in the Move record
    } // move execution method

    public void undoMove(Position from , Position to , Piece capturedPiece , boolean originalHasMoved) {
        Piece piece = getPieceAt(to); // our piece is currently at the "to" position , we want to get it back to from
        setPieceAt(from, piece); // return it to "from" spot

        if (piece != null) {
            piece.internal_setPosition(from);
            piece.internal_setHasMoved(originalHasMoved); // restore the pieces hasMoved state (e.g if it hasn't moved before the move we are undoing right now set it back to false if it was false before
        } // originalHasMoved is going to be handled in GameState as well , and will be stored in a Move object (Move's isFirstMove variable)

        setPieceAt(to, capturedPiece);
        if (capturedPiece != null) {
            capturedPiece.internal_setPosition(to); // ensure the "resurrected" piece knows where it is
        }
    }

}


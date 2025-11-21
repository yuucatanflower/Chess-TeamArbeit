package model;

public class Board {
    Piece[][] grid;

    public Board() {
        this.grid = new Piece[8][8];
    }

    public Piece getPieceAt(Position pos) {
        // check if empty?

        return grid[pos.row()][pos.col()];
    }

    public void setPieceAt(Position pos, Piece piece) {
        grid[pos.row()][pos.col()] = piece;
    }

    public void setPieceAt(String algebraicNotation, Piece piece) {
       Position pos = Position.fromAlgebraicNotation(algebraicNotation);
       this.setPieceAt(pos, piece);
    }

    //TODO rest of methods ( check Miro )

}


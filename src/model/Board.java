package model;

import model.coreData.Color;
import model.coreData.PieceType;
import model.coreData.Position;
import model.strategies.*;

public class Board {
    private Piece[][] grid;

    public Board() {
        this.grid = new Piece[8][8];
        initializeBoard();
    }

    // ACCESSORS / HELPERS

    public Piece getPieceAt(Position pos) {
        return grid[pos.row()][pos.col()];
    } //returns piece at given position

    public Piece getPieceAt(String algebraicNotation) {
        Position pos = Position.fromAlgebraicNotation(algebraicNotation);
        return grid[pos.row()][pos.col()];
    }

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

    //MOVE MANAGEMENT
    public Piece executeMove(Position from , Position to) {
        Piece movedPiece = getPieceAt(from);
        Piece target = getPieceAt(to);

        setPieceAt(to, movedPiece);
        setPieceAt(from, null); // update the grid

        if(movedPiece !=null ){
            movedPiece.internal_setPosition(to); // update pieces "memory"
            movedPiece.internal_setHasMoved(true);
        }
        return target; // return captured piece so GameState (later) can save it in the Move record
    } // move execution method

    public void undoMove(Position from , Position to , Piece capturedPiece , boolean originalHasMoved) {
        Piece piece = getPieceAt(to); // our piece is currently at the "to" position , we want to get it back to from
        setPieceAt(from, piece); // return it to "from" spot

        if (piece != null) {
            piece.internal_setPosition(from);
            piece.internal_setHasMoved(originalHasMoved); // restore the pieces hasMoved state (e.g. if it hasn't moved before the move we are undoing right now set it back to false if it was false before
        } // originalHasMoved is going to be handled in GameState as well , and will be stored in a Move object (Move's isFirstMove variable)

        setPieceAt(to, capturedPiece);
        if (capturedPiece != null) {
            capturedPiece.internal_setPosition(to); // ensure the "resurrected" piece knows where it is
        }
    }

    //board init
    public void initializeBoard(){
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                grid[row][col] = null;
            }
        }

        // Pawns
        for (int col = 0; col < 8; col++) {
            // Black pawns -> row 1
            grid[1][col] = new Piece(new Position(1, col), Color.BLACK, PieceType.PAWN, new PawnMoveStrategy());

            // White pawns -> row 6
            grid[6][col] = new Piece(new Position(6, col), Color.WHITE, PieceType.PAWN, new PawnMoveStrategy());
        }

        // Black Major Pieces -> row 0
        grid[0][0] = new Piece(new Position(0, 0), Color.BLACK, PieceType.ROOK, new RookMoveStrategy());
        grid[0][1] = new Piece(new Position(0, 1), Color.BLACK, PieceType.KNIGHT, new KnightMoveStrategy());
        grid[0][2] = new Piece(new Position(0, 2), Color.BLACK, PieceType.BISHOP, new BishopMoveStrategy());
        grid[0][3] = new Piece(new Position(0, 3), Color.BLACK, PieceType.QUEEN, new QueenMoveStrategy());
        grid[0][4] = new Piece(new Position(0, 4), Color.BLACK, PieceType.KING, new KingMoveStrategy());
        grid[0][5] = new Piece(new Position(0, 5), Color.BLACK, PieceType.BISHOP, new BishopMoveStrategy());
        grid[0][6] = new Piece(new Position(0, 6), Color.BLACK, PieceType.KNIGHT, new KnightMoveStrategy());
        grid[0][7] = new Piece(new Position(0, 7), Color.BLACK, PieceType.ROOK, new RookMoveStrategy());

        // White Major Pieces -> row 7
        grid[7][0] = new Piece(new Position(7, 0), Color.WHITE, PieceType.ROOK, new RookMoveStrategy());
        grid[7][1] = new Piece(new Position(7, 1), Color.WHITE, PieceType.KNIGHT, new KnightMoveStrategy());
        grid[7][2] = new Piece(new Position(7, 2), Color.WHITE, PieceType.BISHOP, new BishopMoveStrategy());
        grid[7][3] = new Piece(new Position(7, 3), Color.WHITE, PieceType.QUEEN, new QueenMoveStrategy());
        grid[7][4] = new Piece(new Position(7, 4), Color.WHITE, PieceType.KING, new KingMoveStrategy());
        grid[7][5] = new Piece(new Position(7, 5), Color.WHITE, PieceType.BISHOP, new BishopMoveStrategy());
        grid[7][6] = new Piece(new Position(7, 6), Color.WHITE, PieceType.KNIGHT, new KnightMoveStrategy());
        grid[7][7] = new Piece(new Position(7, 7), Color.WHITE, PieceType.ROOK, new RookMoveStrategy());
    }

    public void printBoard() {
        System.out.println("\n  +------------------------+");

        // for loop rows
        for (int row = 0; row < 8; row++) {
            System.out.print((8 - row) + " | ");

            // for loop columns
            for (int col = 0; col < 8; col++) {
                Piece piece = grid[row][col];

                if (piece == null) {
                    System.out.print(". ");
                } else {
                    char symbol = 0;

                    switch (piece.getType()) {

                        case PAWN:
                            if (piece.getColor() == Color.BLACK) {
                                symbol = '♙';
                            }else{
                                symbol = '♟';
                            }
                            break;

                        case ROOK:
                            if (piece.getColor() == Color.BLACK) {
                                symbol = '♖';
                            }else{
                                symbol = '♜';
                            }
                            break;

                        case KNIGHT:
                            if (piece.getColor() == Color.BLACK) {
                                symbol = '♘';
                            }else{
                                symbol = '♞';
                            }
                            break;

                        case BISHOP:
                            if (piece.getColor() == Color.BLACK) {
                                symbol = '♗';
                            }else{
                                symbol = '♝';
                            }
                            break;

                        case QUEEN:
                            if (piece.getColor() == Color.BLACK) {
                                symbol = '♕';
                            }else{
                                symbol = '♛';
                            }
                            break;

                        case KING:
                            if (piece.getColor() == Color.BLACK) {
                                symbol = '♔';
                            }else{
                                symbol = '♚';
                            }
                            break;


                    }

                    System.out.print(symbol + " ");
                }
            }
            System.out.println("|");
        }
        System.out.println("  +------------------------+");
        System.out.println("    a b c d e f g h\n");


    }
}


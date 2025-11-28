package model;

public class Board {
    private Piece[][] grid;

    public Board() {
        this.grid = new Piece[8][8];
        initializeBoard();
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

}


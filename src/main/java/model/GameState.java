package model;

import model.coreData.Color;
import model.coreData.Move;
import model.coreData.PieceType;
import model.coreData.Position;
import model.strategies.QueenMoveStrategy;

import java.util.List;
import java.util.Stack;

public class GameState {

    // --- The State Data ---
    private final Board board;
    private Color currentTurn;
    private final Stack<Move> moveHistory;
    private boolean isGameOver;
    private String statusMessage;

    // ---  Constructor ---
    public GameState() {
        this.board = new Board();
        this.board.initializeBoard();

        this.currentTurn = Color.WHITE;
        this.moveHistory = new Stack<>();
        this.isGameOver = false;
        this.statusMessage = "WHITE to move";
    }

    // --- Getters ---
    public Board getBoard() {
        return board;
    }

    public Color getCurrentTurn() {
        return currentTurn;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    // --- Gameplay Methods ---

    //attempts to play a move from point A to point B.
    //return true if the move was successful, false if rejected.
    public boolean playTurn(Position from, Position to) {
        if (isGameOver) {
            statusMessage = "Game Over!";
            return false;
        }

        Piece piece = board.getPieceAt(from);

        // --- CASTLING HANDLING ---
        if (piece != null && piece.getType() == PieceType.KING) {

            int row = (currentTurn == Color.WHITE) ? 7 : 0;

            // Kingside castling (e1 -> g1 or e8 -> g8)
            if (from.equals(new Position(row, 4)) && to.equals(new Position(row, 6)) && canCastle(currentTurn, true)) {

                executeCastling(currentTurn, true);
                switchTurn();
                updateGameStatus();
                return true;
            }

            // Queenside castling (e1 -> c1 or e8 -> c8)
            if (from.equals(new Position(row, 4)) && to.equals(new Position(row, 2)) && canCastle(currentTurn, false)) {

                executeCastling(currentTurn, false);
                switchTurn();
                updateGameStatus();
                return true;
            }
        }

        //basic validation of piece and color
        if (piece == null) {
            statusMessage = "Piece not selected!";
            return false;
        }

        if (piece.getColor() != currentTurn) {
            statusMessage = "It is " + currentTurn + "'s turn!";
            return false;
        }

        List<Position> validMoves = piece.getValidMoves(board);
        if (!validMoves.contains(to)) {
            statusMessage = "Invalid move for " + piece.getType() + "!";
            return false;
        }

        boolean isFirstMove = !piece.hasMoved();
        Piece target = board.getPieceAt(to); // piece we MIGHT capture ( if not null or same color )

        board.executeMove(from, to);

        if (piece.getType() == PieceType.PAWN) {
            boolean whitePromote = (piece.getColor() == Color.WHITE && to.row() == 0); // last row for white
            boolean blackPromote = (piece.getColor() == Color.BLACK && to.row() == 7); // last row for black

            if (whitePromote || blackPromote) {
                System.out.println("Promoting Pawn to Queen!");
                Piece queen = new Piece(to, piece.getColor(), PieceType.QUEEN, new QueenMoveStrategy());
                board.setPieceAt(to, queen);
            }
        }

        Move moveRecord = new Move(piece, from, to, target, isFirstMove);
        moveHistory.push(moveRecord);

        switchTurn();
        updateGameStatus();
        return true;
    }

    // reverts the last move (from moveHistory)
    public void undo() {
        if (moveHistory.isEmpty()) {
            statusMessage = "No moves to undo!";
            return;
        }

        Move lastMove = moveHistory.pop();// gets last move

        //check if promotion happened
        Piece currentPiece = board.getPieceAt(lastMove.to());
        if (lastMove.movedPiece().getType() == PieceType.PAWN &&
                currentPiece != null &&
                currentPiece.getType() != PieceType.PAWN) {

            // we put the original Pawn back on the board manually
            // so board.undoMove() finds the right piece to move back.
            board.setPieceAt(lastMove.to(), lastMove.movedPiece());
        }


        board.undoMove(
                lastMove.from(),
                lastMove.to(),
                lastMove.capturedPiece(),
                lastMove.isFirstMove()
        ); // undoes last move

        switchTurn();
        statusMessage = "Undo successful! " + currentTurn + " to move.";
    }
    // --- Helper Methods ---

    private boolean isMoveLegal(Position from, Position to) {
        if (from == null || to == null) {
            return false;
        }

        Piece currentPiece = board.getPieceAt(from);

        if (currentPiece == null) {
            return false;
        }
        // ensure piece belongs to the current player
        if (currentPiece.getColor() != currentTurn) {
            return false;
        }

        // ensure the move is physically possible for this piece
        List<Position> allowedMoves = currentPiece.getValidMoves(board);
        if (!allowedMoves.contains(to)) {
            return false;
        }

        Board simulatedBoard = board.copy();
        Piece pieceOnSimulatedBoard = simulatedBoard.getPieceAt(from);

        simulatedBoard.setPieceAt(to, pieceOnSimulatedBoard);
        simulatedBoard.setPieceAt(from, null);

        if (isInCheck(currentTurn, simulatedBoard)) {
            return false; // king would be in danger -> illegal
        }
        return true;
    }

    private Position findKing(Color currentColor, Board boardToCheck) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position currentPos = new Position(row, col);
                Piece currentPiece = boardToCheck.getPieceAt(currentPos);
                // is in currentPiece a Piece? is currentPiece a King with the right Color?
                if (currentPiece != null && currentPiece.getType() == PieceType.KING && currentPiece.getColor() == currentColor) {
                    return currentPos;
                }
            }
        }
        return null;
    }

    private Position findKing(Color currentColor) {
        return findKing(currentColor, this.board);
    }

    private void switchTurn() {
        currentTurn = (currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;
        statusMessage = currentTurn + " to move";
    }

    private boolean isInCheck(Color color, Board boardToCheck) {
        Position kingPos = findKing(color, boardToCheck);

        if (kingPos == null) {
            throw new IllegalStateException("King not found");
        }

        Color opponent = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = new Position(row, col);
                Piece piece = boardToCheck.getPieceAt(pos);

                if (piece != null && piece.getColor() == opponent) {
                    List<Position> moves = piece.getValidMoves(boardToCheck);
                    if (moves.contains(kingPos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isInCheck(Color color) {
        return isInCheck(color, this.board);
    }

    private void updateGameStatus() {

        boolean inCheck = isInCheck(currentTurn);
        boolean hasLegalMove = false;

        // Loop through all pieces of the current player
        for (int row = 0; row < 8 && !hasLegalMove; row++) {
            for (int col = 0; col < 8 && !hasLegalMove; col++) {

                Position from = new Position(row, col);
                Piece piece = board.getPieceAt(from);

                if (piece == null || piece.getColor() != currentTurn) {
                    continue;
                }

                // Get all pseudo-legal moves for this piece
                List<Position> moves = piece.getValidMoves(board);

                for (Position to : moves) {

                    Piece captured = board.getPieceAt(to);
                    boolean wasFirstMove = !piece.hasMoved();

                    // Simulate move
                    board.executeMove(from, to);

                    // Check if king is safe after this move
                    boolean stillInCheck = isInCheck(currentTurn);

                    // Undo move
                    board.undoMove(from, to, captured, wasFirstMove);

                    // If there exists at least one move that removes check
                    if (!stillInCheck) {
                        hasLegalMove = true;
                        break;
                    }
                }
            }
        }

        // Determine game state
        if (inCheck && !hasLegalMove) {
            isGameOver = true;
            statusMessage = "Checkmate! " +
                    (currentTurn == Color.WHITE ? "Black" : "White") + " wins!";
        } else if (!inCheck && !hasLegalMove) {
            isGameOver = true;
            statusMessage = "Stalemate! Draw.";
        } else if (inCheck) {
            statusMessage = currentTurn + " is in check!";
        } else {
            statusMessage = currentTurn + " to move";
        }
    }

    private boolean canCastle(Color color, boolean kingSide) {

        Position kingPos = findKing(color);
        if (kingPos == null) return false;

        Piece king = board.getPieceAt(kingPos);
        if (king.hasMoved()) return false;

        int row = kingPos.row();

        // Determine rook position and path squares
        Position rookPos;
        Position[] path;

        if (kingSide) {
            // Kingside: e1 -> g1, rook h1 -> f1
            rookPos = new Position(row, 7);
            path = new Position[]{
                    new Position(row, 5),
                    new Position(row, 6)
            };
        } else {
            // Queenside: e1 -> c1, rook a1 -> d1
            rookPos = new Position(row, 0);
            path = new Position[]{
                    new Position(row, 3),
                    new Position(row, 2),
                    new Position(row, 1)
            };
        }

        Piece rook = board.getPieceAt(rookPos);
        if (rook == null ||
                rook.getType() != PieceType.ROOK ||
                rook.hasMoved()) {
            return false;
        }

        // Squares between king and rook must be empty
        for (Position p : path) {
            if (board.getPieceAt(p) != null) return false;
        }

        // King must not be in check, pass through check, or end in check
        if (isInCheck(color)) return false;

        for (int i = 0; i < 2; i++) { // only squares king passes through
            Position step = path[i];

            boolean wasFirstMove = !king.hasMoved();

            board.executeMove(kingPos, step);
            boolean inCheck = isInCheck(color);
            board.undoMove(kingPos, step, null, wasFirstMove);

            if (inCheck) return false;
        }
        return true;
    }


    private void executeCastling(Color color, boolean kingSide) {
        int row = (color == Color.WHITE) ? 7 : 0;

        Position kingFrom = new Position(row, 4);
        Position kingTo = kingSide
                ? new Position(row, 6)
                : new Position(row, 2);

        Position rookFrom = kingSide
                ? new Position(row, 7)
                : new Position(row, 0);

        Position rookTo = kingSide
                ? new Position(row, 5)
                : new Position(row, 3);

        board.executeMove(kingFrom, kingTo);
        board.executeMove(rookFrom, rookTo);
    }

    private boolean hasNoLegalMoves(Color color) {

        // check every piece
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                Position from = new Position(row, col);
                Piece piece = board.getPieceAt(from);

                if (piece == null) continue;

                if (piece.getColor() != color) continue;

                List<Position> moves = piece.getValidMoves(board);

                // try every possible move
                for (Position to : moves) {

                    Piece captured = board.getPieceAt(to);
                    boolean wasFirstMove = !piece.hasMoved();

                    // simulate move
                    board.executeMove(from, to);

                    boolean kingStillInCheck = isInCheck(color);

                    board.undoMove(from, to, captured, wasFirstMove);

                    // If move keeps king safe, player has a legal move
                    if (!kingStillInCheck) {
                        return false; // not stuck
                    }
                }
            }
        }

        // If no legal move was found, player is stuck (no legal moves)
        return true;
    }
}

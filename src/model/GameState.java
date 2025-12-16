package model;

import model.coreData.Color;
import model.coreData.Move;
import model.coreData.PieceType;
import model.coreData.Position;

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
    public Board getBoard() { return board; }
    public Color getCurrentTurn() { return currentTurn; }
    public String getStatusMessage() { return statusMessage; }
    public boolean isGameOver() { return isGameOver; }

    // --- Gameplay Methods ---

    //attempts to play a move from point A to point B.
    //return true if the move was successful, false if rejected.
    public boolean playTurn(Position from, Position to) {
        if (isGameOver) {
            statusMessage = "Game Over!";
            return false;
        }

        Piece piece = board.getPieceAt(from);

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

        Move moveRecord = new Move(piece, from, to, target, isFirstMove);
        moveHistory.push(moveRecord);

        switchTurn();
        return true;
    }

    // reverts the last move (from moveHistory)
    public void undo() {
        if (moveHistory.isEmpty()) {
            statusMessage = "No moves to undo!";
            return;
        }

        Move lastMove = moveHistory.pop(); // gets last move
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

    private Position findKing(Color currentColor){
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position currentPos = new Position(row, col);
                Piece currentPiece = board.getPieceAt(currentPos);
                // is in currentPiece a Piece? is currentPiece a King with the right Color?
                if (currentPiece != null && currentPiece.getType() == PieceType.KING && currentPiece.getColor() == currentColor){
                    return currentPos;
                }
            }
        }
        return null;
    }

    private void switchTurn() {
        currentTurn = (currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;
        statusMessage = currentTurn + " to move";
    }

    private boolean IsInCheck(Color color) {

        Position kingPos = findKing(color);
        if (kingPos == null) {
            throw new IllegalStateException("King not found");
        }

        Color opponent = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = new Position(row, col);
                Piece piece = board.getPieceAt(pos);

                if (piece != null && piece.getColor() == opponent) {
                    List<Position> moves = piece.getValidMoves(board);
                    if (moves.contains(kingPos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private void updateGameStatus() {

        boolean inCheck = IsInCheck(currentTurn);
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
                    boolean stillInCheck = IsInCheck(currentTurn);

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
        }
        else if (!inCheck && !hasLegalMove) {
            isGameOver = true;
            statusMessage = "Stalemate! Draw.";
        }
        else if (inCheck) {
            statusMessage = currentTurn + " is in check!";
        }
        else {
            statusMessage = currentTurn + " to move";
        }
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

                    boolean kingStillInCheck = IsInCheck(color);

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

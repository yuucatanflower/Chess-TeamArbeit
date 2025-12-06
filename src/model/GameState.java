package model;

import model.coreData.Color;
import model.coreData.Move;
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

    private void switchTurn() {
        currentTurn = (currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;
        statusMessage = currentTurn + " to move";
    }

}

package model;

import model.coreData.Color;
import model.coreData.Move;

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
        this.statusMessage = "White to move";
    }

}

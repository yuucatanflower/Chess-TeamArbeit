package model;

import java.util.ArrayList;
import java.util.List;

public class Piece {

    private Position position;
    private final Color color;
    private final PieceType type;
    private final IMoveStrategy moveStrategy;
    private boolean hasMoved;

    public Piece(Position position, Color color, PieceType type, IMoveStrategy moveStrategy) {
            this.position = position;
            this.color = color;
            this.type = type;
            this.moveStrategy = moveStrategy;
            this.hasMoved = false;
    }

    // GETTERS
    public Position getPosition() {
        return position;
    }

    public Color getColor() {
        return color;
    }

    public PieceType getType() {
        return type;
    }

    public boolean hasMoved() {
        return hasMoved;
    }


    // CORE LOGIC
    public void moveTo(Position newPosition) {
        this.position = newPosition;
        this.hasMoved = true;
    }

    public List<Position> getValidMoves(Board board) {
        return moveStrategy.getMoves(this, board);
    }

    //TODO max \/ \/ \/

    // STATE CHANGERS (FOR BOARD CLASS)
    void internal_setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    } //move without forcing hasMoved to true , only called by Board undoMove

    void internal_setPosition(Position position) {
        this.position = position;
    } // needed for board undoMove (hope it makes sense)
}


package model;

import java.util.ArrayList;
import java.util.List;

public class Piece {

    private Position position;
    private Color color;
    private PieceType type;
    private IMoveStrategy moveStrategy;
    private boolean hasMoved;

    public Piece(Position position, Color color, PieceType type, IMoveStrategy moveStrategy) {
            this.position = position;
            this.color = color;
            this.type = type;
            this.moveStrategy = moveStrategy;
            this.hasMoved = false;
    }

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

    public void moveTo(Position newPosition) {
        this.position = newPosition;
        this.hasMoved = true;
    }

    public List<Position> getValidMoves(Board board) {
        List<Position> validMoves = new ArrayList<>();

        validMoves.addAll(this.moveStrategy.getMoves(this, board));

        return validMoves;
    }

    //TODO rest of methods ( check Miro )
}


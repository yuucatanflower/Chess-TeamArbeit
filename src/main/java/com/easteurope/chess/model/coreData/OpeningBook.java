package com.easteurope.chess.model.coreData;

public class OpeningBook {
    //TODO later for bots
    //standard position
    public static final String START_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    // useful for testing empty board logic
    public static final String EMPTY_BOARD = "8/8/8/8/8/8/8/8 w - - 0 1";

    public static char getPieceChar(PieceType type, Color color) {
        char c = switch (type) {
            case PAWN -> 'P';
            case ROOK -> 'R';
            case KNIGHT -> 'N';
            case BISHOP -> 'B';
            case QUEEN -> 'Q';
            case KING -> 'K';
        };
        return (color == Color.BLACK) ? Character.toLowerCase(c) : c;
    }

    // prevent class instantiation
    private OpeningBook() {}
}

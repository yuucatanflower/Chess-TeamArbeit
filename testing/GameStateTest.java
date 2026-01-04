package model;

import model.coreData.Color;
import model.coreData.Position;
import model.coreData.PieceType;

public class GameStateTest {

    public static void main(String[] args) {

        GameState gs = new GameState();

        System.out.println("===== GameState Test =====");

        // Check kings exist and positions are correct
        Position whiteKingStart = new Position(7, 4);
        Position blackKingStart = new Position(0, 4);

        boolean whiteKingCorrect =
                gs.getBoard().getPieceAt(whiteKingStart) != null &&
                        gs.getBoard().getPieceAt(whiteKingStart).getColor() == Color.WHITE;

        boolean blackKingCorrect =
                gs.getBoard().getPieceAt(blackKingStart) != null &&
                        gs.getBoard().getPieceAt(blackKingStart).getColor() == Color.BLACK;

        System.out.println("White King correct? " + whiteKingCorrect);
        System.out.println("Black King correct? " + blackKingCorrect);

        // 2) White pawn move
        Position pawnFrom = new Position(6, 4);
        Position pawnTo = new Position(5, 4);

        boolean moveSuccess = gs.playTurn(pawnFrom, pawnTo);
        System.out.println("White pawn move success? " + moveSuccess);

        // 3) Undo
        gs.undo();
        boolean undoCorrect =
                gs.getBoard().getPieceAt(pawnFrom) != null &&
                        gs.getBoard().getPieceAt(pawnTo) == null;

        System.out.println("Undo correct? " + undoCorrect);

        System.out.println("===== End of Test =====");
        System.out.println();



        System.out.println("===== isInCheck Test =====");

        GameState gs2 = new GameState();
        Board b2 = gs2.getBoard();

        // Board empty
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                b2.setPieceAt(new Position(row, col), null);
            }
        }

        // White King
        Position kingPos = new Position(4, 4);
        b2.setPieceAt(kingPos, new Piece(
                kingPos, Color.WHITE, PieceType.KING,
                new model.strategies.KingMoveStrategy()
        ));

        // Black Rook gives check
        Position rookPos = new Position(4, 0);
        b2.setPieceAt(rookPos, new Piece(
                rookPos, Color.BLACK, PieceType.ROOK,
                new model.strategies.RookMoveStrategy()
        ));

        boolean check1 = callIsInCheck(gs2, Color.WHITE);
        System.out.println("White in check? expected TRUE → " + check1);

        // Block
        Position blockPos = new Position(4, 2);
        b2.setPieceAt(blockPos, new Piece(
                blockPos, Color.WHITE, PieceType.PAWN,
                new model.strategies.PawnMoveStrategy()
        ));

        boolean check2 = callIsInCheck(gs2, Color.WHITE);
        System.out.println("White in check after blocking? expected FALSE → " + check2);

        System.out.println("===== End isInCheck Test =====");
        System.out.println();


        // hasNoLegalMoves TEST


        System.out.println("===== hasNoLegalMoves Test =====");

        GameState gs3 = new GameState();
        Board b3 = gs3.getBoard();


        // Board leeren
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                b3.setPieceAt(new Position(r, c), null);
            }
        }

// Weißer König (a1)
        Position wk3 = new Position(7, 0);
        b3.setPieceAt(wk3, new Piece(
                wk3, Color.WHITE, PieceType.KING,
                new model.strategies.KingMoveStrategy()
        ));

// Schwarzer König (c3)
        Position bk3 = new Position(5, 2);
        b3.setPieceAt(bk3, new Piece(
                bk3, Color.BLACK, PieceType.KING,
                new model.strategies.KingMoveStrategy()
        ));

// Schwarze Dame (b2)
        Position q3 = new Position(6, 1);
        b3.setPieceAt(q3, new Piece(
                q3, Color.BLACK, PieceType.QUEEN,
                new model.strategies.QueenMoveStrategy()
        ));

        boolean noMoves = callHasNoLegalMoves(gs3, Color.WHITE);
        System.out.println("White has no legal moves? expected TRUE → " + noMoves);

        System.out.println("===== End hasNoLegalMoves Test =====");
        System.out.println();

    }


    // Hilfsmethode für private isInCheck()
    private static boolean callIsInCheck(GameState gs, Color color) {
        try {
            var method = GameState.class.getDeclaredMethod("isInCheck", Color.class);
            method.setAccessible(true);
            return (boolean) method.invoke(gs, color);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Hilfsmethode für private hasNoLegalMoves()
    private static boolean callHasNoLegalMoves(GameState gs, Color color) {
        try {
            var m = GameState.class.getDeclaredMethod("hasNoLegalMoves", Color.class);
            m.setAccessible(true);
            return (boolean) m.invoke(gs, color);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}



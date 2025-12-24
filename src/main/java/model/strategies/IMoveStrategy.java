package model.strategies;

import model.Board;
import model.Piece;
import model.coreData.Position;

import java.util.List;

public interface IMoveStrategy {
    List<Position> getMoves(Piece piece, Board board);
}

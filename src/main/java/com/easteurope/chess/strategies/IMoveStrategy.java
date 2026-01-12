package com.easteurope.chess.strategies;

import com.easteurope.chess.model.Board;
import com.easteurope.chess.model.Piece;
import com.easteurope.chess.model.coreData.Position;

import java.util.List;

public interface IMoveStrategy {
    List<Position> getMoves(Piece piece, Board board);
}

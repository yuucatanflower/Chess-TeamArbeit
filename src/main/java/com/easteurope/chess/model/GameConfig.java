package com.easteurope.chess.model;

import com.easteurope.chess.model.coreData.Color;

/**
 * A simple data carrier that holds the settings chosen by the user
 * (such as time limits, player color, and bot difficulty).
 * * This object is created in the Setup screen and passed to the Game screen
 * so the game knows how to start.
 */

public record GameConfig(
        long timeMs,
        long incrementMs,
        Color playerColor,
        int botLevel
) {}
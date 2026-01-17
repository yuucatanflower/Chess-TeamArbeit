package com.easteurope.chess.view;

import javafx.scene.image.*;
import javafx.scene.paint.Color;
import com.easteurope.chess.model.coreData.PieceType;

public class ImageLoader {
    private static final Image SPRITE_SHEET = new Image(ImageLoader.class.getResourceAsStream("/NullTale_Chess1.png")); //1 for purple 2 for blue
    private static final int GRID_SIZE = 16;

    // Scales 1 original pixel to a 5x5 block (e.g., 12px width becomes 60px).
    private static final int SCALE_FACTOR = 5;

    /**
     * Manually upscales the image region to ensure a sharp "pixel-art" look (Nearest Neighbor).
     * This avoids the blurriness caused by standard ImageView scaling.
     */
    private static ImageView createResampledImage(int sourceX, int sourceY, int width, int height) {
        int targetWidth = width * SCALE_FACTOR;
        int targetHeight = height * SCALE_FACTOR;
        WritableImage outputImage = new WritableImage(targetWidth, targetHeight);

        PixelReader reader = SPRITE_SHEET.getPixelReader();
        PixelWriter writer = outputImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(sourceX + x, sourceY + y);

                if (color.getOpacity() == 0) continue;

                // Write the single source pixel as a 5x5 block on the target
                for (int dy = 0; dy < SCALE_FACTOR; dy++) {
                    for (int dx = 0; dx < SCALE_FACTOR; dx++) {
                        writer.setColor((x * SCALE_FACTOR) + dx, (y * SCALE_FACTOR) + dy, color);
                    }
                }
            }
        }

        return new ImageView(outputImage);
    }

    // --- PIECES ---
    public static ImageView getPieceSprite(PieceType type, com.easteurope.chess.model.coreData.Color color) {
        int col = switch (type) {
            case PAWN -> 1; case ROOK -> 2; case KNIGHT -> 3;
            case BISHOP -> 4; case QUEEN -> 5; case KING -> 6;
        };

        // Determine row based on color
        int baseRow = (color == com.easteurope.chess.model.coreData.Color.BLACK) ? 12 : 14;

        // Calculate specific coordinates: 12x30px crop, +2px margin, starts 1 row higher
        int sourceX = (col * GRID_SIZE) + 2;
        int sourceY = ((baseRow - 1) * GRID_SIZE) + 1;

        int width = 12;
        int height = 30;

        return createResampledImage(sourceX, sourceY, width, height);
    }

    // --- TILES ---
    public static ImageView getBoardTile(boolean isLightSquare) {
        int row = 10;
        int col = isLightSquare ? 6 : 4;

        // Calculate specific coordinates: 12x12px crop, +2px margin
        int sourceX = (col * GRID_SIZE) + 2;
        int sourceY = (row * GRID_SIZE) + 2;

        int width = 12;
        int height = 12;

        return createResampledImage(sourceX, sourceY, width, height);
    }
}
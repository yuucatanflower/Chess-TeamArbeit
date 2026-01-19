package com.easteurope.chess.view;

import javafx.scene.image.*;
import javafx.scene.paint.Color;
import com.easteurope.chess.model.coreData.PieceType;

public class ImageLoader {
    // Default image, changed dynamically
    private static Image SPRITE_SHEET = new Image(ImageLoader.class.getResourceAsStream("/NullTale_Chess1.png"));
    private static final int GRID_SIZE = 16;
    private static final int SCALE_FACTOR = 5;

    public static void loadTheme(int themeId) {
        String path = switch (themeId) {
            case 2 -> "/NullTale_Chess.png";  // Red Theme
            case 3 -> "/NullTale_Chess2.png"; // Blue Theme
            case 4 -> "/NullTale_Chess3.png"; // NEW: Zombie Theme
            default -> "/NullTale_Chess1.png"; // Original Purple Theme
        };

        try {
            SPRITE_SHEET = new Image(ImageLoader.class.getResourceAsStream(path));
        } catch (Exception e) {
            System.err.println("Could not load theme image: " + path);
            SPRITE_SHEET = new Image(ImageLoader.class.getResourceAsStream("/NullTale_Chess1.png"));
        }
    }

    private static ImageView createResampledImage(int sourceX, int sourceY, int width, int height) {
        int targetWidth = width * SCALE_FACTOR;
        int targetHeight = height * SCALE_FACTOR;
        WritableImage outputImage = new WritableImage(targetWidth, targetHeight);

        PixelReader reader = SPRITE_SHEET.getPixelReader();
        PixelWriter writer = outputImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Safety check for bounds
                if (sourceX + x >= SPRITE_SHEET.getWidth() || sourceY + y >= SPRITE_SHEET.getHeight()) continue;

                Color color = reader.getColor(sourceX + x, sourceY + y);

                if (color.getOpacity() == 0) continue;

                for (int dy = 0; dy < SCALE_FACTOR; dy++) {
                    for (int dx = 0; dx < SCALE_FACTOR; dx++) {
                        writer.setColor((x * SCALE_FACTOR) + dx, (y * SCALE_FACTOR) + dy, color);
                    }
                }
            }
        }

        return new ImageView(outputImage);
    }

    public static ImageView getPieceSprite(PieceType type, com.easteurope.chess.model.coreData.Color color) {
        int col = switch (type) {
            case PAWN -> 1; case ROOK -> 2; case KNIGHT -> 3;
            case BISHOP -> 4; case QUEEN -> 5; case KING -> 6;
        };

        int baseRow = (color == com.easteurope.chess.model.coreData.Color.BLACK) ? 12 : 14;
        int sourceX = (col * GRID_SIZE) + 2;
        int sourceY = ((baseRow - 1) * GRID_SIZE) + 1;

        return createResampledImage(sourceX, sourceY, 12, 30);
    }

    public static ImageView getBoardTile(boolean isLightSquare) {
        int row = 10;
        int col = isLightSquare ? 6 : 4;
        int sourceX = (col * GRID_SIZE) + 2;
        int sourceY = (row * GRID_SIZE) + 2;

        return createResampledImage(sourceX, sourceY, 12, 12);
    }
}
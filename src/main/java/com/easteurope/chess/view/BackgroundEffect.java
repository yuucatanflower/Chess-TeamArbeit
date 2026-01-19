package com.easteurope.chess.view;

import com.easteurope.chess.view.scenes.SettingsScene;
import javafx.animation.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class BackgroundEffect {

    // Overload: If no ID provided, use the current global theme
    public static Pane createAnimatedBackground() {
        return createAnimatedBackground(SettingsScene.currentTheme);
    }

    // Main method: Creates background for specific theme
    public static Pane createAnimatedBackground(int themeId) {
        StackPane backgroundPane = new StackPane();

        double width = 3000;
        double height = 4000;
        double scrollDistance = 1080;

        Rectangle bgRect = new Rectangle(width, height);

        // --- DEFINE GRADIENTS BASED ON THEME ---
        Stop[] stops;

        switch (themeId) {
            case 2: // Red Theme (Dark Red / Black)
                stops = new Stop[] {
                        new Stop(0.0, Color.web("#1a0505")), // Very Dark Red
                        new Stop(0.5, Color.web("#4a0e0e")), // Deep Crimson
                        new Stop(1.0, Color.web("#1a0505"))
                };
                break;
            case 3: // Blue/Green Theme (Dark Teal / Cyan)
                stops = new Stop[] {
                        new Stop(0.0, Color.web("#1ed4bc")),
                        new Stop(0.5, Color.web("#1ea7d4")),
                        new Stop(1.0, Color.web("#1ed4bc"))
                };
                break;
            case 4: //  Zombie Theme (Olive/Brown)
                stops = new Stop[] {
                        new Stop(0.0, Color.web("#3e522f")), // Dark Brownish-Olive
                        new Stop(0.5, Color.web("#698750")), // Brighter Olive Green
                        new Stop(1.0, Color.web("#3e522f"))  // Match Start
                };
                break;
            default: // Theme 1 (Original Purple/Navy)
                stops = new Stop[] {
                        new Stop(0.0, Color.web("#141E30")),
                        new Stop(0.5, Color.web("#243B55")),
                        new Stop(1.0, Color.web("#141E30"))
                };
                break;
        }

        LinearGradient gradient = new LinearGradient(
                0, 0, 0, scrollDistance,
                false,
                CycleMethod.REPEAT,
                stops
        );

        bgRect.setFill(gradient);

        // Dark Overlay
        Rectangle overlay = new Rectangle(width, height, Color.rgb(0, 0, 0, 0.4));

        // Animation
        TranslateTransition tt = new TranslateTransition(Duration.seconds(40), bgRect);
        tt.setFromY(0);
        tt.setToY(-scrollDistance);
        tt.setInterpolator(Interpolator.LINEAR);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.play();

        backgroundPane.getChildren().addAll(bgRect, overlay);
        return backgroundPane;
    }
}
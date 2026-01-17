package com.easteurope.chess.view;

import javafx.animation.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class BackgroundEffect {

    public static Pane createAnimatedBackground() {
        StackPane backgroundPane = new StackPane();

        // 1. DIMENSIONS
        double width = 3000;
        double height = 4000;
        double scrollDistance = 1080; // The distance we move before looping

        Rectangle bgRect = new Rectangle(width, height);

        // 2. SEAMLESS GRADIENT
        Stop[] stops = new Stop[] {
                new Stop(0.0, Color.web("#141E30")),  // Deep Slate (Dark)
                new Stop(0.5, Color.web("#243B55")),  // Navy (Lighter)
                new Stop(1.0, Color.web("#141E30"))   // Deep Slate (Dark) - Matches Start
        };

        LinearGradient gradient = new LinearGradient(
                0, 0, 0, scrollDistance, // Start(0,0) -> End(0, 1080)
                false,                   // Proportional = false (Use absolute pixels)
                CycleMethod.REPEAT,      // Repeat this pattern vertically
                stops
        );

        bgRect.setFill(gradient);

        // 3. DARK OVERLAY
        // Keeps the text readable
        Rectangle overlay = new Rectangle(width, height, Color.rgb(0, 0, 0, 0.4));

        // 4. ANIMATION
        // Move up by exactly one gradient loop length (1080px)
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
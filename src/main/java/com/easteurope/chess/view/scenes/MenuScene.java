package com.easteurope.chess.view.scenes;

import com.easteurope.chess.Main;
import com.easteurope.chess.view.BackgroundEffect;
import com.easteurope.chess.view.SoundManager;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class MenuScene {

    private final Main mainApp;

    public MenuScene(Main mainApp) {
        this.mainApp = mainApp;
    }

    public StackPane getView() {
        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);

        // --- Logo Logic ---
        ImageView logoImageView = new ImageView(new Image(getClass().getResourceAsStream("/logo.png")));
        logoImageView.setFitWidth(150);
        logoImageView.setPreserveRatio(true);

        // Up/Down Hover Animation
        TranslateTransition hoverAnimation = new TranslateTransition(Duration.seconds(2.5), logoImageView);
        hoverAnimation.setFromY(12);
        hoverAnimation.setToY(-12);
        hoverAnimation.setCycleCount(TranslateTransition.INDEFINITE);
        hoverAnimation.setAutoReverse(true);
        hoverAnimation.setInterpolator(Interpolator.EASE_BOTH);
        hoverAnimation.play();

        VBox.setMargin(logoImageView, new Insets(0, 0, 30, 0));

        // --- Styles ---
        Font customFont = Font.loadFont(getClass().getResourceAsStream("/RetroByte.ttf"), 28);
        String fontFamily = (customFont != null) ? customFont.getFamily() : "Arial";

        String btnStyle = """
                -fx-background-color: white;
                -fx-text-fill: #233447;
                -fx-font-family: "%s";
                -fx-font-size: 30px;
                -fx-padding: 8 24;
                -fx-border-color: white;
                -fx-border-width: 2;
                -fx-border-radius: 6;
                -fx-background-radius: 6;
            """.formatted(fontFamily);

        Button playBtn = new Button("PLAY");
        Button settingsBtn = new Button("SETTINGS");
        Button exitBtn = new Button("EXIT");

        playBtn.setStyle(btnStyle);
        settingsBtn.setStyle(btnStyle);
        exitBtn.setStyle(btnStyle);

        // --- Actions ---
        playBtn.setOnAction(e -> {
            SoundManager.playSound("click");
            mainApp.showSetupView(); // Update main navigation call
        });

        settingsBtn.setOnAction(e -> {
            SoundManager.playSound("click");
            mainApp.showSettingsView();
        });

        exitBtn.setOnAction(e -> {
            SoundManager.playSound("click");
            mainApp.getWindow().close();
        });

        content.getChildren().addAll(logoImageView, playBtn, settingsBtn, exitBtn);

        StackPane root = new StackPane();
        root.getChildren().addAll(BackgroundEffect.createAnimatedBackground(), content);

        return root; // Return the StackPane directly
    }
}
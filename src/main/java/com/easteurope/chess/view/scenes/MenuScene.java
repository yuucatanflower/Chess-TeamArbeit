package com.easteurope.chess.view.scenes;

import com.easteurope.chess.Main;
import com.easteurope.chess.view.BackgroundEffect;
import com.easteurope.chess.view.SoundManager;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class MenuScene {

    private final Main mainApp;
    // Static flag to ensure the intro only plays once upon application start
    private static boolean hasPlayedIntro = false;

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

        // Normal Style: White background, Dark text
        String btnStyle = """
                -fx-background-color: white;
                -fx-text-fill: #233447;
                -fx-font-family: "%s";
                -fx-font-size: 30px;
                -fx-padding: 5 24; 
                -fx-border-color: white;
                -fx-border-width: 2;
                -fx-border-radius: 3;
                -fx-background-radius: 6;
                -fx-effect: dropshadow(one-pass-box, black, 0, 0, -4, 4);
            """.formatted(fontFamily);

        // Hover Style: Transparent background, White text
        String btnHoverStyle = """
                -fx-background-color: transparent;
                -fx-text-fill: white;
                -fx-font-family: "%s";
                -fx-font-size: 30px;
                -fx-padding: 5 24; 
                -fx-border-color: white;
                -fx-border-width: 2;
                -fx-border-radius: 3;
                -fx-background-radius: 6;
                -fx-effect: dropshadow(one-pass-box, black, 0, 0, -4, 4);
            """.formatted(fontFamily);

        Button playBtn = new Button("PLAY");
        Button settingsBtn = new Button("SETTINGS");
        Button exitBtn = new Button("EXIT");

        // Set initial style
        playBtn.setStyle(btnStyle);
        settingsBtn.setStyle(btnStyle);
        exitBtn.setStyle(btnStyle);

        // --- Hover Effects ---
        playBtn.setOnMouseEntered(e -> playBtn.setStyle(btnHoverStyle));
        playBtn.setOnMouseExited(e -> playBtn.setStyle(btnStyle));

        settingsBtn.setOnMouseEntered(e -> settingsBtn.setStyle(btnHoverStyle));
        settingsBtn.setOnMouseExited(e -> settingsBtn.setStyle(btnStyle));

        exitBtn.setOnMouseEntered(e -> exitBtn.setStyle(btnHoverStyle));
        exitBtn.setOnMouseExited(e -> exitBtn.setStyle(btnStyle));

        // --- Actions ---
        playBtn.setOnAction(e -> {
            SoundManager.playSound("click");
            mainApp.showSetupView();
        });

        settingsBtn.setOnAction(e -> {
            SoundManager.playSound("click");
            mainApp.showSettingsView();
        });

        exitBtn.setOnAction(e -> {
            SoundManager.playSound("click");
            mainApp.getWindow().close();
        });

        // --- Layout ---
        content.getChildren().addAll(logoImageView, playBtn, settingsBtn, exitBtn);

        StackPane root = new StackPane();
        root.getChildren().addAll(BackgroundEffect.createAnimatedBackground(), content);

        // --- INTRO ANIMATION (First Load Only) ---
        if (!hasPlayedIntro) {
            Rectangle blackCurtain = new Rectangle();
            blackCurtain.setFill(Color.BLACK);
            blackCurtain.widthProperty().bind(root.widthProperty());
            blackCurtain.heightProperty().bind(root.heightProperty());

            root.getChildren().add(blackCurtain);

            PauseTransition wait = new PauseTransition(Duration.seconds(1.5));

            FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.0), blackCurtain);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            SequentialTransition introSequence = new SequentialTransition(wait, fadeOut);

            introSequence.setOnFinished(e -> root.getChildren().remove(blackCurtain));
            introSequence.play();

            hasPlayedIntro = true;
        }

        return root;
    }
}
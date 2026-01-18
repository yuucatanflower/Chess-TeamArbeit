package com.easteurope.chess.view.scenes;

import com.easteurope.chess.Main;
import com.easteurope.chess.view.BackgroundEffect;
import com.easteurope.chess.view.SoundManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class SettingsScene {

    private final Main mainApp;

    public SettingsScene(Main mainApp) {
        this.mainApp = mainApp;
    }

    public StackPane getView() {
        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);

        // --- Fonts ---
        Font customFont = Font.loadFont(getClass().getResourceAsStream("/RetroByte.ttf"), 28);
        String fontFamily = (customFont != null) ? customFont.getFamily() : "Arial";

        Label title = new Label("SETTINGS");
        title.setStyle("""
                -fx-font-family: "%s";
                -fx-font-size: 64px;
                -fx-text-fill: white;
                -fx-font-weight: bold;
            """.formatted(fontFamily));

        // --- Volume Controls ---
        VBox volumeBox = new VBox(20);
        volumeBox.setAlignment(Pos.CENTER);
        volumeBox.setPadding(new Insets(20));
        volumeBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-background-radius: 10;");
        volumeBox.setMaxWidth(400);

        String sliderStyle = """
            -fx-control-inner-background: #7f8c8d; 
            -fx-background-color: transparent, #2c3e50, transparent;
            -fx-color: #124373;
            """;

        // Master Volume
        Label masterLabel = new Label("Master Volume");
        masterLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        Slider masterSlider = new Slider(0, 100, SoundManager.getMasterVolume() * 100);
        masterSlider.setStyle(sliderStyle);
        masterSlider.setShowTickLabels(true);
        masterSlider.setShowTickMarks(true);
        masterSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            SoundManager.setMasterVolume(newVal.doubleValue() / 100.0);
        });

        // Music Volume
        Label musicLabel = new Label("Music Volume");
        musicLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        Slider musicSlider = new Slider(0, 100, SoundManager.getMusicVolume() * 100);
        musicSlider.setStyle(sliderStyle);
        musicSlider.setShowTickLabels(true);
        musicSlider.setShowTickMarks(true);
        musicSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            SoundManager.setMusicVolume(newVal.doubleValue() / 100.0);
        });

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

        Button musicBtn = new Button(SoundManager.isMusicPlaying() ? "MUSIC: ON" : "MUSIC: OFF");
        musicBtn.setStyle(btnStyle);
        musicBtn.setOnAction(e -> {
            SoundManager.toggleMusic();
            SoundManager.playSound("click");
            musicBtn.setText(SoundManager.isMusicPlaying() ? "MUSIC: ON" : "MUSIC: OFF");
        });
        musicBtn.setOnMouseEntered(e -> musicBtn.setStyle(btnStyle));
        musicBtn.setOnMouseExited(e -> musicBtn.setStyle(btnStyle));

        volumeBox.getChildren().addAll(masterLabel, masterSlider, musicLabel, musicSlider, musicBtn);

        // --- Back Button ---
        Button backBtn = new Button("BACK TO MENU");
        backBtn.setStyle(btnStyle);

        backBtn.setOnAction(e -> {
            SoundManager.playSound("click");
            mainApp.showMenuView(); // Calls back to Main
        });

        content.getChildren().addAll(title, volumeBox, backBtn);

        StackPane root = new StackPane();
        root.getChildren().addAll(BackgroundEffect.createAnimatedBackground(), content);

        return root;
    }
}

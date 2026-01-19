package com.easteurope.chess.view.scenes;

import com.easteurope.chess.Main;
import com.easteurope.chess.view.BackgroundEffect;
import com.easteurope.chess.view.ImageLoader;
import com.easteurope.chess.view.SoundManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class SettingsScene {

    private final Main mainApp;
    public static int currentTheme = 1;

    public SettingsScene(Main mainApp) {
        this.mainApp = mainApp;
    }

    public StackPane getView() {
        StackPane root = new StackPane();
        root.getChildren().add(BackgroundEffect.createAnimatedBackground(currentTheme));

        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);

        Font customFont = Font.loadFont(getClass().getResourceAsStream("/RetroByte.ttf"), 28);
        String fontFamily = (customFont != null) ? customFont.getFamily() : "Arial";

        Label title = new Label("SETTINGS");
        title.setStyle("-fx-font-family: \"%s\"; -fx-font-size: 64px; -fx-text-fill: white; -fx-font-weight: bold;".formatted(fontFamily));

        VBox settingsBox = new VBox(20);
        settingsBox.setAlignment(Pos.CENTER);
        settingsBox.setPadding(new Insets(20));
        settingsBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-background-radius: 10;");
        settingsBox.setMaxWidth(600); // Widened to fit 4 buttons

        String sliderStyle = "-fx-control-inner-background: #7f8c8d; -fx-background-color: transparent, #2c3e50, transparent; -fx-color: #124373;";
        String btnStyle = "-fx-background-color: white; -fx-text-fill: #233447; -fx-font-family: \"%s\"; -fx-font-size: 24px; -fx-padding: 8 24; -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 6; -fx-background-radius: 6;".formatted(fontFamily);

        Label masterLabel = new Label("Master Volume");
        masterLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        Slider masterSlider = new Slider(0, 100, SoundManager.getMasterVolume() * 100);
        masterSlider.setStyle(sliderStyle);
        masterSlider.valueProperty().addListener((obs, oldVal, newVal) -> SoundManager.setMasterVolume(newVal.doubleValue() / 100.0));

        Label musicLabel = new Label("Music Volume");
        musicLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        Slider musicSlider = new Slider(0, 100, SoundManager.getMusicVolume() * 100);
        musicSlider.setStyle(sliderStyle);
        musicSlider.valueProperty().addListener((obs, oldVal, newVal) -> SoundManager.setMusicVolume(newVal.doubleValue() / 100.0));

        Button musicBtn = new Button(SoundManager.isMusicPlaying() ? "MUSIC: ON" : "MUSIC: OFF");
        musicBtn.setStyle(btnStyle);
        musicBtn.setOnAction(e -> {
            SoundManager.toggleMusic();
            SoundManager.playSound("click");
            musicBtn.setText(SoundManager.isMusicPlaying() ? "MUSIC: OFF" : "MUSIC: ON");
        });

        // --- Theme Controls ---
        Label themeLabel = new Label("Theme Selection");
        themeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");

        HBox themeButtons = new HBox(15);
        themeButtons.setAlignment(Pos.CENTER);

        Button theme1Btn = new Button("Darko");
        theme1Btn.setStyle("-fx-background-color: #243B55; -fx-text-fill: white; -fx-font-size: 14px;");
        theme1Btn.setOnAction(e -> { applyTheme(1, root); SoundManager.playSound("click"); });

        Button theme2Btn = new Button("Sunset");
        theme2Btn.setStyle("-fx-background-color: #5e1818; -fx-text-fill: white; -fx-font-size: 14px;");
        theme2Btn.setOnAction(e -> { applyTheme(2, root); SoundManager.playSound("click"); });

        Button theme3Btn = new Button("Heaven");
        theme3Btn.setStyle("-fx-background-color: #203a43; -fx-text-fill: white; -fx-font-size: 14px;");
        theme3Btn.setOnAction(e -> { applyTheme(3, root); SoundManager.playSound("click"); });

        // NEW BUTTON: Zombie Theme
        Button theme4Btn = new Button("Zombie");
        theme4Btn.setStyle("-fx-background-color: #525c06; -fx-text-fill: white; -fx-font-size: 14px;");
        theme4Btn.setOnAction(e -> { applyTheme(4, root); SoundManager.playSound("click"); });

        themeButtons.getChildren().addAll(theme1Btn, theme2Btn, theme3Btn, theme4Btn);

        settingsBox.getChildren().addAll(masterLabel, masterSlider, musicLabel, musicSlider, musicBtn, themeLabel, themeButtons);

        Button backBtn = new Button("BACK TO MENU");
        backBtn.setStyle(btnStyle);
        backBtn.setOnAction(e -> {
            SoundManager.playSound("click");
            mainApp.showMenuView();
        });

        content.getChildren().addAll(title, settingsBox, backBtn);
        root.getChildren().add(content);

        return root;
    }

    private void applyTheme(int themeId, StackPane root) {
        currentTheme = themeId;
        ImageLoader.loadTheme(themeId);
        root.getChildren().remove(0);
        root.getChildren().add(0, BackgroundEffect.createAnimatedBackground(themeId));
    }
}
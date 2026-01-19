package com.easteurope.chess.view.scenes;

import com.easteurope.chess.Main;
import com.easteurope.chess.model.GameConfig;
import com.easteurope.chess.model.coreData.Color;
import com.easteurope.chess.view.BackgroundEffect;
import com.easteurope.chess.view.SoundManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SetupScene {

    private final Main mainApp;

    // Internal State
    private long selectedTimeMs = 60 * 1000;
    private long selectedIncrementMs = 0;
    private Color selectedColor = Color.WHITE;
    private int selectedBotLevel = 0;

    // Styles
    private String timeSelected, timeUnselected;
    private String botSelected, botUnselected, pvpSelected;
    private String startStyle, startHover;

    public SetupScene(Main mainApp) {
        this.mainApp = mainApp;
    }

    public StackPane getView() {
        VBox layout = new VBox(30);

        // --- 1. LOAD FONTS ---
        Font mineFont = Font.loadFont(getClass().getResourceAsStream("/Minecraftia-Regular.ttf"), 16);
        String mineFamily = (mineFont != null) ? mineFont.getFamily() : "Verdana";

        Font retroFont = Font.loadFont(getClass().getResourceAsStream("/RetroByte.ttf"), 18);
        String retroFamily = (retroFont != null) ? retroFont.getFamily() : "Arial";

        // --- 2. DEFINE STYLES ---

        // Label Styles (RetroByte)
        String titleStyle = "-fx-text-fill: white; -fx-font-family: \"%s\"; -fx-font-size: 32px;".formatted(retroFamily);
        String sectionStyle = "-fx-text-fill: white; -fx-font-family: \"%s\"; -fx-font-size: 20px;".formatted(retroFamily);

        // Base Style Construction
        String shadowEffect = "-fx-effect: dropshadow(one-pass-box, black, 0, 0, -4, 4);";
        String commonBtn = "-fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 3; -fx-background-radius: 6; " + shadowEffect;

        // A) MINECRAFTIA STYLES (Time, Increment, Bot Names, Colors)
        String baseMineStyle = commonBtn + "-fx-font-family: \"%s\"; -fx-font-size: 16px;".formatted(mineFamily);

        // Time & Increment
        timeSelected = baseMineStyle + "-fx-background-color: white; -fx-text-fill: #233447;";
        timeUnselected = baseMineStyle + "-fx-background-color: #7f8c8d; -fx-text-fill: white;";

        // Bot Names (Minecraftia)
        String baseBotStyle = commonBtn + "-fx-font-family: \"%s\"; -fx-font-size: 14px;".formatted(mineFamily);
        botUnselected = baseBotStyle + "-fx-background-color: #7f8c8d; -fx-text-fill: white;";
        String botActiveColor = "-fx-background-color: #27ae60; -fx-text-fill: white;";
        String pvpActiveColor = "-fx-background-color: #533c98; -fx-text-fill: white;";

        botSelected = baseBotStyle + botActiveColor;
        pvpSelected = baseBotStyle + pvpActiveColor;

        // B) START BUTTON STYLE (RetroByte)
        String baseStartStyle = commonBtn + "-fx-font-family: \"%s\"; -fx-font-size: 24px; -fx-padding: 5 24;".formatted(retroFamily);
        startStyle = baseStartStyle + "-fx-background-color: white; -fx-text-fill: #233447;";
        // Hover: Transparent background
        startHover = baseStartStyle + "-fx-background-color: transparent; -fx-text-fill: white;";


        // --- Time Buttons (Minecraftia) ---
        Button btn1 = new Button("1m");
        Button btn5 = new Button("5m");
        Button btn10 = new Button("10m");

        // Set Uniform Size for Buttons
        btn1.setPrefSize(100, 40);
        btn5.setPrefSize(100, 40);
        btn10.setPrefSize(100, 40);

        btn1.setAlignment(Pos.CENTER);
        btn5.setAlignment(Pos.CENTER);
        btn10.setAlignment(Pos.CENTER);

        btn1.setStyle(timeSelected);
        btn5.setStyle(timeUnselected);
        btn10.setStyle(timeUnselected);

        btn1.setOnAction(e -> {
            selectedTimeMs = 60 * 1000;
            SoundManager.playSound("click");
            btn1.setStyle(timeSelected);
            btn5.setStyle(timeUnselected);
            btn10.setStyle(timeUnselected);
        });
        btn5.setOnAction(e -> {
            selectedTimeMs = 5 * 60 * 1000;
            SoundManager.playSound("click");
            btn5.setStyle(timeSelected);
            btn1.setStyle(timeUnselected);
            btn10.setStyle(timeUnselected);
        });
        btn10.setOnAction(e -> {
            selectedTimeMs = 10 * 60 * 1000;
            SoundManager.playSound("click");
            btn10.setStyle(timeSelected);
            btn1.setStyle(timeUnselected);
            btn5.setStyle(timeUnselected);
        });

        // --- Layout Setup ---
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Label title = new Label("Bot Selection");
        title.setStyle(titleStyle);

        // --- Navigation Buttons ---
        Button backBtn = new Button("⮌");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 28px;");
        backBtn.setOnAction(e -> {
            SoundManager.playSound("click");
            mainApp.showMenuView();
        });

        HBox backBox = new HBox();
        backBox.setAlignment(Pos.TOP_RIGHT);
        backBox.getChildren().add(backBtn);

        layout.getChildren().add(backBox);

        // Color Selector (Now uses Minecraftia and uniform size)
        layout.getChildren().add(createColorSelector());

        // --- Bot Selection (Minecraftia) ---
        HBox bots = new HBox(20);
        bots.setAlignment(Pos.CENTER);
        List<Button> botButtons = new ArrayList<>();

        // PvP Button
        Button pvpBtn = new Button("PvP");
        pvpBtn.setPrefSize(150, 140); // INCREASED WIDTH
        pvpBtn.setStyle(pvpSelected);
        pvpBtn.setAlignment(Pos.CENTER);
        pvpBtn.setTextAlignment(TextAlignment.CENTER);

        Image pvpImage = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/images/pvp.png"))
        );

        pvpBtn.setOnAction(e -> {
            selectedBotLevel = 0;
            SoundManager.playSound("click");
            pvpBtn.setStyle(pvpSelected);
            for (Button b : botButtons) b.setStyle(botUnselected);
        });

        ImageView pvpView = new ImageView(pvpImage);
        pvpView.setFitWidth(80);
        pvpView.setFitHeight(80);
        pvpView.setPreserveRatio(true);

        pvpBtn.setGraphic(pvpView);
        pvpBtn.setContentDisplay(ContentDisplay.TOP);
        pvpBtn.setGraphicTextGap(5);

        bots.getChildren().add(pvpBtn);

        // Bot portraits
        for (int i = 1; i <= 4; i++) {
            final int level = i;
            String imagePath = "/images/bot" + i + ".png";
            Image image = new Image(getClass().getResource(imagePath).toString());

            Button portraitBtn = new Button(getBotName(i));
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(80);
            imageView.setFitHeight(80);
            imageView.setPreserveRatio(true);
            portraitBtn.setGraphic(imageView);
            portraitBtn.setContentDisplay(ContentDisplay.TOP);
            portraitBtn.setGraphicTextGap(5);

            portraitBtn.setPrefSize(150, 140); // INCREASED WIDTH
            portraitBtn.setStyle(botUnselected);
            portraitBtn.setAlignment(Pos.CENTER);
            portraitBtn.setTextAlignment(TextAlignment.CENTER);

            botButtons.add(portraitBtn);

            portraitBtn.setOnAction(e -> {
                selectedBotLevel = level;
                SoundManager.playSound("click");
                pvpBtn.setStyle(botUnselected);
                for (Button b : botButtons) {
                    if (b == portraitBtn)
                        b.setStyle(botSelected);
                    else
                        b.setStyle(botUnselected);
                }
            });
            bots.getChildren().add(portraitBtn);
        }

        layout.getChildren().add(bots);

        // --- Time Control UI (RetroByte Label) ---
        Label timeLabel = new Label("Time Control");
        timeLabel.setStyle(sectionStyle);
        layout.getChildren().add(timeLabel);

        HBox timeControls = new HBox(20);
        timeControls.setAlignment(Pos.CENTER);
        timeControls.getChildren().addAll(btn1, btn5, btn10);
        layout.getChildren().add(timeControls);

        // --- Increment UI (RetroByte Label) ---
        Label incLabel = new Label("Increment");
        incLabel.setStyle(sectionStyle);
        layout.getChildren().add(incLabel);

        HBox incrementControls = new HBox(20);
        incrementControls.setAlignment(Pos.CENTER);

        // Increment Buttons (Minecraftia)
        Button inc0 = new Button("+0s");
        Button inc5 = new Button("+5s");
        Button inc10 = new Button("+10s");

        // Set Uniform Size for Increment Buttons
        inc0.setPrefSize(100, 40);
        inc5.setPrefSize(100, 40);
        inc10.setPrefSize(100, 40);

        inc0.setAlignment(Pos.CENTER);
        inc5.setAlignment(Pos.CENTER);
        inc10.setAlignment(Pos.CENTER);

        inc0.setStyle(timeSelected);
        inc5.setStyle(timeUnselected);
        inc10.setStyle(timeUnselected);

        inc0.setOnAction(e -> {
            selectedIncrementMs = 0;
            SoundManager.playSound("click");
            inc0.setStyle(timeSelected);
            inc5.setStyle(timeUnselected);
            inc10.setStyle(timeUnselected);
        });
        inc5.setOnAction(e -> {
            selectedIncrementMs = 5 * 1000;
            SoundManager.playSound("click");
            inc5.setStyle(timeSelected);
            inc0.setStyle(timeUnselected);
            inc10.setStyle(timeUnselected);
        });
        inc10.setOnAction(e -> {
            selectedIncrementMs = 10 * 1000;
            SoundManager.playSound("click");
            inc10.setStyle(timeSelected);
            inc0.setStyle(timeUnselected);
            inc5.setStyle(timeUnselected);
        });

        incrementControls.getChildren().addAll(inc0, inc5, inc10);
        layout.getChildren().add(incrementControls);

        // --- Start Button (RetroByte) ---
        Button startBtn = new Button("START");
        startBtn.setPrefSize(160, 50);
        startBtn.setAlignment(Pos.CENTER);
        startBtn.setStyle(startStyle);

        startBtn.setOnMouseEntered(e -> startBtn.setStyle(startHover));
        startBtn.setOnMouseExited(e -> startBtn.setStyle(startStyle));

        startBtn.setOnAction(e -> {
            SoundManager.playSound("start");
            GameConfig config = new GameConfig(selectedTimeMs, selectedIncrementMs, selectedColor, selectedBotLevel);
            mainApp.startGame(config);
        });

        layout.getChildren().add(startBtn);

        // --- Final Background ---
        Pane animatedBg = BackgroundEffect.createAnimatedBackground();
        StackPane root = new StackPane();
        root.getChildren().add(animatedBg);
        root.getChildren().add(layout);

        return root;
    }

    private HBox createColorSelector() {
        // Load Minecraftia for these buttons
        Font mineFont = Font.loadFont(getClass().getResourceAsStream("/Minecraftia-Regular.ttf"), 16);
        String mineFamily = (mineFont != null) ? mineFont.getFamily() : "Verdana";

        String baseColorStyle = "-fx-font-family: \"%s\"; -fx-font-size: 16px; -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 3; -fx-background-radius: 6; -fx-effect: dropshadow(one-pass-box, black, 0, 0, -4, 4);".formatted(mineFamily);
        String colorSelected = baseColorStyle + "-fx-background-color: white; -fx-text-fill: #233447;";
        String colorUnselected = baseColorStyle + "-fx-background-color: #7f8c8d; -fx-text-fill: white;";

        HBox colorControl = new HBox(30);
        colorControl.setAlignment(Pos.CENTER);

        Button btnW = new Button("White");
        Button btnB = new Button("Black");

        // Uniform size for Color buttons
        btnW.setPrefSize(100, 40);
        btnB.setPrefSize(100, 40);

        btnW.setAlignment(Pos.CENTER);
        btnB.setAlignment(Pos.CENTER);

        btnW.setStyle(colorSelected); // Default
        btnB.setStyle(colorUnselected);

        btnW.setOnAction(e -> {
            selectedColor = Color.WHITE;
            SoundManager.playSound("click");
            btnW.setStyle(colorSelected);
            btnB.setStyle(colorUnselected);
        });

        btnB.setOnAction(e -> {
            selectedColor = Color.BLACK;
            SoundManager.playSound("click");
            btnB.setStyle(colorSelected);
            btnW.setStyle(colorUnselected);
        });

        colorControl.getChildren().addAll(btnW, btnB);
        return colorControl;
    }

    private String getBotName(int i) {
        return switch (i) {
            case 1 -> "Ember Wizard";
            case 2 -> "Guard Knight";
            case 3 -> "Omen Seer";
            case 4 -> "Rune Lord";
            default -> "Unknown";
        };
    }
}
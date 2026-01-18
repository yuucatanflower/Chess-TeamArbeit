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

    public SetupScene(Main mainApp) {
        this.mainApp = mainApp;
    }

    public StackPane getView() {
        VBox layout = new VBox(30);

        String titleStyle = "-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;";
        String sectionStyle = "-fx-text-fill: white; -fx-font-size: 16px;";

        // --- Time Buttons ---
        Button btn1 = new Button("1m");
        Button btn5 = new Button("5m");
        Button btn10 = new Button("10m");

        btn1.setStyle("-fx-background-color: #ffffff;");
        btn5.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        btn10.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");

        btn1.setOnAction(e -> {
            selectedTimeMs = 60 * 1000;
            SoundManager.playSound("click");
            btn1.setStyle("-fx-background-color: #ffffff;");
            btn5.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
            btn10.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        });
        btn5.setOnAction(e -> {
            selectedTimeMs = 5 * 60 * 1000;
            SoundManager.playSound("click");
            btn5.setStyle("-fx-background-color: #ffffff;");
            btn1.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
            btn10.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        });
        btn10.setOnAction(e -> {
            selectedTimeMs = 10 * 60 * 1000;
            SoundManager.playSound("click");
            btn10.setStyle("-fx-background-color: #ffffff;");
            btn1.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
            btn5.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        });

        // --- Layout Setup ---
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Label title = new Label("Bot Selection");
        title.setStyle(titleStyle);

        // --- Navigation Buttons ---
        Button backBtn = new Button("⮌");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 22px;");
        backBtn.setOnAction(e -> mainApp.showMenuView()); // FIXED: Calls Main

        HBox backBox = new HBox();
        backBox.setAlignment(Pos.TOP_RIGHT);
        backBox.getChildren().add(backBtn);

        layout.getChildren().add(backBox);
        layout.getChildren().add(chooseColor()); // Calls helper method below

        // --- Bot Selection ---
        HBox bots = new HBox(20);
        bots.setAlignment(Pos.CENTER);
        List<Button> botButtons = new ArrayList<>();


        // PvP Button
        Button pvpBtn = new Button("PvP");
        pvpBtn.setPrefSize(100, 120);
        pvpBtn.setStyle("-fx-background-color: #533c98; -fx-text-fill: white;");

        Image pvpImage = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/images/pvp.png"))
        );

        ImageView pvpView = new ImageView(pvpImage);
        pvpView.setFitWidth(90);
        pvpView.setFitHeight(90);
        pvpView.setPreserveRatio(true);

        pvpBtn.setGraphic(pvpView);
        pvpBtn.setContentDisplay(ContentDisplay.TOP);
        pvpBtn.setGraphicTextGap(5);

        bots.getChildren().add(pvpBtn);


        for (int i = 1; i <= 4; i++) {
            final int level = i;
            String imagePath = "/images/bot" + i + ".png";

            // Check if image exists to prevent crash
            Image image;
            try {
                image = new Image(getClass().getResource(imagePath).toString());
            } catch (Exception ex) {
                // Fallback if image missing
                image = null;
            }

            Button portraitBtn = new Button(getBotName(i));
            if (image != null) {
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(90);
                imageView.setFitHeight(90);
                imageView.setPreserveRatio(true);
                portraitBtn.setGraphic(imageView);
                portraitBtn.setContentDisplay(ContentDisplay.TOP);
                portraitBtn.setGraphicTextGap(5);
            }

            portraitBtn.setPrefSize(100, 120);
            portraitBtn.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");

            botButtons.add(portraitBtn);

            portraitBtn.setOnAction(e -> {
                selectedBotLevel = level;
                SoundManager.playSound("click");
                pvpBtn.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
                for (Button b : botButtons) {
                    if (b == portraitBtn)
                        b.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                    else
                        b.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
                }
            });
            bots.getChildren().add(portraitBtn);
        }

        layout.getChildren().add(bots);

        // --- Time Control UI ---
        Label timeLabel = new Label("Time Control");
        timeLabel.setStyle(sectionStyle);
        layout.getChildren().add(timeLabel);

        HBox timeControls = new HBox(20);
        timeControls.setAlignment(Pos.CENTER);
        timeControls.getChildren().addAll(btn1, btn5, btn10);
        layout.getChildren().add(timeControls);

        // --- Increment UI ---
        Label incLabel = new Label("Increment");
        incLabel.setStyle(sectionStyle);
        layout.getChildren().add(incLabel);

        HBox incrementControls = new HBox(20);
        incrementControls.setAlignment(Pos.CENTER);

        Button inc0 = new Button("+0s");
        Button inc5 = new Button("+5s");
        Button inc10 = new Button("+10s");

        inc0.setStyle("-fx-background-color: #ffffff;");
        inc5.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        inc10.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");

        inc0.setOnAction(e -> {
            selectedIncrementMs = 0;
            SoundManager.playSound("click");
            inc0.setStyle("-fx-background-color: #ffffff;");
            inc5.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
            inc10.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        });
        inc5.setOnAction(e -> {
            selectedIncrementMs = 5 * 1000;
            SoundManager.playSound("click");
            inc5.setStyle("-fx-background-color: #ffffff;");
            inc0.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
            inc10.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        });
        inc10.setOnAction(e -> {
            selectedIncrementMs = 10 * 1000;
            SoundManager.playSound("click");
            inc10.setStyle("-fx-background-color: #ffffff;");
            inc0.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
            inc5.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        });

        incrementControls.getChildren().addAll(inc0, inc5, inc10);
        layout.getChildren().add(incrementControls);

        // --- Start Button ---
        Button startBtn = new Button("START");
        startBtn.setPrefSize(120, 40);
        startBtn.setStyle("""
                    -fx-background-color: #124373;
                    -fx-text-fill: white;
                    -fx-font-size: 16px;
                    -fx-font-weight: bold;
                """);

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

        return root; // Returns StackPane
    }

    private HBox chooseColor() {
        HBox colorControl = new HBox(30);
        colorControl.setAlignment(Pos.CENTER);

        Button btnW = new Button("White");
        Button btnB = new Button("Black");

        btnW.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: black;"); // Default selected
        btnB.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");

        btnW.setOnAction(e -> {
            selectedColor = Color.WHITE;
            SoundManager.playSound("click");
            btnW.setStyle("-fx-background-color: #ffffff; -fx-text-fill: black;");
            btnB.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        });

        btnB.setOnAction(e -> {
            selectedColor = Color.BLACK;
            SoundManager.playSound("click");
            btnB.setStyle("-fx-background-color: #ffffff; -fx-text-fill: black;");
            btnW.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
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
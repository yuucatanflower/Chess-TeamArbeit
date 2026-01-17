package com.easteurope.chess;

import com.easteurope.chess.controller.Stockfish;
import com.easteurope.chess.model.Piece;
import com.easteurope.chess.view.BackgroundEffect;
import com.easteurope.chess.view.ImageLoader;
import com.easteurope.chess.view.SoundManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider; // Added Slider import
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import com.easteurope.chess.model.GameState;
import com.easteurope.chess.model.coreData.Position;
import com.easteurope.chess.model.coreData.PieceType;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.Scanner;

public class Main extends Application {
    private GameState game;
    private Label whiteTimeLabel;
    private Label blackTimeLabel;
    private TextArea historyArea;
    private Stage window;
    private Position selectedPosition = null;
    private long selectedTimeMs = 60 * 1000;
    private com.easteurope.chess.model.coreData.Color selectedColor = com.easteurope.chess.model.coreData.Color.WHITE;
    private long selectedIncrementMs = 0;

    private Timeline timeline;

    private boolean isPaused = false;
    private StackPane pauseOverlay;

    private java.util.List<Position> possibleMoves = new java.util.ArrayList<>();

    // --- Bot Variables ---
    private int selectedBotLevel = 0;
    private Stockfish bot;
    private boolean isBotTurn = false;


    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        System.out.println("--- CHESS PROJECT STARTER ---");
        System.out.println("Select your mode:");
        System.out.println("[1] Console Mode (Text-based)");
        System.out.println("[2] Graphical Mode (JavaFX Window)");
        System.out.print("> ");

        String choice = sc.nextLine();

        if (choice.equals("2")) {
            launch(args);
        } else {
            startConsoleGame();
        }
    }

    // --- Console ---
    public static void startConsoleGame() {
        GameState game = new GameState(5 * 60 * 1000, com.easteurope.chess.model.coreData.Color.WHITE, 0);


        Scanner input = new Scanner(System.in);

        System.out.println("---GAME STARTED---");
        System.out.println("Type moves as 'e2-e4' or 'undo' to revert move. Type 'exit' or 'quit' to end the game. ");
        Stockfish bot = new Stockfish();

        if (bot.startEngine()) {
            System.out.println("Engine started!");
            String startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
            String move = bot.getBestMove(startFen, 1000);
            System.out.println("Stockfish suggests: " + move);
            bot.stopEngine();
        } else {
            System.out.println("Failed to start engine.");
        }

        while (!game.isGameOver()) {
            game.getBoard().printBoard();
            System.out.println("STATUS: " + game.getStatusMessage());
            System.out.print("> ");

            String command = input.nextLine().trim();

            if (command.equalsIgnoreCase("exit") || command.equalsIgnoreCase("quit")) {
                break;
            }
            if (command.equalsIgnoreCase("undo")) {
                game.undo();
                continue;
            }

            if (isValidInputFormat(command)) {
                try {
                    String[] commandParts = command.split("-");
                    Position from = Position.fromAlgebraicNotation(commandParts[0]);
                    Position to = Position.fromAlgebraicNotation(commandParts[1]);

                    boolean success = game.playTurn(from, to);
                    if (success) {
                        System.out.println("Move from " + from.toAlgebraicNotation() + " to " + to.toAlgebraicNotation());
                    } else {
                        System.out.println("Move failed!");
                    }
                } catch (Exception e) {
                    System.out.println("Error parsing move: Use 'e2-e4' format.");
                }
            } else {
                System.out.println("Unknown command! Type moves as 'e2-e4' or 'undo' to revert move.");
            }
        }
        game.getBoard().printBoard();
        System.out.println("FINAL STATUS: " + game.getStatusMessage());
        System.out.println("Game Ended.");
        input.close();
    }

    private static boolean isValidInputFormat(String input) {
        return input.matches("[a-h][1-8]-[a-h][1-8]");
    }

    // --- JAVAFX ---
    @Override
    public void start(Stage primaryStage) {
        this.window = primaryStage;
        window.setTitle("Chess TeamArbeit");

        SoundManager.loadSounds();
        SoundManager.startMusic();

        showMenuScene();
    }

    // SCREEN 1: START MENU
    private void showMenuScene() {
        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);

        Label title = new Label("CHESS");
        title.setStyle("-fx-font-size: 64px; -fx-text-fill: white; -fx-font-weight: bold;");
        VBox.setMargin(title, new Insets(0, 0, 30, 0));

        String btnStyle = """
                    -fx-background-color: transparent;
                    -fx-text-fill: white;
                    -fx-font-size: 24px;
                    -fx-padding: 8 24;
                    -fx-border-color: white;
                    -fx-border-width: 2;
                    -fx-border-radius: 6;
                    -fx-background-radius: 6;
                """;

        String btnStyleHover = """
                    -fx-background-color: white;
                    -fx-text-fill: #233447;
                    -fx-font-size: 24px;
                    -fx-padding: 8 24;
                    -fx-border-color: white;
                    -fx-border-width: 2;
                    -fx-border-radius: 6;
                    -fx-background-radius: 6;
                """;

        Button playBtn = new Button("PLAY");
        Button settingsBtn = new Button("SETTINGS");
        Button exitBtn = new Button("EXIT");

        playBtn.setStyle(btnStyle);
        settingsBtn.setStyle(btnStyle);
        exitBtn.setStyle(btnStyle);

        // ---  Sound on Click ---
        playBtn.setOnMouseEntered(e -> playBtn.setStyle(btnStyleHover));
        playBtn.setOnMouseExited(e -> playBtn.setStyle(btnStyle));

        settingsBtn.setOnMouseEntered(e -> settingsBtn.setStyle(btnStyleHover));
        settingsBtn.setOnMouseExited(e -> settingsBtn.setStyle(btnStyle));

        exitBtn.setOnMouseEntered(e -> exitBtn.setStyle(btnStyleHover));
        exitBtn.setOnMouseExited(e -> exitBtn.setStyle(btnStyle));

        playBtn.setOnAction(e -> {
            SoundManager.playSound("click");
            showSetupScene();
        });

        exitBtn.setOnAction(e -> {
            SoundManager.playSound("click");
            window.close();
        });

        settingsBtn.setOnAction(e -> {
            SoundManager.playSound("click");
            showSettingsScene();
        });

        // 1. Add the buttons to the VBox (Removed Music Btn from here)
        content.getChildren().addAll(title, playBtn, settingsBtn, exitBtn);

        Pane animatedBg = BackgroundEffect.createAnimatedBackground();
        StackPane root = new StackPane();
        root.getChildren().add(animatedBg);
        root.getChildren().add(content);

        Scene scene = new Scene(root, 800, 600);
        window.setScene(scene);
        window.show();
    }

    // SCREEN 2: GAME SETUP
    private void showSetupScene() {
        VBox layout = new VBox(30);

        String titleStyle = "-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;";
        String sectionStyle = "-fx-text-fill: white; -fx-font-size: 16px;";

        String setupBtnStyle = """
            -fx-background-color: #7f8c8d;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-pref-width: 70px;
        """;

        Button btn1 = new Button("1m");
        Button btn5 = new Button("5m");
        Button btn10 = new Button("10m");

        btn1.setOnAction(e -> {
            selectedTimeMs = 60 * 1000;
            SoundManager.playSound("click");
        });
        btn5.setOnAction(e -> {
            selectedTimeMs = 5 * 60 * 1000;
            SoundManager.playSound("click");
        });
        btn10.setOnAction(e -> {
            selectedTimeMs = 10 * 60 * 1000;
            SoundManager.playSound("click");
        });

        Button startBtn = new Button("START");
        startBtn.setPrefSize(120, 40);
        startBtn.setStyle("""
            -fx-background-color: #124373;
            -fx-text-fill: white;
            -fx-font-size: 16px;
            -fx-font-weight: bold;
        """);

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Label title = new Label("Bot Selection");
        title.setStyle(titleStyle);

        // --- BACK BUTTON IN TOP-RIGHT  ---
        Button backBtn = new Button("⮌");
        backBtn.setStyle("""
                    -fx-background-color: transparent;
                    -fx-text-fill: white;
                    -fx-font-size: 22px;
                """);
        backBtn.setOnAction(e -> showMenuScene());


        HBox backBox = new HBox();
        backBox.setAlignment(Pos.TOP_RIGHT);
        backBox.getChildren().add(backBtn);


        layout.getChildren().add(backBox);
        layout.getChildren().add(chooseColor());

        // --- Bot Selection Logic ---
        HBox bots = new HBox(20);
        bots.setAlignment(Pos.CENTER);

        java.util.List<Button> botButtons = new java.util.ArrayList<>();

        Button pvpBtn = new Button("PvP");
        pvpBtn.setPrefSize(80, 80);
        pvpBtn.setStyle("-fx-background-color: #533c98; -fx-text-fill: white;"); // Default selected
        pvpBtn.setOnAction(e -> {
            selectedBotLevel = 0;
            SoundManager.playSound("click");
            // Update styles
            pvpBtn.setStyle("-fx-background-color: #533c98; -fx-text-fill: white;");
            for (Button b : botButtons) b.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        });
        bots.getChildren().add(pvpBtn);

        // Bot Buttons (1-4)
        for (int i = 1; i <= 4; i++) {
            final int level = i;
            Button botBtn = new Button("BOT " + i);
            botBtn.setPrefSize(80, 80);
            botBtn.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
            botButtons.add(botBtn);

            botBtn.setOnAction(e -> {
                selectedBotLevel = level;
                SoundManager.playSound("click");
                // Update styles
                pvpBtn.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
                for (Button b : botButtons) {
                    if (b == botBtn) b.setStyle("-fx-background-color: #124373; -fx-text-fill: white;");
                    else b.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
                }
            });

            bots.getChildren().add(botBtn);
        }
        layout.getChildren().add(bots); // ADD THE BOTS CONTAINER TO LAYOUT

        // Time Setting
        Label timeLabel = new Label("Time Control");
        timeLabel.setStyle(sectionStyle);
        layout.getChildren().add(timeLabel); // ADD LABEL

        HBox timeControls = new HBox(20);
        timeControls.setAlignment(Pos.CENTER);

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


        timeControls.getChildren().addAll(btn1, btn5, btn10);
        layout.getChildren().add(timeControls); // ADD TIME CONTROLS

        // Increments
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
        layout.getChildren().add(incrementControls); // ADD INCREMENTS

        // --- Start Logic (Init Bot) ---
        startBtn.setOnAction(e -> {
            SoundManager.playSound("start");
            // Initialize Bot if selected
            if (selectedBotLevel > 0) {
                bot = new Stockfish();
                if (bot.startEngine()) {
                    System.out.println("Engine Started. Level: " + selectedBotLevel);
                } else {
                    System.out.println("Engine Failed.");
                    selectedBotLevel = 0; // Fallback
                }
            }
            showBoardScene();
        });

        layout.getChildren().add(startBtn); // ADD START BUTTON

        Pane animatedBg = BackgroundEffect.createAnimatedBackground();
        StackPane root = new StackPane();
        root.getChildren().add(animatedBg);
        root.getChildren().add(layout);

        window.setScene(new Scene(root, 800, 600));
    }

    // SCREEN 3: GAME BOARD
    private void showBoardScene() {
        game = new GameState(selectedTimeMs, selectedColor, selectedIncrementMs);

        BorderPane uiLayout = new BorderPane();
        uiLayout.setStyle("-fx-background-color: transparent;");

        StackPane boardStack = new StackPane();

        GridPane boardGui = new GridPane();
        boardGui.setAlignment(Pos.CENTER);
        boardGui.setMouseTransparent(true);

        GridPane inputGrid = new GridPane();
        inputGrid.setAlignment(Pos.CENTER);
        setupInputLayer(inputGrid, boardGui);

        boardStack.getChildren().addAll(boardGui, inputGrid);

        updateBoard(boardGui);

        uiLayout.setCenter(boardStack);
        pauseOverlay = buildPauseOverlay();
        pauseOverlay.setVisible(false);
        boardStack.getChildren().add(pauseOverlay);


        historyArea = new TextArea();
        historyArea.setEditable(false);
        // Made preferred height smaller to fit a "square" look alongside the board padding
        historyArea.setPrefHeight(200); // Decreased height

        // --- Transparent Style ---
        historyArea.setStyle("""
            -fx-control-inner-background: transparent;
            -fx-background-color: transparent;
            -fx-text-fill: white;
            -fx-font-family: 'Consolas', 'Monospaced';
            -fx-font-size: 16px;
            -fx-highlight-fill: transparent;
            -fx-highlight-text-fill: white;
        """);
        // To remove the scrollpane border/background inside TextArea structure
        historyArea.getStylesheets().add("data:text/css," +
                ".text-area .scroll-pane { -fx-background-color: transparent; -fx-hbar-policy: never; }" + // Hide horizontal scrollbar
                ".text-area .scroll-pane .viewport { -fx-background-color: transparent; }" +
                ".text-area .content { -fx-background-color: transparent; }"
        );

        VBox sidebar = new VBox(10);
        // --- INCREASED PADDING ---
        // Top and Bottom padding increased to make it smaller in height visually
        sidebar.setPadding(new Insets(10, 15, 10, 15));
        sidebar.setPrefWidth(240); // Slightly wider to avoid horizontal scroll
        // More transparent backing for readability
        sidebar.setStyle("-fx-background-color: rgba(0, 0, 0, 0.15);");

        // --- TOP_CENTER ALIGNMENT ---
        sidebar.setAlignment(Pos.TOP_CENTER);

        Label historyLabel = new Label("");
        historyLabel.setTextFill(Color.WHITE);
        historyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        sidebar.getChildren().addAll(historyLabel, historyArea);
        uiLayout.setRight(sidebar);


        HBox topBar = new HBox(100);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(10));

        whiteTimeLabel = new Label();
        blackTimeLabel = new Label();

        whiteTimeLabel.setText("White: " + formatTime(game.getWhiteTimeMs()));
        blackTimeLabel.setText("Black: " + formatTime(game.getBlackTimeMs()));

        whiteTimeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        blackTimeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        topBar.getChildren().addAll(blackTimeLabel, whiteTimeLabel);
        Button pauseBtn = new Button("≡");
        pauseBtn.setOnAction(e -> togglePause());
        pauseBtn.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-background-color: #7f8c8d;");
        topBar.getChildren().add(pauseBtn);

        uiLayout.setTop(topBar);

        // --- FINAL ASSEMBLY ---
        Pane animatedBg = BackgroundEffect.createAnimatedBackground();
        StackPane root = new StackPane();

        root.getChildren().add(animatedBg);  // Bottom: Animation
        root.getChildren().add(uiLayout);    // Middle: Game UI
        root.getChildren().add(pauseOverlay);// Top: Pause Menu (Hidden by default)

        window.setScene(new Scene(root, 900, 700));
        window.getScene().setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ESCAPE -> togglePause();
            }
        });


        // --- TIMELINE ---
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {

                    game.tickTimer();   // time runs

                    whiteTimeLabel.setText(
                            "White: " + formatTime(game.getWhiteTimeMs())
                    );
                    blackTimeLabel.setText(
                            "Black: " + formatTime(game.getBlackTimeMs())
                    );

                    if (game.isGameOver()) {
                        timeline.stop();
                        SoundManager.playSound("defeat"); // Sound on time out
                    }
                })
        );

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private HBox chooseColor() {

        HBox colorControl = new HBox(30);
        colorControl.setAlignment(Pos.CENTER);

        Button btnW = new Button("White");
        Button btnB = new Button("Black");

        btnW.setStyle("-fx-background-color: #ecf0f1;");
        btnB.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");

        btnW.setOnAction(e -> {
            SoundManager.playSound("click");
            selectedColor = com.easteurope.chess.model.coreData.Color.WHITE;
            btnW.setStyle("-fx-background-color: #ffffff;");
            btnB.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        });

        btnB.setOnAction(e -> {
            SoundManager.playSound("click");
            selectedColor = com.easteurope.chess.model.coreData.Color.BLACK;
            btnB.setStyle("-fx-background-color: #ffffff;");
            btnW.setStyle("-fx-background-color: #7f8c8d;");
        });

        colorControl.getChildren().addAll(btnW, btnB);
        return colorControl;
    }

    // Setting Scene (UPDATED)
    private void showSettingsScene() {
        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);

        Label title = new Label("SETTINGS");
        title.setStyle("-fx-font-size: 48px; -fx-text-fill: white; -fx-font-weight: bold;");

        // --- NEW: Volume Sliders ---
        VBox volumeBox = new VBox(20);
        volumeBox.setAlignment(Pos.CENTER);
        volumeBox.setPadding(new Insets(20));
        volumeBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-background-radius: 10;");
        volumeBox.setMaxWidth(400);

        // Styling string for sliders
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

        // Music Toggle (Moved from Menu)
        String btnStyle = """
                    -fx-background-color: transparent;
                    -fx-text-fill: white;
                    -fx-font-size: 18px;
                    -fx-padding: 8 24;
                    -fx-border-color: white;
                    -fx-border-width: 2;
                    -fx-border-radius: 6;
                    -fx-background-radius: 6;
                """;

        String btnStyleHover = """
                    -fx-background-color: white;
                    -fx-text-fill: #233447;
                    -fx-font-size: 18px;
                    -fx-padding: 8 24;
                    -fx-border-color: white;
                    -fx-border-width: 2;
                    -fx-border-radius: 6;
                    -fx-background-radius: 6;
                """;

        Button musicBtn = new Button(SoundManager.isMusicPlaying() ? "MUSIC: ON" : "MUSIC: OFF");
        musicBtn.setStyle(btnStyle);
        musicBtn.setOnAction(e -> {
            SoundManager.toggleMusic();
            SoundManager.playSound("click");
            musicBtn.setText(SoundManager.isMusicPlaying() ? "MUSIC: ON" : "MUSIC: OFF");
        });
        musicBtn.setOnMouseEntered(e -> musicBtn.setStyle(btnStyleHover));
        musicBtn.setOnMouseExited(e -> musicBtn.setStyle(btnStyle));

        volumeBox.getChildren().addAll(masterLabel, masterSlider, musicLabel, musicSlider, musicBtn);

        // Back Button
        Button backBtn = new Button("BACK TO MENU");
        backBtn.setStyle("""
                    -fx-background-color: transparent;
                    -fx-text-fill: white;
                    -fx-font-size: 24px;
                    -fx-padding: 8 24;
                    -fx-border-color: white;
                    -fx-border-width: 2;
                    -fx-border-radius: 6;
                    -fx-background-radius: 6;
                """);

        backBtn.setOnMouseEntered(e -> backBtn.setStyle("""
                    -fx-background-color: white;
                    -fx-text-fill: #233447;
                    -fx-font-size: 24px;
                    -fx-padding: 8 24;
                    -fx-border-color: white;
                    -fx-border-width: 2;
                    -fx-border-radius: 6;
                    -fx-background-radius: 6;
                """));

        backBtn.setOnMouseExited(e -> backBtn.setStyle("""
                    -fx-background-color: transparent;
                    -fx-text-fill: white;
                    -fx-font-size: 24px;
                    -fx-padding: 8 24;
                    -fx-border-color: white;
                    -fx-border-width: 2;
                    -fx-border-radius: 6;
                    -fx-background-radius: 6;
                """));

        backBtn.setOnAction(e -> {
            SoundManager.playSound("click");
            showMenuScene();
        });

        content.getChildren().addAll(title, volumeBox, backBtn);

        Pane animatedBg = BackgroundEffect.createAnimatedBackground();
        StackPane root = new StackPane();
        root.getChildren().add(animatedBg);
        root.getChildren().add(content);

        Scene scene = new Scene(root, 900, 700);
        window.setScene(scene);
    }

    private String formatTime(long ms) {
        long totalSeconds = ms / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void updateHistory() {
        StringBuilder sb = new StringBuilder();
        int moveNumber = 1;

        String whiteMove = null;

        for (com.easteurope.chess.model.coreData.Move move : game.getMoveHistory()) {

            String moveText =
                    move.from().toAlgebraicNotation()
                            + "-"
                            + move.to().toAlgebraicNotation();

            // WHITE move → start a new line
            if (move.movedPiece().getColor()
                    == com.easteurope.chess.model.coreData.Color.WHITE) {

                whiteMove = moveText;

                sb.append(String.format(
                        "%2d. %-10s",
                        moveNumber,
                        whiteMove
                ));
            }
            // BLACK move → same line, aligned
            else {
                sb.append(String.format(
                        " %-10s%n",
                        moveText
                ));

                moveNumber++;
                whiteMove = null;
            }
        }

        // If last move was only White (no Black yet)
        if (whiteMove != null) {
            sb.append("\n");
        }

        historyArea.setText(sb.toString());
    }

    // Builds the invisible grid for click detection
    private void setupInputLayer(GridPane inputGrid, GridPane boardGui) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                // Create an invisible rectangle as the "click target"
                Rectangle clickArea = new Rectangle(60, 60, Color.TRANSPARENT);

                final int r = row;
                final int c = col;

                // The click triggers the logic and then updates the visual layer (boardGui)
                clickArea.setOnMouseClicked(e -> handleTileClick(r, c, boardGui));

                inputGrid.add(clickArea, col, row);
            }
        }
    }

    private void handleTileClick(int row, int col, GridPane boardGui) {
        // ---Block input if Bot Turn ---
        if (isBotTurn) return;

        Position clickedPos = new Position(row, col);

        System.out.println("Clicked: " + clickedPos.toAlgebraicNotation()); // Debugging

        // CASE 1: Select a piece (First Click)
        if (selectedPosition == null) {
            Piece piece = game.getBoard().getPieceAt(clickedPos);

            // Only allow selecting pieces that belong to the current turn's player
            if (piece != null && piece.getColor() == game.getCurrentTurn()) {
                selectedPosition = clickedPos;
                SoundManager.playSound("start"); // --- Sound ---
                System.out.println("Selected: " + clickedPos.toAlgebraicNotation());
                possibleMoves.clear();
                possibleMoves = piece.getValidMoves(game.getBoard());
                updateBoard(boardGui); // Redraw to show highlight
            }
        }
        // CASE 2: Move or Change Selection (Second Click)
        else {
            // If clicking the same tile again -> Deselect
            if (clickedPos.equals(selectedPosition)) {
                selectedPosition = null;
                possibleMoves.clear();
                updateBoard(boardGui);
                return;
            }

            // --- Detect Capture / Special Move Type BEFORE playing ---
            Piece piece = game.getBoard().getPieceAt(selectedPosition);
            Piece targetPiece = game.getBoard().getPieceAt(clickedPos);
            boolean isCapture = (targetPiece != null);
            boolean isCastle = (piece.getType() == PieceType.KING && Math.abs(selectedPosition.col() - clickedPos.col()) > 1);
            boolean isPromotion = (piece.getType() == PieceType.PAWN && (clickedPos.row() == 0 || clickedPos.row() == 7));

            // Try to execute the move in the game logic
            boolean success = game.playTurn(selectedPosition, clickedPos);
            System.out.println("GameOver? " + game.isGameOver() + " status=" + game.getStatusMessage());

            if (piece != null) {
                possibleMoves = piece.getValidMoves(game.getBoard());
            }
            if (success) {
                // --- Play Sound based on event ---
                if (game.isGameOver()) {
                    if (game.getStatusMessage().contains("Checkmate")) SoundManager.playSound("checkmate");
                    else SoundManager.playSound("victory");
                } else if (game.getStatusMessage().contains("check")) {
                    SoundManager.playSound("check");
                } else if (isCastle) {
                    SoundManager.playSound("castle");
                } else if (isPromotion) {
                    SoundManager.playSound("promote");
                } else if (isCapture) {
                    SoundManager.playSound("capture");
                } else {
                    if (piece.getType() == PieceType.KNIGHT) SoundManager.playSound("knight_move");
                    else SoundManager.playSound("move");
                }

                System.out.println("Move successful!");
                possibleMoves.clear();
                selectedPosition = null; // Reset selection after move

                // 1. Update the board immediately so the user sees the final move
                updateBoard(boardGui);

                updateHistory();

                // 2. Check if the game is over
                if (game.isGameOver()) {
                    showGameOverDialog(); // Shows the popup
                    return; // Stop execution here
                }

                // --- Trigger Bot Move if applicable ---
                if (selectedBotLevel > 0 && !game.isGameOver()) {
                    makeBotMove(boardGui);
                }

                return; // Return here to avoid double-updating at the bottom
            } else {
                // --- FIX: Only play illegal sound if not switching selection ---
                Piece clickedPiece = game.getBoard().getPieceAt(clickedPos);
                if (clickedPiece != null && clickedPiece.getColor() == game.getCurrentTurn()) {
                    selectedPosition = clickedPos; // Switch selection
                    SoundManager.playSound("start"); // --- Sound ---
                    System.out.println("Switched selection to: " + clickedPos.toAlgebraicNotation());
                    // Re-calculate moves for the new selection
                    possibleMoves.clear();
                    possibleMoves = clickedPiece.getValidMoves(game.getBoard());
                } else {
                    SoundManager.playSound("illegal"); // --- Sound ---
                    System.out.println("Invalid move");
                    selectedPosition = null;
                    possibleMoves.clear();
                }
            }
            // Redraw board to reflect new positions or cleared selection
            updateBoard(boardGui);
        }
    }

    // --- Bot Move Logic ---
    private void makeBotMove(GridPane boardGui) {
        isBotTurn = true; // Lock Input

        new Thread(() -> {
            try {
                // Ensure toFEN() is implemented in Board.java!
                String fen = game.getBoard().toFEN(game.getCurrentTurn(), null, 0, 1);

                // --- Bot Skill Levels ---
                // Level 1: Weak (Rank 7)
                // Level 2: Medium (Rank 5)
                // Level 3: Strong (Rank 3, fast)
                // Level 4: Expert (Rank 1, thought out)

                int rank = 1; // Default to best move
                long thinkTime = 1000; // Default 1 second

                switch (selectedBotLevel) {
                    case 1 -> { rank = 7; thinkTime = 100; }
                    case 2 -> { rank = 5; thinkTime = 500; }
                    case 3 -> { rank = 3; thinkTime = 800; }
                    case 4 -> { rank = 1; thinkTime = 2000; }
                }

                String bestMove = bot.getRankedMove(fen, (int)thinkTime, rank);

                Platform.runLater(() -> {
                    if (bestMove != null) {
                        Position from = Position.fromAlgebraicNotation(bestMove.substring(0, 2));
                        Position to = Position.fromAlgebraicNotation(bestMove.substring(2, 4));

                        // Pre-calculate sound conditions for bot
                        Piece piece = game.getBoard().getPieceAt(from);
                        Piece target = game.getBoard().getPieceAt(to);
                        boolean isCapture = (target != null);
                        boolean isCastle = (piece.getType() == PieceType.KING && Math.abs(from.col() - to.col()) > 1);

                        game.playTurn(from, to);

                        // Play sound for bot move
                        if (game.isGameOver()) SoundManager.playSound("defeat"); // Player lost
                        else if (game.getStatusMessage().contains("check")) SoundManager.playSound("check");
                        else if (isCastle) SoundManager.playSound("castle");
                        else if (isCapture) SoundManager.playSound("capture");
                        else SoundManager.playSound("move");

                        updateBoard(boardGui);
                        updateHistory();

                        if (game.isGameOver()) {
                            showGameOverDialog();
                        }
                    }
                    isBotTurn = false; // Unlock Input
                });

            } catch (Exception e) {
                e.printStackTrace();
                isBotTurn = false;
            }
        }).start();
    }


    private void showGameOverDialog() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText(game.getStatusMessage());

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                showMenuScene(); // Return to menu
                this.game = null; // Reset game
            }
        });
    }

    private void updateBoard(GridPane boardGui) {
        // Forces the GridPane to only be as big as the tiles
        // This prevents the border from floating at the edge of the window
        boardGui.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        // Ensures tiles are centered and touching each other
        boardGui.setAlignment(Pos.CENTER);
        boardGui.setHgap(0);
        boardGui.setVgap(0);

        // Applies a black border around the entire grid
        boardGui.setStyle("-fx-border-color: black; -fx-border-width: 5; -fx-border-style: solid;");

        boardGui.getChildren().clear(); // Clear old visuals

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane tile = new StackPane();

                // Set size for visual consistency
                tile.setPrefSize(60, 60);

                boolean isLight = (row + col) % 2 == 0;

                // 1. Background Image
                ImageView backgroundSprite = ImageLoader.getBoardTile(isLight);
                backgroundSprite.setFitWidth(60);
                backgroundSprite.setFitHeight(60);
                tile.getChildren().add(backgroundSprite);

                // 2. Selection Highlight (Visual)
                Position currentPos = new Position(row, col);
                if (selectedPosition != null && selectedPosition.equals(currentPos)) {
                    Rectangle highlight = new Rectangle(60, 60, Color.rgb(255, 14, 225, 0.4));
                    tile.getChildren().add(highlight);
                }
                // highlight possible moves
                if (possibleMoves.contains(currentPos)) {
                    Rectangle moveHighlight = new Rectangle(60, 60, Color.rgb(255, 14, 225, 0.25));
                    tile.getChildren().add(moveHighlight);
                }


                // 3. Piece Rendering
                Piece piece = game.getBoard().getPieceAt(currentPos);
                if (piece != null) {
                    ImageView pieceSprite = ImageLoader.getPieceSprite(piece.getType(), piece.getColor());

                    // Exclude from layout calculations so the tile size remains fixed
                    pieceSprite.setManaged(false);

                    // Manual Positioning:
                    // Tile Height (60) - Sprite Height (~150) = -90.
                    // Added offset (-10) for visual centering -> -100.
                    pieceSprite.setLayoutX(0);
                    pieceSprite.setLayoutY(-100);

                    tile.getChildren().add(pieceSprite);
                }

                boardGui.add(tile, col, row);
            }
        }
    }

    private void togglePause() {
        SoundManager.playSound("pause"); // --- Sound ---
        if (!isPaused) {
            isPaused = true;
            timeline.pause();
            pauseOverlay.setVisible(true);
        } else {
            isPaused = false;
            timeline.play();
            pauseOverlay.setVisible(false);
        }
    }

    private StackPane buildPauseOverlay() {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.6);");

        VBox menu = new VBox(20);
        menu.setAlignment(Pos.CENTER);

        Button btnContinue = new Button("Continue");
        Button btnRestart = new Button("Restart");
        Button btnMain = new Button("Main Menu");

        btnContinue.setOnAction(e -> togglePause());
        btnRestart.setOnAction(e -> {
            SoundManager.playSound("click");
            timeline.stop();
            showBoardScene();
        });
        btnMain.setOnAction(e -> {
            SoundManager.playSound("click");
            timeline.stop();
            showMenuScene();
        });

        btnContinue.setStyle("-fx-font-size: 22px;");
        btnRestart.setStyle("-fx-font-size: 22px;");
        btnMain.setStyle("-fx-font-size: 22px;");

        menu.getChildren().addAll(btnContinue, btnRestart, btnMain);
        overlay.getChildren().add(menu);

        return overlay;
    }

}
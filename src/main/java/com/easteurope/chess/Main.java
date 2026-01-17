package com.easteurope.chess;

import com.easteurope.chess.controller.Stockfish;
import com.easteurope.chess.model.Piece;
import com.easteurope.chess.view.BackgroundEffect;
import com.easteurope.chess.view.ImageLoader;
import com.easteurope.chess.view.SoundManager; // Added SoundManager import
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import com.easteurope.chess.model.GameState;
import com.easteurope.chess.model.coreData.Position;
import com.easteurope.chess.model.coreData.PieceType; // Needed for sound logic

import javafx.util.Duration;

import java.util.Scanner;

public class Main extends Application {
    private GameState game;
    private Label whiteTimeLabel;
    private Label blackTimeLabel;
    private TextArea historyArea;
    private Stage window; // Reference to the main window for switching of the scenes
    private Position selectedPosition = null;
    private long selectedTimeMs = 60 * 1000;
    private com.easteurope.chess.model.coreData.Color selectedColor = com.easteurope.chess.model.coreData.Color.WHITE;
    private long selectedIncrementMs = 0;

    private Timeline timeline;

    private boolean isPaused = false;
    private StackPane pauseOverlay;

    private java.util.List<Position> possibleMoves = new java.util.ArrayList<>();

    // --- Bot Variables ---
    private int selectedBotLevel = 0; // 0 = PvP, 1-4 = Bot Level
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
            // Launches the JavaFX application
            launch(args);
        } else {
            // Launches the console logic
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

            // Ask for the best move from the starting position
            String startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
            String move = bot.getBestMove(startFen, 1000); // Think for 1 second

            System.out.println("Stockfish suggests: " + move);

            bot.stopEngine();
        } else {
            System.out.println("Failed to start engine.");
        }

        while (!game.isGameOver()) {
            game.getBoard().printBoard();
            System.out.println("STATUS: " + game.getStatusMessage());
            System.out.print("> "); // prompt move or undo

            String command = input.nextLine().trim();

            //input processing
            if (command.equalsIgnoreCase("exit") || command.equalsIgnoreCase("quit")) {
                break;
            }
            if (command.equalsIgnoreCase("undo")) {
                game.undo();
                continue;
            }

            // move parsing ( expects 'e2-e4' type format )
            if (isValidInputFormat(command)) {
                try {
                    String[] commandParts = command.split("-"); // splits the command on '-' into two elements and puts them in an array
                    Position from = Position.fromAlgebraicNotation(commandParts[0]); // for 'e2-e4' that would be e2
                    Position to = Position.fromAlgebraicNotation(commandParts[1]); // and e4

                    boolean success = game.playTurn(from, to);
                    if (success) {
                        System.out.println("Move from " + from.toAlgebraicNotation() + " to " + to.toAlgebraicNotation());
                    } else {
                        System.out.println("Move failed!");
                    }
                } catch (Exception e) {
                    System.out.println("Error parsing move: Use 'e2-e4' format."); //if format is wrong
                }
            } else {
                System.out.println("Unknown command! Type moves as 'e2-e4' or 'undo' to revert move.");//if command is not available
            }
        }
        //final render: shows final board layout and status message
        game.getBoard().printBoard();
        System.out.println("FINAL STATUS: " + game.getStatusMessage());

        System.out.println("Game Ended.");
        input.close();
    }

    //regex check for "a1-a2" format
    private static boolean isValidInputFormat(String input) {
        return input.matches("[a-h][1-8]-[a-h][1-8]");
    }

    // --- JAVAFX ---
    @Override
    public void start(Stage primaryStage) {
        this.window = primaryStage;
        window.setTitle("Chess TeamArbeit");

        // --- Initialize Sounds ---
        SoundManager.loadSounds();
        SoundManager.startMusic();

        window.setFullScreenExitHint("");

        Scene mainScene = new Scene(new StackPane(), 800, 600);
        window.setScene(mainScene);

        // Start with first scene (menu)
        showMenuScene();

        window.setFullScreen(true);
        window.show();
    }

    // SCREEN 1: START MENU
    private void showMenuScene() {
        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);

        ImageView logoImageView;

        Image logoImage = new Image(getClass().getResourceAsStream("/logo.png"));
        logoImageView = new ImageView(logoImage);
        logoImageView.setFitWidth(150);
        logoImageView.setPreserveRatio(true);


        // Create Up/Down Hover Animation
        // Duration specifies how long one direction takes (e.g., 2.5 seconds up)
        TranslateTransition hoverAnimation = new TranslateTransition(Duration.seconds(2.5), logoImageView);
        hoverAnimation.setFromY(12); // Start slightly below center
        hoverAnimation.setToY(-12);  // Move to slightly above center
        hoverAnimation.setCycleCount(TranslateTransition.INDEFINITE); // Repeat forever
        hoverAnimation.setAutoReverse(true); // Go back down after going up
        // EASE_BOTH makes the movement smoother at the turnaround points
        hoverAnimation.setInterpolator(Interpolator.EASE_BOTH);
        hoverAnimation.play();

        VBox.setMargin(logoImageView, new Insets(0, 0, 30, 0));

        // Make sure RetroByte.ttf is in src/main/resources/
        Font customFont = Font.loadFont(getClass().getResourceAsStream("/RetroByte.ttf"), 28);

        // getting the family name ensures we use the correct name in CSS (e.g., "RetroByte")
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

//        String btnStyleHover = """
//                -fx-background-color: white;
//                -fx-text-fill: #233447;
//                -fx-font-family: "%s";
//                -fx-font-size: 24px;
//                -fx-padding: 8 24;
//                -fx-border-color: white;
//                -fx-border-width: 2;
//                -fx-border-radius: 6;
//                -fx-background-radius: 6;
//            """.formatted(fontFamily);

        Button playBtn = new Button("PLAY");
        Button settingsBtn = new Button("SETTINGS");
        Button exitBtn = new Button("EXIT");

        playBtn.setStyle(btnStyle);
        settingsBtn.setStyle(btnStyle);
        exitBtn.setStyle(btnStyle);

        // ---  Sound on Click ---
        playBtn.setOnMouseEntered(e -> playBtn.setStyle(btnStyle));
        playBtn.setOnMouseExited(e -> playBtn.setStyle(btnStyle));

        settingsBtn.setOnMouseEntered(e -> settingsBtn.setStyle(btnStyle));
        settingsBtn.setOnMouseExited(e -> settingsBtn.setStyle(btnStyle));

        exitBtn.setOnMouseEntered(e -> exitBtn.setStyle(btnStyle));
        exitBtn.setOnMouseExited(e -> exitBtn.setStyle(btnStyle));

        playBtn.setOnAction(e -> {
            SoundManager.playSound("click");
            showSetupScene();
        });

        exitBtn.setOnAction(e -> {
            SoundManager.playSound("click");
            window.close();
        });

        // --- Music Toggle Button ---
        Button musicBtn = new Button("MUSIC: ON");
        musicBtn.setStyle(btnStyle);
        musicBtn.setOnAction(e -> {
            SoundManager.toggleMusic();
            SoundManager.playSound("click");
            musicBtn.setText(SoundManager.isMusicPlaying() ? "MUSIC: ON" : "MUSIC: OFF");
        });
        musicBtn.setOnMouseEntered(e -> musicBtn.setStyle(btnStyle));
        musicBtn.setOnMouseExited(e -> musicBtn.setStyle(btnStyle));

        settingsBtn.setOnAction(e -> {
            SoundManager.playSound("click");
            showSettingsScene();
        });

        // 1. Add the buttons to the VBox
        content.getChildren().addAll(logoImageView, playBtn, settingsBtn, musicBtn, exitBtn);

        // 2. Create the Animated Background
        Pane animatedBg = BackgroundEffect.createAnimatedBackground();

        // 3. Stack the UI on top of the Background
        StackPane root = new StackPane();

        // Layer 1: The background (at the back)
        root.getChildren().add(animatedBg);

        // Layer 2: The VBox containing the buttons (at the front)
        root.getChildren().add(content);

        window.getScene().setRoot(root);
    }

    // SCREEN 2: GAME SETUP
    private void showSetupScene() {
        // 1. Layout Container
        VBox layout = new VBox(20); // Spacing between rows
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        // --- STYLES ---
        String titleStyle = "-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;";
        String sectionStyle = "-fx-text-fill: white; -fx-font-size: 16px;";

        // --- TOP: Back Button ---
        Button backBtn = new Button("⮌");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 22px;");
        backBtn.setOnAction(e -> showMenuScene());

        HBox backBox = new HBox(backBtn);
        backBox.setAlignment(Pos.TOP_RIGHT);

        // --- 1. Color Selection ---
        HBox colorBox = chooseColor();

        // --- 2. Bot Selection ---
        Label botLabel = new Label("Bot Selection");
        botLabel.setStyle(titleStyle);

        HBox bots = new HBox(20);
        bots.setAlignment(Pos.CENTER);

        // Logic for Bot Buttons
        java.util.List<Button> botButtons = new java.util.ArrayList<>();
        Button pvpBtn = new Button("PvP");
        pvpBtn.setPrefSize(80, 80);
        // Default Selected Style
        pvpBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        pvpBtn.setOnAction(e -> {
            selectedBotLevel = 0;
            SoundManager.playSound("click");
            pvpBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
            for (Button b : botButtons) b.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        });
        bots.getChildren().add(pvpBtn);

        for (int i = 1; i <= 4; i++) {
            final int level = i;
            Button botBtn = new Button("BOT " + i);
            botBtn.setPrefSize(80, 80);
            botBtn.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
            botButtons.add(botBtn);

            botBtn.setOnAction(e -> {
                selectedBotLevel = level;
                SoundManager.playSound("click");
                pvpBtn.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
                for (Button b : botButtons) {
                    if (b == botBtn) b.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                    else b.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
                }
            });
            bots.getChildren().add(botBtn);
        }

        // --- 3. Time Control ---
        Label timeLabel = new Label("Time Control");
        timeLabel.setStyle(sectionStyle);

        HBox timeControls = new HBox(20);
        timeControls.setAlignment(Pos.CENTER);

        Button btn1 = new Button("1m");
        Button btn5 = new Button("5m");
        Button btn10 = new Button("10m");

        // Default Styles
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

        // --- 4. Increment Control ---
        Label incLabel = new Label("Increment");
        incLabel.setStyle(sectionStyle);

        HBox incControls = new HBox(20);
        incControls.setAlignment(Pos.CENTER);

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

        incControls.getChildren().addAll(inc0, inc5, inc10);

        // --- 5. Start Button ---
        Button startBtn = new Button("START");
        startBtn.setPrefSize(120, 40);
        startBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        startBtn.setOnAction(e -> {
            SoundManager.playSound("start");
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

        // --- ASSEMBLE THE LAYOUT ---
        layout.getChildren().addAll(
                backBox,
                colorBox,
                botLabel,
                bots,
                timeLabel,
                timeControls,
                incLabel,
                incControls,
                startBtn
        );

        VBox.setMargin(startBtn, new Insets(20, 0, 0, 0));

        // --- ANIMATION / BACKGROUND ---
        Pane animatedBg = BackgroundEffect.createAnimatedBackground();
        StackPane root = new StackPane();
        root.getChildren().add(animatedBg); // Layer 1 (Background)
        root.getChildren().add(layout);     // Layer 2 (Buttons)

        window.getScene().setRoot(root);
    }

    // SCREEN 3: GAME BOARD
    private void showBoardScene() {
        game = new GameState(selectedTimeMs, selectedColor, selectedIncrementMs);

        BorderPane uiLayout = new BorderPane();
        uiLayout.setStyle("-fx-background-color: transparent;");

        // --- GAME STACK (Board Layer) ---
        StackPane boardStack = new StackPane();

        // LAYER 1: VISUALS (Bottom)
        // Only renders images. Mouse events are disabled here.
        GridPane boardGui = new GridPane();
        boardGui.setAlignment(Pos.CENTER);
        boardGui.setMouseTransparent(true); // CRITICAL: Makes all images "ghosts" to the mouse.

        // LAYER 2: INPUT (Top)
        // An invisible grid that captures clicks.
        GridPane inputGrid = new GridPane();
        inputGrid.setAlignment(Pos.CENTER);
        setupInputLayer(inputGrid, boardGui); // Helper method to build the invisible grid

        // Stack them: Input goes ON TOP of Visuals
        boardStack.getChildren().addAll(boardGui, inputGrid);

        // Initial render of the board visuals
        updateBoard(boardGui);

        uiLayout.setCenter(boardStack);
        // ---- PAUSE OVERLAY ----
        pauseOverlay = buildPauseOverlay();
        pauseOverlay.setVisible(false);
        boardStack.getChildren().add(pauseOverlay);


        // --- HISTORY ---
        historyArea = new TextArea();
        historyArea.setEditable(false);
        historyArea.setPrefHeight(400);

        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setPrefWidth(200);

        Label historyLabel = new Label("Move Order");
        historyLabel.setTextFill(Color.WHITE);

        sidebar.getChildren().addAll(historyLabel, historyArea);
        uiLayout.setRight(sidebar);


        // --- TIMER ---
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

        window.getScene().setRoot(root);
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

    // Setting Scene
    private void showSettingsScene() {
        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: linear-gradient(to bottom, #233447, #1b2838);");

        Label title = new Label("SETTINGS");
        title.setStyle("-fx-font-size: 48px; -fx-text-fill: white; -fx-font-weight: bold;");

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

        layout.getChildren().addAll(title, backBtn);

        Scene scene = new Scene(layout, 900, 700);
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
                // ---Only play illegal sound if not switching selection ---
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

                // Delay thinking based on difficulty (Level 1=fast, Level 4=slower)
                String bestMove = bot.getRankedMove(fen, selectedBotLevel * 300, 1);

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
                    Rectangle highlight = new Rectangle(60, 60, Color.rgb(0, 255, 0, 0.4));
                    tile.getChildren().add(highlight);
                }
                // highlight possible moves
                if (possibleMoves.contains(currentPos)) {
                    Rectangle moveHighlight = new Rectangle(60, 60, Color.rgb(0, 255, 0, 0.25));
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
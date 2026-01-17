package com.easteurope.chess;

import com.easteurope.chess.controller.Stockfish;
import com.easteurope.chess.model.Piece;
import com.easteurope.chess.view.BackgroundEffect;
import com.easteurope.chess.view.ImageLoader;
import com.easteurope.chess.view.SoundManager;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.easteurope.chess.model.GameState;
import com.easteurope.chess.model.coreData.Position;
import com.easteurope.chess.model.coreData.PieceType;

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
    private StackPane gameOverOverlay;
    private Text gameOverMessage;

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
        TranslateTransition hoverAnimation = new TranslateTransition(Duration.seconds(2.5), logoImageView);
        hoverAnimation.setFromY(12); // Start slightly below center
        hoverAnimation.setToY(-12);  // Move to slightly above center
        hoverAnimation.setCycleCount(TranslateTransition.INDEFINITE); // Repeat forever
        hoverAnimation.setAutoReverse(true); // Go back down after going up
        hoverAnimation.setInterpolator(Interpolator.EASE_BOTH);
        hoverAnimation.play();

        VBox.setMargin(logoImageView, new Insets(0, 0, 30, 0));

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

        settingsBtn.setOnAction(e -> {
            SoundManager.playSound("click");
            showSettingsScene();
        });

        content.getChildren().addAll(logoImageView, playBtn, settingsBtn, exitBtn);

        Pane animatedBg = BackgroundEffect.createAnimatedBackground();
        StackPane root = new StackPane();
        root.getChildren().add(animatedBg);
        root.getChildren().add(content);

        window.getScene().setRoot(root);
    }

    // SCREEN 2: GAME SETUP
    private void showSetupScene() {
        VBox layout = new VBox(30);

        String titleStyle = "-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;";
        String sectionStyle = "-fx-text-fill: white; -fx-font-size: 16px;";

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

        HBox bots = new HBox(20);
        bots.setAlignment(Pos.CENTER);

        java.util.List<Button> botButtons = new java.util.ArrayList<>();


        Button pvpBtn = new Button("PvP");
        pvpBtn.setPrefSize(80, 80);
        pvpBtn.setStyle("-fx-background-color: #533c98; -fx-text-fill: white;");
        pvpBtn.setOnAction(e -> {
            selectedBotLevel = 0;
            SoundManager.playSound("click");
            pvpBtn.setStyle("-fx-background-color: #533c98; -fx-text-fill: white;");
            for (Button b : botButtons) b.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        });
        bots.getChildren().add(pvpBtn);


        // Bot portraits

        for (int i = 1; i <= 4; i++) {
            final int level = i;

            String imagePath = "/images/bot" + i + ".png";
            Image image = new Image(getClass().getResource(imagePath).toString());
            ImageView imageView = new ImageView(image);

            imageView.setFitWidth(90);
            imageView.setFitHeight(90);
            imageView.setPreserveRatio(true);

            // Bot names, can be changed anytime
            String botName = switch (i) {
                case 1 -> "name1";
                case 2 -> "name2";
                case 3 -> "name3";
                case 4 -> "name4";
                default -> "";
            };

            Button portraitBtn = new Button(botName);
            portraitBtn.setGraphic(imageView);
            portraitBtn.setContentDisplay(ContentDisplay.TOP); // image above text
            portraitBtn.setGraphicTextGap(5);
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

        Label timeLabel = new Label("Time Control");
        timeLabel.setStyle(sectionStyle);
        layout.getChildren().add(timeLabel);

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
        layout.getChildren().add(timeControls);

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

        startBtn.setOnAction(e -> {
            SoundManager.playSound("start");
            if (selectedBotLevel > 0) {
                bot = new Stockfish();
                if (bot.startEngine()) {
                    System.out.println("Engine Started. Level: " + selectedBotLevel);
                } else {
                    System.out.println("Engine Failed.");
                    selectedBotLevel = 0;
                }
            }
            showBoardScene();
        });

        layout.getChildren().add(startBtn);

        Pane animatedBg = BackgroundEffect.createAnimatedBackground();
        StackPane root = new StackPane();
        root.getChildren().add(animatedBg);
        root.getChildren().add(layout);

        window.getScene().setRoot(root);
    }

    // SCREEN 3: GAME BOARD
    private void showBoardScene() {
        game = new GameState(selectedTimeMs, com.easteurope.chess.model.coreData.Color.WHITE, selectedIncrementMs);


        // --- LOAD FONTS  ---

        // Font 1: RetroByte (For Pause Button) - Size 24
        Font retroFont = Font.loadFont(getClass().getResourceAsStream("/RetroByte.ttf"), 24);
        String retroFamily = (retroFont != null) ? retroFont.getFamily() : "Arial";

        // Font 2: Minecraftia-Regular (For Timers) - Size 26
        Font mineFont = Font.loadFont(getClass().getResourceAsStream("/Minecraftia-Regular.ttf"), 26);
        String mineFamily = (mineFont != null) ? mineFont.getFamily() : "Verdana";

        // Style for Timers
        String timerStyle = """
                    -fx-text-fill: white; 
                    -fx-font-family: "%s"; 
                    -fx-font-size: 26px;
                """.formatted(mineFamily);

        // Style for Pause Button
        String pauseBtnStyle = """
                    -fx-background-color: transparent;
                    -fx-text-fill: white;
                    -fx-font-family: "%s";
                    -fx-font-size: 24px;
                    -fx-padding: 10 10;
                    -fx-border-color: white;
                    -fx-border-width: 2;
                    -fx-border-radius: 6;
                    -fx-background-radius: 6;
                """.formatted(retroFamily);

        String pauseBtnHoverStyle = """
                    -fx-background-color: white;
                    -fx-text-fill: #233447;
                    -fx-font-family: "%s";
                    -fx-font-size: 24px;
                    -fx-padding: 10 10;
                    -fx-border-color: white;
                    -fx-border-width: 2;
                    -fx-border-radius: 6;
                    -fx-background-radius: 6;
                """.formatted(retroFamily);


        // --- 2. SETUP BOARD ---
        StackPane boardStack = new StackPane();
        GridPane boardGui = new GridPane();
        boardGui.setAlignment(Pos.CENTER);
        boardGui.setMouseTransparent(true);

        GridPane inputGrid = new GridPane();
        inputGrid.setAlignment(Pos.CENTER);
        setupInputLayer(inputGrid, boardGui);

        boardStack.getChildren().addAll(boardGui, inputGrid);
        updateBoard(boardGui);
        if (selectedColor == com.easteurope.chess.model.coreData.Color.BLACK) {
            if (selectedBotLevel > 0) {
                isBotTurn = true;
                Platform.runLater(() -> makeBotMove(boardGui));
            }
        }

        // --- 3. SETUP TIMERS (Left of Board) ---
        whiteTimeLabel = new Label();
        blackTimeLabel = new Label();

        whiteTimeLabel.setText(formatTime(game.getWhiteTimeMs()));
        blackTimeLabel.setText(formatTime(game.getBlackTimeMs()));

        whiteTimeLabel.setStyle(timerStyle);
        blackTimeLabel.setStyle(timerStyle);

        // Vertical Box for Timers
        VBox timerBox = new VBox();
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        timerBox.getChildren().addAll(blackTimeLabel, spacer, whiteTimeLabel);

        timerBox.setPrefHeight(600);
        timerBox.setMaxHeight(600);

        timerBox.setMinWidth(150);
        timerBox.setPrefWidth(150);

        timerBox.setAlignment(Pos.TOP_RIGHT);
        timerBox.setPadding(new Insets(0, 30, 0, 0));


        // --- 4. CENTER AREA ---
        HBox centerContent = new HBox();
        centerContent.setAlignment(Pos.CENTER);
        centerContent.getChildren().addAll(timerBox, boardStack);


        // --- 5. PAUSE BUTTON (Top Left) ---
        Button pauseBtn = new Button(" II ");
        pauseBtn.setStyle(pauseBtnStyle);

        pauseBtn.setOnMouseEntered(e -> pauseBtn.setStyle(pauseBtnHoverStyle));
        pauseBtn.setOnMouseExited(e -> pauseBtn.setStyle(pauseBtnStyle));

        pauseBtn.setOnAction(e -> togglePause());

        StackPane topBar = new StackPane(pauseBtn);
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPadding(new Insets(10));


        // --- 6. SIDEBAR (Right) ---
        historyArea = new TextArea();
        historyArea.setEditable(false);
        historyArea.setPrefHeight(600);
        historyArea.setWrapText(true);

        historyArea.setStyle("""
                    -fx-control-inner-background: transparent;
                    -fx-background-color: transparent;
                    -fx-text-fill: white;
                    -fx-font-family: 'Consolas', 'Monospaced';
                    -fx-font-size: 18px;
                    -fx-highlight-fill: transparent;
                    -fx-highlight-text-fill: white;
                """);
        historyArea.getStylesheets().add("data:text/css," +
                ".text-area .scroll-pane { -fx-background-color: transparent; -fx-hbar-policy: never; -fx-vbar-policy: never; }" +
                ".text-area .scroll-pane .viewport { -fx-background-color: transparent; }" +
                ".text-area .content { -fx-background-color: transparent; }"
        );

        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10, 15, 10, 15));
        sidebar.setPrefWidth(350);
        sidebar.setStyle("-fx-background-color: rgba(0, 0, 0, 0.2);");
        sidebar.setAlignment(Pos.TOP_CENTER);

        Label historyLabel = new Label("");
        historyLabel.setTextFill(Color.WHITE);

        sidebar.getChildren().addAll(historyLabel, historyArea);
        VBox.setVgrow(historyArea, Priority.ALWAYS);


        // --- 7. LAYOUT COMPOSITION ---
        BorderPane gameLayout = new BorderPane();
        gameLayout.setTop(topBar);
        gameLayout.setCenter(centerContent);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: transparent;");

        mainLayout.setCenter(gameLayout);
        mainLayout.setRight(sidebar);


        // --- FINAL ASSEMBLY ---
        pauseOverlay = buildPauseOverlay();
        pauseOverlay.setVisible(false);

        Pane animatedBg = BackgroundEffect.createAnimatedBackground();
        StackPane root = new StackPane();

        root.getChildren().add(animatedBg);
        root.getChildren().add(mainLayout);
        root.getChildren().add(pauseOverlay);

        window.getScene().setRoot(root);
        window.getScene().setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ESCAPE -> togglePause();
            }
        });




        // Game Over Overlay
        gameOverMessage = new Text();
        gameOverMessage.setFill(Color.WHITE);
        gameOverMessage.setFont(Font.font("Arial", 64));

        Button restartBtn = new Button("Restart");
        Button mainMenuBtn = new Button("Main Menu");

        restartBtn.setOnAction(e -> {
            gameOverOverlay.setVisible(false);
            timeline.stop();
            showBoardScene(); // restart game
        });

        mainMenuBtn.setOnAction(e -> {
            gameOverOverlay.setVisible(false);
            timeline.stop();
            showMenuScene(); // back to menu
        });

        VBox menuBox = new VBox(20, restartBtn, mainMenuBtn);
        menuBox.setAlignment(Pos.CENTER);

        VBox overlayContent = new VBox(40, gameOverMessage, menuBox);
        overlayContent.setAlignment(Pos.CENTER);

        gameOverOverlay = new StackPane(overlayContent);
        gameOverOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.75);");
        gameOverOverlay.setVisible(false);
        gameOverOverlay.setAlignment(Pos.CENTER);


        root.getChildren().add(gameOverOverlay);

        // --- TIMELINE ---
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    game.tickTimer();
                    whiteTimeLabel.setText(formatTime(game.getWhiteTimeMs()));
                    blackTimeLabel.setText(formatTime(game.getBlackTimeMs()));

                    if (game.isGameOver()) {
                        timeline.stop();
                        SoundManager.playSound("defeat");
                        gameOverMessage.setText(game.getStatusMessage());
                        gameOverOverlay.setVisible(true);
                    }
                })
        );

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

    }

    private void updateBoard(GridPane boardGui) {
        boardGui.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        boardGui.setAlignment(Pos.CENTER);
        boardGui.setHgap(0);
        boardGui.setVgap(0);
        boardGui.setStyle("-fx-border-color: black; -fx-border-width: 5; -fx-border-style: solid;");

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.6));
        shadow.setRadius(70);
        shadow.setSpread(0.4);
        shadow.setOffsetX(10);
        shadow.setOffsetY(10);
        boardGui.setEffect(shadow);

        boardGui.getChildren().clear();

        int tileSize = 75;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane tile = new StackPane();
                tile.setPrefSize(tileSize, tileSize);

                boolean isLight = (row + col) % 2 == 0;

                ImageView backgroundSprite = ImageLoader.getBoardTile(isLight);
                backgroundSprite.setFitWidth(tileSize);
                backgroundSprite.setFitHeight(tileSize);
                tile.getChildren().add(backgroundSprite);

                Position currentPos = new Position(row, col);
                if (selectedPosition != null && selectedPosition.equals(currentPos)) {
                    Rectangle highlight = new Rectangle(tileSize, tileSize, Color.rgb(255, 0, 222, 0.4));
                    tile.getChildren().add(highlight);
                }
                if (possibleMoves.contains(currentPos)) {
                    Rectangle moveHighlight = new Rectangle(tileSize, tileSize, Color.rgb(255, 0, 222, 0.25));
                    tile.getChildren().add(moveHighlight);
                }

                Piece piece = game.getBoard().getPieceAt(currentPos);
                if (piece != null) {
                    ImageView pieceSprite = ImageLoader.getPieceSprite(piece.getType(), piece.getColor());
                    pieceSprite.setManaged(false);

                    pieceSprite.setFitWidth(tileSize);
                    pieceSprite.setPreserveRatio(true);

                    pieceSprite.setLayoutX(0);
                    pieceSprite.setLayoutY(-125);

                    tile.getChildren().add(pieceSprite);
                }

                boardGui.add(tile, col, row);
            }
        }
    }

    private void setupInputLayer(GridPane inputGrid, GridPane boardGui) {
        // CHANGED: Match input layer to visual tile size (75)
        int tileSize = 75;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Rectangle clickArea = new Rectangle(tileSize, tileSize, Color.TRANSPARENT);
                final int r = row;
                final int c = col;
                clickArea.setOnMouseClicked(e -> handleTileClick(r, c, boardGui));
                inputGrid.add(clickArea, col, row);
            }
        }
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

    private void showSettingsScene() {
        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);

        Font customFont = Font.loadFont(getClass().getResourceAsStream("/RetroByte.ttf"), 28);
        String fontFamily = (customFont != null) ? customFont.getFamily() : "Arial";

        Label title = new Label("SETTINGS");
        title.setStyle("""
                    -fx-font-family: "%s";
                    -fx-font-size: 64px;
                    -fx-text-fill: white;
                    -fx-font-weight: bold;
                """.formatted(fontFamily));

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

        Label masterLabel = new Label("Master Volume");
        masterLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        Slider masterSlider = new Slider(0, 100, SoundManager.getMasterVolume() * 100);
        masterSlider.setStyle(sliderStyle);
        masterSlider.setShowTickLabels(true);
        masterSlider.setShowTickMarks(true);
        masterSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            SoundManager.setMasterVolume(newVal.doubleValue() / 100.0);
        });

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

        Button backBtn = new Button("BACK TO MENU");
        backBtn.setStyle(btnStyle);

        backBtn.setOnAction(e -> {
            SoundManager.playSound("click");
            showMenuScene();
        });

        content.getChildren().addAll(title, volumeBox, backBtn);

        Pane animatedBg = BackgroundEffect.createAnimatedBackground();
        StackPane root = new StackPane();
        root.getChildren().add(animatedBg);
        root.getChildren().add(content);

        window.getScene().setRoot(root);
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

            if (move.movedPiece().getColor()
                    == com.easteurope.chess.model.coreData.Color.WHITE) {

                whiteMove = moveText;

                sb.append(String.format(
                        "%2d. %-10s",
                        moveNumber,
                        whiteMove
                ));
            }
            else {
                sb.append(String.format(
                        " %-10s%n",
                        moveText
                ));

                moveNumber++;
                whiteMove = null;
            }
        }

        if (whiteMove != null) {
            sb.append("\n");
        }

        historyArea.setText(sb.toString());
    }

    private void handleTileClick(int row, int col, GridPane boardGui) {
        if (isBotTurn) return;

        Position clickedPos = new Position(row, col);

        System.out.println("Clicked: " + clickedPos.toAlgebraicNotation());

        if (selectedPosition == null) {
            Piece piece = game.getBoard().getPieceAt(clickedPos);

            if (piece != null && piece.getColor() == game.getCurrentTurn()) {
                selectedPosition = clickedPos;
                SoundManager.playSound("start");
                System.out.println("Selected: " + clickedPos.toAlgebraicNotation());
                possibleMoves.clear();
                possibleMoves = piece.getValidMoves(game.getBoard());
                updateBoard(boardGui);
            }
        }
        else {
            if (clickedPos.equals(selectedPosition)) {
                selectedPosition = null;
                possibleMoves.clear();
                updateBoard(boardGui);
                return;
            }

            Piece piece = game.getBoard().getPieceAt(selectedPosition);
            Piece targetPiece = game.getBoard().getPieceAt(clickedPos);
            boolean isCapture = (targetPiece != null);
            boolean isCastle = (piece.getType() == PieceType.KING && Math.abs(selectedPosition.col() - clickedPos.col()) > 1);
            boolean isPromotion = (piece.getType() == PieceType.PAWN && (clickedPos.row() == 0 || clickedPos.row() == 7));

            boolean success = game.playTurn(selectedPosition, clickedPos);
            System.out.println("GameOver? " + game.isGameOver() + " status=" + game.getStatusMessage());

            if (piece != null) {
                possibleMoves = piece.getValidMoves(game.getBoard());
            }
            if (success) {
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
                selectedPosition = null;

                updateBoard(boardGui);

                updateHistory();

                if (game.isGameOver()) {
                    showGameOverDialog();
                    return;
                }

                if (selectedBotLevel > 0 && !game.isGameOver()) {
                    makeBotMove(boardGui);
                }

                return;
            } else {
                Piece clickedPiece = game.getBoard().getPieceAt(clickedPos);
                if (clickedPiece != null && clickedPiece.getColor() == game.getCurrentTurn()) {
                    selectedPosition = clickedPos;
                    SoundManager.playSound("start");
                    System.out.println("Switched selection to: " + clickedPos.toAlgebraicNotation());
                    possibleMoves.clear();
                    possibleMoves = clickedPiece.getValidMoves(game.getBoard());
                } else {
                    SoundManager.playSound("illegal");
                    System.out.println("Invalid move");
                    selectedPosition = null;
                    possibleMoves.clear();
                }
            }
            updateBoard(boardGui);
        }
    }

    private void makeBotMove(GridPane boardGui) {
        isBotTurn = true;

        new Thread(() -> {
            try {
                String fen = game.getBoard().toFEN(game.getCurrentTurn(), null, 0, 1);

                int rank = 1;
                long thinkTime = 1000;

                switch (selectedBotLevel) {
                    case 1 -> {
                        rank = 7;
                        thinkTime = 100;
                    }
                    case 2 -> {
                        rank = 5;
                        thinkTime = 500;
                    }
                    case 3 -> {
                        rank = 3;
                        thinkTime = 800;
                    }
                    case 4 -> {
                        rank = 1;
                        thinkTime = 2000;
                    }
                }

                String bestMove = bot.getRankedMove(fen, (int) thinkTime, rank);

                Platform.runLater(() -> {
                    if (bestMove != null) {
                        Position from = Position.fromAlgebraicNotation(bestMove.substring(0, 2));
                        Position to = Position.fromAlgebraicNotation(bestMove.substring(2, 4));

                        Piece piece = game.getBoard().getPieceAt(from);
                        Piece target = game.getBoard().getPieceAt(to);
                        boolean isCapture = (target != null);
                        boolean isCastle = (piece.getType() == PieceType.KING && Math.abs(from.col() - to.col()) > 1);

                        game.playTurn(from, to);

                        if (game.isGameOver()) SoundManager.playSound("defeat");
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
                    isBotTurn = false;
                });

            } catch (Exception e) {
                e.printStackTrace();
                isBotTurn = false;
            }
        }).start();
    }


    private void showGameOverDialog() {

        if (game.isGameOver()) {
            timeline.stop(); // stop timers
            gameOverMessage.setText(game.getStatusMessage());
            gameOverOverlay.setVisible(true);
        }
    }

    private void togglePause() {
        SoundManager.playSound("pause");
        if (!isPaused) {
            isPaused = true;
            timeline.pause();
            pauseOverlay.setVisible(true);
        } else {
            isPaused = false;

            game.resetLastMoveTimestamp();

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
package com.easteurope.chess.view;

import com.easteurope.chess.Main;
import com.easteurope.chess.controller.Stockfish;
import com.easteurope.chess.model.GameConfig;
import com.easteurope.chess.model.GameState;
import com.easteurope.chess.model.Piece;
import com.easteurope.chess.model.coreData.PieceType;
import com.easteurope.chess.model.coreData.Position;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameScreen {

    private final Main mainApp;
    private final GameConfig config;

    // --- State Variables ---
    private GameState game;
    private Label whiteTimeLabel;
    private Label blackTimeLabel;
    private TextArea historyArea;
    private Position selectedPosition = null;

    // Captured Pieces Labels
    private Label whiteCapturedLabel; // Shows pieces captured BY White
    private Label blackCapturedLabel; // Shows pieces captured BY Black

    private long selectedTimeMs;
    private com.easteurope.chess.model.coreData.Color selectedColor;
    private long selectedIncrementMs;
    private int selectedBotLevel;

    private StackPane gameOverOverlay;
    private Text gameOverMessage;
    private Timeline timeline;
    private boolean isPaused = false;
    private StackPane pauseOverlay;
    private List<Position> possibleMoves = new ArrayList<>();

    // Bot
    private Stockfish bot;
    private boolean isBotTurn = false;

    // UI Reference
    private GridPane boardGui;
    private DropShadow boardShadow;

    public GameScreen(Main mainApp, GameConfig config) {
        this.mainApp = mainApp;
        this.config = config;

        this.selectedTimeMs = config.timeMs();
        this.selectedIncrementMs = config.incrementMs();
        this.selectedColor = config.playerColor();
        this.selectedBotLevel = config.botLevel();
    }

    public void startGameLoop() {
        if (timeline != null) {
            timeline.play();
        }
    }

    public StackPane getView() {
        game = new GameState(selectedTimeMs, com.easteurope.chess.model.coreData.Color.WHITE, selectedIncrementMs);

        if (selectedBotLevel > 0) {
            bot = new Stockfish();
            if (bot.startEngine()) {
                System.out.println("Engine Started. Level: " + selectedBotLevel);
            } else {
                System.out.println("Engine Failed.");
                selectedBotLevel = 0;
            }
        }

        // --- LOAD FONTS ---
        Font retroFont = Font.loadFont(getClass().getResourceAsStream("/RetroByte.ttf"), 24);
        String retroFamily = (retroFont != null) ? retroFont.getFamily() : "Arial";

        Font mineFont = Font.loadFont(getClass().getResourceAsStream("/Minecraftia-Regular.ttf"), 26);
        String mineFamily = (mineFont != null) ? mineFont.getFamily() : "Verdana";

        Font chessFont = Font.loadFont(getClass().getResourceAsStream("/TT_USUAL.TTF"), 32);
        String chessFamily = (chessFont != null) ? chessFont.getFamily() : "Serif";

        String timerStyle = """
                    -fx-text-fill: white; 
                    -fx-font-family: "%s"; 
                    -fx-font-size: 26px;
                """.formatted(mineFamily);

        String capturedStyle = """
                    -fx-text-fill: white; 
                    -fx-font-family: "%s"; 
                    -fx-font-size: 32px;
                """.formatted(chessFamily);

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

        // --- 1. SETUP CAPTURED PIECES (Initialize these FIRST) ---
        whiteCapturedLabel = new Label("");
        blackCapturedLabel = new Label("");

        whiteCapturedLabel.setStyle(capturedStyle);
        blackCapturedLabel.setStyle(capturedStyle);

        whiteCapturedLabel.setWrapText(true);
        blackCapturedLabel.setWrapText(true);
        whiteCapturedLabel.setMaxWidth(150);
        blackCapturedLabel.setMaxWidth(150);

        VBox capturedBox = new VBox();
        Region capSpacer = new Region();
        VBox.setVgrow(capSpacer, Priority.ALWAYS);

        capturedBox.getChildren().addAll(blackCapturedLabel, capSpacer, whiteCapturedLabel);

        capturedBox.setPrefHeight(600);
        capturedBox.setMaxHeight(600);
        capturedBox.setMinWidth(150);
        capturedBox.setPrefWidth(150);
        capturedBox.setAlignment(Pos.TOP_LEFT);
        capturedBox.setPadding(new Insets(0, 0, 0, 30));

        // --- 2. SETUP BOARD ---
        StackPane boardStack = new StackPane();
        this.boardGui = new GridPane();
        boardGui.setAlignment(Pos.CENTER);
        boardGui.setMouseTransparent(true);

        // --- SHADOW SETUP ---
        this.boardShadow = new DropShadow();
        boardShadow.setColor(Color.rgb(0, 0, 0, 0.9)); // More opaque
        boardShadow.setRadius(70);
        boardShadow.setSpread(0.4);
        boardShadow.setOffsetX(10);
        boardShadow.setOffsetY(10);
        boardGui.setEffect(boardShadow);

        // Animation
        Timeline shadowAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(boardShadow.offsetYProperty(), 10)),
                new KeyFrame(Duration.seconds(2.0), new KeyValue(boardShadow.offsetYProperty(), 25))
        );
        shadowAnimation.setAutoReverse(true);
        shadowAnimation.setCycleCount(Animation.INDEFINITE);
        shadowAnimation.play();

        GridPane inputGrid = new GridPane();
        inputGrid.setAlignment(Pos.CENTER);
        setupInputLayer(inputGrid, boardGui);

        boardStack.getChildren().addAll(boardGui, inputGrid);

        // --- SAFE TO UPDATE BOARD NOW ---
        updateBoard(boardGui);

        if (selectedColor == com.easteurope.chess.model.coreData.Color.BLACK) {
            if (selectedBotLevel > 0) {
                isBotTurn = true;
                Platform.runLater(() -> makeBotMove(boardGui));
            }
        }

        // --- 3. SETUP TIMERS ---
        whiteTimeLabel = new Label();
        blackTimeLabel = new Label();

        whiteTimeLabel.setText(formatTime(game.getWhiteTimeMs()));
        blackTimeLabel.setText(formatTime(game.getBlackTimeMs()));

        whiteTimeLabel.setStyle(timerStyle);
        blackTimeLabel.setStyle(timerStyle);

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
        centerContent.getChildren().addAll(timerBox, boardStack, capturedBox);

        // --- 5. PAUSE BUTTON ---
        Button pauseBtn = new Button(" II ");
        pauseBtn.setStyle(pauseBtnStyle);

        pauseBtn.setOnMouseEntered(e -> pauseBtn.setStyle(pauseBtnHoverStyle));
        pauseBtn.setOnMouseExited(e -> pauseBtn.setStyle(pauseBtnStyle));
        pauseBtn.setOnAction(e -> togglePause());

        StackPane topBar = new StackPane(pauseBtn);
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPadding(new Insets(20));

        // --- 6. SIDEBAR ---
        historyArea = new TextArea();
        historyArea.setEditable(false);
        historyArea.setPrefHeight(600);
        historyArea.setWrapText(true);

        // CHANGED: Use mineFamily (Minecraftia) and increased spacing/size slightly for readability
        historyArea.setStyle("""
                    -fx-control-inner-background: transparent;
                    -fx-background-color: transparent;
                    -fx-text-fill: white;
                    -fx-font-family: "%s"; 
                    -fx-font-size: 18px;
                    -fx-highlight-fill: transparent;
                    -fx-highlight-text-fill: white;
                """.formatted(mineFamily));

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
        BorderPane gameContentLayout = new BorderPane();
        gameContentLayout.setTop(topBar);
        gameContentLayout.setCenter(centerContent);
        gameContentLayout.setStyle("-fx-background-color: transparent;");

        BorderPane mainLayout = new BorderPane();
        mainLayout.setCenter(gameContentLayout);
        mainLayout.setRight(sidebar);
        mainLayout.setStyle("-fx-background-color: transparent;");

        // --- FINAL ASSEMBLY ---
        pauseOverlay = buildPauseOverlay();
        pauseOverlay.setVisible(false);

        Pane animatedBg = BackgroundEffect.createAnimatedBackground();
        StackPane root = new StackPane();

        root.getChildren().add(animatedBg);
        root.getChildren().add(mainLayout);
        root.getChildren().add(pauseOverlay);

        // --- GAME OVER OVERLAY ---
        gameOverMessage = new Text();
        gameOverMessage.setFill(Color.WHITE);
        gameOverMessage.setFont(Font.font("Arial", 64));

        Button restartBtn = new Button("Restart");
        Button mainMenuBtn = new Button("Main Menu");

        restartBtn.setOnAction(e -> {
            gameOverOverlay.setVisible(false);
            timeline.stop();
            mainApp.startGame(config);
        });

        mainMenuBtn.setOnAction(e -> {
            gameOverOverlay.setVisible(false);
            timeline.stop();
            mainApp.showMenuView();
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

        root.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case P -> togglePause();
            }
        });

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

        return root;
    }

    private void updateBoard(GridPane boardGui) {
        boardGui.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        boardGui.setAlignment(Pos.CENTER);
        boardGui.setHgap(0);
        boardGui.setVgap(0);
        boardGui.setStyle("-fx-border-color: black; -fx-border-width: 5; -fx-border-style: solid;");

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

        // Update captured pieces text whenever board updates
        updateCapturedPieces();
    }

    private void updateCapturedPieces() {
        Map<PieceType, Integer> whitePieces = new HashMap<>();
        Map<PieceType, Integer> blackPieces = new HashMap<>();

        // Standard chess counts
        whitePieces.put(PieceType.PAWN, 8); whitePieces.put(PieceType.ROOK, 2); whitePieces.put(PieceType.KNIGHT, 2);
        whitePieces.put(PieceType.BISHOP, 2); whitePieces.put(PieceType.QUEEN, 1); whitePieces.put(PieceType.KING, 1);

        blackPieces.putAll(whitePieces);

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = game.getBoard().getPieceAt(new Position(r, c));
                if (p != null) {
                    if (p.getColor() == com.easteurope.chess.model.coreData.Color.WHITE) {
                        int count = whitePieces.getOrDefault(p.getType(), 0);
                        if (count > 0) whitePieces.put(p.getType(), count - 1);
                    } else {
                        int count = blackPieces.getOrDefault(p.getType(), 0);
                        if (count > 0) blackPieces.put(p.getType(), count - 1);
                    }
                }
            }
        }

        StringBuilder whiteLost = new StringBuilder();
        StringBuilder blackLost = new StringBuilder();

        PieceType[] types = {PieceType.PAWN, PieceType.KNIGHT, PieceType.BISHOP, PieceType.ROOK, PieceType.QUEEN};

        for (PieceType t : types) {
            int missing = whitePieces.get(t);
            for (int i = 0; i < missing; i++) whiteLost.append(getCharForPiece(t, true));
        }

        for (PieceType t : types) {
            int missing = blackPieces.get(t);
            for (int i = 0; i < missing; i++) blackLost.append(getCharForPiece(t, false));
        }

        blackCapturedLabel.setText(whiteLost.toString());
        whiteCapturedLabel.setText(blackLost.toString());
    }

    private String getCharForPiece(PieceType type, boolean isWhite) {
        if (isWhite) {
            return switch (type) {
                case PAWN -> "o";
                case ROOK -> "t";
                case KNIGHT -> "m";
                case BISHOP -> "v";
                case QUEEN -> "w";
                case KING -> "l";
            };
        } else {
            return switch (type) {
                case PAWN -> "p";
                case ROOK -> "r";
                case KNIGHT -> "n";
                case BISHOP -> "b";
                case QUEEN -> "q";
                case KING -> "k";
            };
        }
    }

    private void setupInputLayer(GridPane inputGrid, GridPane boardGui) {
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
            } else {
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
        // CHANGED: Scroll to bottom
        historyArea.positionCaret(historyArea.getText().length());
        historyArea.setScrollTop(Double.MAX_VALUE);
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
        } else {
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
            game.pause();
            pauseOverlay.setVisible(true);
        } else {
            isPaused = false;
            game.resume();
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
            // Call Main to restart
            mainApp.startGame(config);
        });
        btnMain.setOnAction(e -> {
            SoundManager.playSound("click");
            timeline.stop();
            // Call Main to go to menu
            mainApp.showMenuView();
        });

        btnContinue.setStyle("-fx-font-size: 22px;");
        btnRestart.setStyle("-fx-font-size: 22px;");
        btnMain.setStyle("-fx-font-size: 22px;");

        menu.getChildren().addAll(btnContinue, btnRestart, btnMain);
        overlay.getChildren().add(menu);

        return overlay;
    }
}
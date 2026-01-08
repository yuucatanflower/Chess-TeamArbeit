package view;

import controller.Stockfish;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import model.GameState;
import model.coreData.Position;

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
    private Stage window; // Reference to the main window for switching of the scenes
    private Position selectedPosition = null;
    private long selectedTimeMs = 60 * 1000;

    private Timeline timeline;

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
        GameState game =  new GameState(5 * 60 * 1000);
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

        // Start with first scene (menu)
        showMenuScene();
    }

    // SCREEN 1: START MENU
    private void showMenuScene() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #2c3e50;");

        Label title = new Label("CHESS");
        title.setStyle("-fx-font-size: 50px; -fx-text-fill: white; -fx-font-weight: bold;");

        Button playBtn = new Button("play");
        Button settingsBtn = new Button("settings");
        Button exitBtn = new Button("exit");

        String btnStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 24px;";
        playBtn.setStyle(btnStyle);
        settingsBtn.setStyle(btnStyle);
        exitBtn.setStyle(btnStyle);

        playBtn.setOnAction(e -> showSetupScene());
        exitBtn.setOnAction(e -> window.close());

        layout.getChildren().addAll(title, playBtn, settingsBtn, exitBtn);

        Scene scene = new Scene(layout, 800, 600);
        window.setScene(scene);
        window.show();
    }

    // SCREEN 2: GAME SETUP
    private void showSetupScene() {
        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #34495e;");
        layout.setPadding(new Insets(20));

        Label title = new Label("Bot Selection + Time Control");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");

        // Bot Placeholder
        HBox bots = new HBox(20);
        bots.setAlignment(Pos.CENTER);
        for (int i = 1; i <= 4; i++) {
            VBox botBox = new VBox(5);
            Rectangle imgPlaceholder = new Rectangle(80, 80, Color.GRAY);
            Label name = new Label("BOT " + i);
            name.setTextFill(Color.WHITE);
            botBox.getChildren().addAll(imgPlaceholder, name);
            bots.getChildren().add(botBox);
        }

        // Time Setting
        HBox timeControls = new HBox(20);
        timeControls.setAlignment(Pos.CENTER);

        Button btn1 = new Button("1m");
        Button btn5 = new Button("5m");
        Button btn10 = new Button("10m");

        btn1.setOnAction(e -> selectedTimeMs = 60 * 1000);
        btn5.setOnAction(e -> selectedTimeMs = 5 * 60 * 1000);
        btn10.setOnAction(e -> selectedTimeMs = 10 * 60 * 1000);

        btn1.setStyle("-fx-text-fill: white; -fx-background-color: #7f8c8d;");
        btn5.setStyle("-fx-text-fill: white; -fx-background-color: #7f8c8d;");
        btn10.setStyle("-fx-text-fill: white; -fx-background-color: #7f8c8d;");

        timeControls.getChildren().addAll(btn1, btn5, btn10);

        Button startBtn = new Button("START");
        startBtn.setPrefSize(120, 40);
        startBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setOnAction(e -> showBoardScene());

        layout.getChildren().addAll(title, bots, new Label("Select time control:"), timeControls, startBtn);

        window.setScene(new Scene(layout, 800, 600));
    }

    // SCREEN 3: GAME BOARD
    private void showBoardScene() {
            this.game = new GameState(5 * 60 * 1000);

        game = new GameState(selectedTimeMs);

        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: #2c3e50;");

        // --- KEY CONCEPT: STACKPANE FOR LAYERING ---
        StackPane gameStack = new StackPane();

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
        gameStack.getChildren().addAll(boardGui, inputGrid);

        // Initial render of the board visuals
        updateBoard(boardGui);

        layout.setCenter(gameStack);

        // --- HISTORY ---

        historyArea = new TextArea();
        historyArea.setEditable(false);
        historyArea.setPrefHeight(400);

        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setStyle("-fx-background-color: #34495e;");
        sidebar.setPrefWidth(200);

        Label historyLabel = new Label("Move Order");
        historyLabel.setTextFill(Color.WHITE);

        sidebar.getChildren().addAll(historyLabel, historyArea);
        layout.setRight(sidebar);


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
        layout.setTop(topBar);

        window.setScene(new Scene(layout, 900, 700));

        // --- TIMELINE ---
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {

                    game.tickTimer();   // ⏱️ Zeit läuft wirklich

                    whiteTimeLabel.setText(
                            "White: " + formatTime(game.getWhiteTimeMs())
                    );
                    blackTimeLabel.setText(
                            "Black: " + formatTime(game.getBlackTimeMs())
                    );

                    if (game.isGameOver()) {
                        timeline.stop();
                    }
                })
        );

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
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

        for (model.coreData.Move move : game.getMoveHistory()) {
            if (move.movedPiece().getColor() == model.coreData.Color.WHITE) {
                sb.append(moveNumber++).append(". ");
            }

            sb.append(move.from().toAlgebraicNotation())
                    .append("-")
                    .append(move.to().toAlgebraicNotation())
                    .append("\n");
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
        // Since your Board logic (Row 0 = Black/Top) matches JavaFX (Row 0 = Top),
        // we use the coordinates directly without flipping.
        Position clickedPos = new Position(row, col);

        System.out.println("Clicked: " + clickedPos.toAlgebraicNotation()); // Debugging

        // CASE 1: Select a piece (First Click)
        if (selectedPosition == null) {
            model.Piece piece = game.getBoard().getPieceAt(clickedPos);

            // Only allow selecting pieces that belong to the current turn's player
            if (piece != null && piece.getColor() == game.getCurrentTurn()) {
                selectedPosition = clickedPos;
                System.out.println("Selected: " + clickedPos.toAlgebraicNotation());
                updateBoard(boardGui); // Redraw to show highlight
            }
        }
        // CASE 2: Move or Change Selection (Second Click)
        else {
            // If clicking the same tile again -> Deselect
            if (clickedPos.equals(selectedPosition)) {
                selectedPosition = null;
                updateBoard(boardGui);
                return;
            }

            // Try to execute the move in the game logic
            boolean success = game.playTurn(selectedPosition, clickedPos);

            if (success) {
                System.out.println("Move successful!");
                selectedPosition = null; // Reset selection after move

                // 1. Update the board immediately so the user sees the final move
                updateBoard(boardGui);

                updateHistory();

                // 2. Check if the game is over
                if (game.isGameOver()) {
                    showGameOverDialog(); // Shows the popup
                    return; // Stop execution here
                }

                return; // Return here to avoid double-updating at the bottom
            } else {
                System.out.println("Invalid move or selection switch");

                // UX Feature: If the move failed but the user clicked on another OWN piece,
                // switch selection to that new piece instead of just deselecting everything.
                model.Piece clickedPiece = game.getBoard().getPieceAt(clickedPos);
                if (clickedPiece != null && clickedPiece.getColor() == game.getCurrentTurn()) {
                    selectedPosition = clickedPos; // Switch selection
                    System.out.println("Switched selection to: " + clickedPos.toAlgebraicNotation());
                } else {
                    selectedPosition = null; // Clicked empty space or enemy -> Deselect all
                }
            }
            // Redraw board to reflect new positions or cleared selection
            updateBoard(boardGui);
        }
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

                // 3. Piece Rendering
                model.Piece piece = game.getBoard().getPieceAt(currentPos);
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
}

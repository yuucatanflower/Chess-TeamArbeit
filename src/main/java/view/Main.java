package view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import model.GameState;
import model.coreData.Position;

import java.util.Scanner;

public class Main extends Application{
    private Stage window; // Reference to the main window for switching of the scenes

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
        GameState game =  new GameState();
        Scanner input = new Scanner(System.in);

        System.out.println("---GAME STARTED---");
        System.out.println("Type moves as 'e2-e4' or 'undo' to revert move. Type 'exit' or 'quit' to end the game. ");
        controller.Stockfish bot = new controller.Stockfish();

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

        while(!game.isGameOver()){
            game.getBoard().printBoard();
            System.out.println("STATUS: "+ game.getStatusMessage());
            System.out.print("> "); // prompt move or undo

            String command = input.nextLine().trim();

            //input processing
            if(command.equalsIgnoreCase("exit") || command.equalsIgnoreCase("quit")) {
                break;
            }
            if(command.equalsIgnoreCase("undo")) {
                game.undo();
                continue;
            }

            // move parsing ( expects 'e2-e4' type format )
            if(isValidInputFormat(command)) {
                try{
                    String[] commandParts = command.split("-"); // splits the command on '-' into two elements and puts them in an array
                    Position from = Position.fromAlgebraicNotation(commandParts[0]); // for 'e2-e4' that would be e2
                    Position to = Position.fromAlgebraicNotation(commandParts[1]); // and e4

                    boolean success = game.playTurn(from, to);
                    if(success) {
                        System.out.println("Move from " + from.toAlgebraicNotation() + " to " + to.toAlgebraicNotation());
                    }else{
                        System.out.println("Move failed!");
                    }
                }catch(Exception e){
                    System.out.println("Error parsing move: Use 'e2-e4' format."); //if format is wrong
                }
            }else{
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
        for(int i=1; i<=4; i++) {
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
        String[] times = {"5m", "10m", "20m"};
        for(String t : times) {
            Button tBtn = new Button(t);
            tBtn.setStyle("-fx-text-fill: white; -fx-background-color: #7f8c8d;");
            timeControls.getChildren().add(tBtn);
        }

        Button startBtn = new Button("START");
        startBtn.setPrefSize(120, 40);
        startBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setOnAction(e -> showBoardScene());

        layout.getChildren().addAll(title, bots, new Label("Select time control:"), timeControls, startBtn);

        window.setScene(new Scene(layout, 800, 600));
    }

    // SCREEN 3: GAME BOARD
    private void showBoardScene() {
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: #2c3e50;");

        // Chessboard
        GridPane boardGui = new GridPane();
        boardGui.setAlignment(Pos.CENTER);
        int tileSize = 60;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Rectangle rect = new Rectangle(tileSize, tileSize);
                rect.setFill((row + col) % 2 == 0 ? Color.web("#EBECD0") : Color.web("#779556"));
                boardGui.add(new StackPane(rect), col, row);
            }
        }
        layout.setCenter(boardGui);

        // --- Right: MOVE HISTORY & BANNER ---
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setStyle("-fx-background-color: #34495e;");
        sidebar.setPrefWidth(200);

        Label historyLabel = new Label("Move Order");
        historyLabel.setTextFill(Color.WHITE);

        TextArea dummyHistory = new TextArea("1. e4 d5\n2. exd5 Qxd5"); // Placeholder
        dummyHistory.setEditable(false);
        dummyHistory.setPrefHeight(400);

        sidebar.getChildren().addAll(historyLabel, dummyHistory);
        layout.setRight(sidebar);

        // --- Up: TIMER ---
        HBox topBar = new HBox(100);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(10));
        Label timerW = new Label("White: 05:00");
        Label timerB = new Label("Black: 05:00");
        timerW.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        timerB.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        topBar.getChildren().addAll(timerB, timerW);
        layout.setTop(topBar);

        window.setScene(new Scene(layout, 900, 700));
    }
}

package com.easteurope.chess;

import com.easteurope.chess.controller.ConsoleGame;
import com.easteurope.chess.model.GameConfig;
import com.easteurope.chess.view.GameScreen;
import com.easteurope.chess.view.scenes.MenuScene;
import com.easteurope.chess.view.scenes.SettingsScene;
import com.easteurope.chess.view.scenes.SetupScene;
import com.easteurope.chess.view.SoundManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Scanner;

public class Main extends Application {

    private Stage window;
    private Scene mainScene; // We use one single scene and swap the content (Root)

    public static void main(String[] args) {
        System.out.println("--- CHESS PROJECT STARTER ---");
        System.out.println("[1] Console Mode");
        System.out.println("[2] Graphical Mode");

        // We check for input safely so it works in IDEs without console arguments
        Scanner sc = new Scanner(System.in);
        if (sc.hasNextLine()) {
            String input = sc.nextLine();
            if (input.equals("2")) {
                launch(args);
            } else {
                new ConsoleGame().start();
            }
        } else {
            // Default to GUI if no input is provided
            launch(args);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.window = primaryStage;
        window.setTitle("C.");

        // Load audio once at startup
        SoundManager.loadSounds();
        SoundManager.startMusic();

        // 1. Initialize the single scene with an empty root
        mainScene = new Scene(new StackPane(), 800, 600);
        window.setScene(mainScene);

        // 2. Set Fullscreen settings once
        window.setFullScreenExitHint("");
        window.setFullScreen(true);

        // 3. Load the Menu as the first screen
        showMenuView();

        window.show();
    }

    // --- NAVIGATION METHODS ---
    // These methods allow the Scene classes to switch views without needing new Scenes.

    public void showMenuView() {
        MenuScene menu = new MenuScene(this);
        mainScene.setRoot(menu.getView());
    }

    public void showSetupView() {
        SetupScene setup = new SetupScene(this);
        mainScene.setRoot(setup.getView());
    }

    public void showSettingsView() {
        SettingsScene settings = new SettingsScene(this);
        mainScene.setRoot(settings.getView());
    }

    public void startGame(GameConfig config) {
        GameScreen gameScreen = new GameScreen(this, config);
        mainScene.setRoot(gameScreen.getView());

        // Start the game loop (timers, bot logic, etc.)
        gameScreen.startGameLoop();
    }

    // Allows other classes to close the window
    public Stage getWindow() {
        return window;
    }
}
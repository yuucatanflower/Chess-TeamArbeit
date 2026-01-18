package com.easteurope.chess.controller;

import com.easteurope.chess.model.GameState;
import com.easteurope.chess.model.coreData.Color;
import com.easteurope.chess.model.coreData.Position;

import java.util.Scanner;

public class ConsoleGame {

    public void start() {
        GameState game = new GameState(5 * 60 * 1000, Color.WHITE, 0);
        Scanner input = new Scanner(System.in);

        System.out.println("--- CONSOLE GAME STARTED ---");
        System.out.println("Type moves as 'e2-e4' or 'undo'. Type 'exit' to quit.");

        // Simple Bot Setup for Console
        Stockfish bot = new Stockfish();
        if (bot.startEngine()) {
            System.out.println("Engine started! (Stockfish available)");
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
                        System.out.println("Move: " + command);
                    } else {
                        System.out.println("Move failed! (Illegal move or wrong turn)");
                    }
                } catch (Exception e) {
                    System.out.println("Error parsing move: Use 'e2-e4' format.");
                }
            } else {
                System.out.println("Unknown command! Type moves as 'e2-e4' or 'undo'.");
            }
        }

        System.out.println("FINAL STATUS: " + game.getStatusMessage());
        input.close();
        bot.stopEngine();
    }

    private boolean isValidInputFormat(String input) {
        return input.matches("[a-h][1-8]-[a-h][1-8]");
    }
}
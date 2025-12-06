import model.Board;
import model.GameState;
import model.coreData.Position;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        GameState game =  new GameState();
        Scanner input = new Scanner(System.in);

        System.out.println("---GAME STARTED---");
        System.out.println("Type moves as 'e2-e4' or 'undo' to revert move. Type 'exit' or 'quit' to end the game. ");

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
}

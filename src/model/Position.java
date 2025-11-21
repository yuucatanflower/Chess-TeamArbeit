package model;
// record for positions on the board

public record Position(int row, int col) {
    // TODO dont forget to write exceptions in the methods!!! later

    public String toAlgebraicNotation() {
        if (row < 0 || col < 0 || row > 7 || col > 7) {
            System.out.println("Row or column out of bounds");
        } //TODO later write an exception here InvalidArgumentException
        char column = (char) ('a' + col);
        char rank = (char) ('1' + (7 - row));

        return "" + column + rank;
    }
    public static Position fromAlgebraicNotation(String algebraicNotation) {
        if(algebraicNotation.length() != 2 || algebraicNotation == null){
            System.out.println("Invalid String Input");
            //TODO throw an exception here
        }
        char columnChar = algebraicNotation.charAt(0);
        char rowChar = algebraicNotation.charAt(1);

        if(columnChar < 'a' || columnChar > 'h' || rowChar < '1' || rowChar > '8') {
            //TODO again exception here later
            System.out.println("Invalid Algebraic Notation");
        }

        int newCol = columnChar - 'a';
        int newRow = 7 - (rowChar - '1');

        return new Position(newRow, newCol);
    }

}

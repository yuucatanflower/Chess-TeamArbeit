package model;

public record Position(int row, int col) {
    // TODO dont forget to write exceptions in the methods!!! later
    // Like this? Im aware there are more types of exceptions but i think this should work

    public String toAlgebraicNotation() {
        if (row < 0 || col < 0 || row > 7 || col > 7) {
            throw new IllegalArgumentException("Row or column out of bounds");
        }
        char column = (char) ('a' + col);
        char rank = (char) ('1' + (7 - row));

        return "" + column + rank;
    }
    public static Position fromAlgebraicNotation(String algebraicNotation) {
        if(algebraicNotation == null || algebraicNotation.length() != 2){
            throw new IllegalArgumentException("Invalid String Input");

        }
        char columnChar = algebraicNotation.charAt(0);
        char rowChar = algebraicNotation.charAt(1);

        if(columnChar < 'a' || columnChar > 'h' || rowChar < '1' || rowChar > '8') {
            throw new IllegalArgumentException("Invalid Algebraic Notation");

        }

        int newCol = columnChar - 'a';
        int newRow = 7 - (rowChar - '1');

        return new Position(newRow, newCol);
    }

}
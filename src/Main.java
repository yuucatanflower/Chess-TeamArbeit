import model.Position;

public class Main {
    public static void main(String[] args) {
        Position a1 = new Position(7, 0);
        Position e5 = new Position(3, 4);
        System.out.println(a1.toAlgebraicNotation());
        System.out.println(e5.toAlgebraicNotation());
        System.out.println();


        Position test_e5 = Position.fromAlgebraicNotation("e5");
        System.out.println(test_e5.toAlgebraicNotation());
        System.out.println(test_e5.row());
        System.out.println(test_e5.col());
        System.out.println();

        try{
            Position test = new Position(9, 9);
            System.out.println(test.toAlgebraicNotation());
        } catch(Exception e){
            System.out.println(e);
        }
    }
}

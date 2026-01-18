module Chess.TeamArbeit {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.graphics;


    exports com.easteurope.chess;
    opens com.easteurope.chess to javafx.fxml;

}
module Chess.TeamArbeit {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    exports com.easteurope.chess;
    opens com.easteurope.chess to javafx.fxml;

}
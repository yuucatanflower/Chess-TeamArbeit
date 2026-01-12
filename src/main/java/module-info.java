module Chess.TeamArbeit {
    requires javafx.controls;
    requires javafx.fxml;

    exports com.easteurope.chess;
    opens com.easteurope.chess to javafx.fxml;

}
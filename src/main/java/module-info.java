module Chess.TeamArbeit {
    requires javafx.controls;
    requires javafx.fxml;

    opens view to javafx.fxml;
    exports view;

}
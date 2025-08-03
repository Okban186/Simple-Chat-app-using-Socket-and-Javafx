module com.chat {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    opens com.chat to javafx.fxml;
    exports com.chat;
}

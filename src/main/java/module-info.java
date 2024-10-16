module com.example.nachito {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.nachito to javafx.fxml;
    exports com.example.nachito;
}
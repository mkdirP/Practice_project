module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires org.apache.pdfbox;
    requires lombok;
    requires com.fasterxml.jackson.databind;
    requires com.github.librepdf.openpdf;


    opens com.example.myjavafx to javafx.fxml;
    exports com.example.myjavafx;
    exports com.example.myjavafx.controller;
    opens com.example.myjavafx.controller to javafx.fxml;
    exports com.example.myjavafx.model;
    opens com.example.myjavafx.model to javafx.base, javafx.fxml;
}
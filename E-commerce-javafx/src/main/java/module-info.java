module org.example.ecommercejavafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.apache.pdfbox;
    requires kernel;
    requires layout;


    opens org.example.ecommercejavafx to javafx.fxml;
    exports org.example.ecommercejavafx;
}
module com.cafe.cafemanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.cafe.cafemanagementsystem to javafx.fxml;
    exports com.cafe.cafemanagementsystem;
}
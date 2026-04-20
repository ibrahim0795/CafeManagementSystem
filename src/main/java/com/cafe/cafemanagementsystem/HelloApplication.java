package com.cafe.cafemanagementsystem;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    public static Stage primaryStage;

    public static final String BLUE  = "#1a3a6b";
    public static final String WHITE = "#ffffff";
    public static final String BG    = "#f4f6f9";
    public static final String RED   = "#c0392b";
    public static final String GREEN = "#1a7a4a";

    @Override
    public void start(Stage stage) {
        DataStore.init();
        primaryStage = stage;
        stage.setTitle("Cafe Management System — ByteBrew");
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.setOnCloseRequest(e -> DataStore.shutdown());
        showLogin();
        stage.show();
    }

    public static void showLogin()         { primaryStage.setScene(new LoginScreen().getScene()); }
    public static void showDashboard()     { primaryStage.setScene(new DashboardScreen().getScene()); }
    public static void showMenu()          { primaryStage.setScene(new MenuScreen().getScene()); }
    public static void showInventory()     { primaryStage.setScene(new InventoryScreen().getScene()); }
    public static void showNewOrder()      { primaryStage.setScene(new NewOrderScreen().getScene()); }
    public static void showOrderHistory()  { primaryStage.setScene(new OrderHistoryScreen().getScene()); }
    public static void showSalesReport()   { primaryStage.setScene(new SalesReportScreen().getScene()); }
    public static void showUserManagement(){ primaryStage.setScene(new UserManagementScreen().getScene()); }

    public static Button styledButton(String text, String bgColor, String textColor) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color:" + bgColor + ";-fx-text-fill:" + textColor +
                ";-fx-font-size:13px;-fx-font-weight:bold;-fx-padding:8 18;-fx-background-radius:6;-fx-cursor:hand;");
        btn.setOnMouseEntered(e -> btn.setOpacity(0.85));
        btn.setOnMouseExited(e  -> btn.setOpacity(1.0));
        return btn;
    }

    public static HBox topBar(String title) {
        Label titleLabel = new Label("☕  " + title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.WHITE);

        String who = DataStore.currentUser != null
                ? DataStore.currentUser.username + " (" + DataStore.currentUser.role + ")" : "";
        Label userLabel = new Label("Logged in as: " + who);
        userLabel.setTextFill(Color.web("#aad4f5"));
        userLabel.setFont(Font.font("Arial", 13));

        Button homeBtn   = styledButton("Dashboard", "#2e5fa3", WHITE);
        Button logoutBtn = styledButton("Logout", RED, WHITE);
        homeBtn.setOnAction(e -> showDashboard());
        logoutBtn.setOnAction(e -> { DataStore.currentUser = null; showLogin(); });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(12, titleLabel, spacer, userLabel, homeBtn, logoutBtn);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 20, 12, 20));
        bar.setStyle("-fx-background-color:" + BLUE + ";");
        return bar;
    }

    public static void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}
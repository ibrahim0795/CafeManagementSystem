package com.cafe.cafemanagementsystem;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

public class LoginScreen {

    public Scene getScene() {

        // ── LEFT BRAND PANEL ──────────────────────────────────────
        VBox left = new VBox(14);
        left.setAlignment(Pos.CENTER);
        left.setPadding(new Insets(60, 50, 60, 50));
        left.setStyle("-fx-background-color:" + HelloApplication.BLUE + ";");
        left.setPrefWidth(300);

        Label icon = new Label("☕");
        icon.setFont(Font.font(64));

        Label appName = new Label("Cafe Management\nSystem");
        appName.setTextFill(Color.WHITE);
        appName.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        appName.setTextAlignment(TextAlignment.CENTER);
        appName.setAlignment(Pos.CENTER);

        Label team = new Label("ByteBrew Software Solutions");
        team.setTextFill(Color.web("#aad4f5"));
        team.setFont(Font.font("Arial", 13));

        Label hint = new Label("Manager:  manager / manager123\nCashier:   cashier / cashier123");
        hint.setTextFill(Color.web("#7fb3d3"));
        hint.setFont(Font.font("Arial", 11));
        hint.setTextAlignment(TextAlignment.CENTER);
        hint.setAlignment(Pos.CENTER);
        hint.setPadding(new Insets(30, 0, 0, 0));

        left.getChildren().addAll(icon, appName, team, hint);

        // ── RIGHT LOGIN PANEL ─────────────────────────────────────
        VBox right = new VBox(18);
        right.setAlignment(Pos.CENTER);
        right.setPadding(new Insets(60, 60, 60, 60));
        right.setStyle("-fx-background-color:#f4f6f9;");

        Label loginTitle = new Label("Welcome Back");
        loginTitle.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        loginTitle.setTextFill(Color.web(HelloApplication.BLUE));

        Label loginSub = new Label("Sign in to your account");
        loginSub.setTextFill(Color.GRAY);
        loginSub.setFont(Font.font("Arial", 14));

        Label userLbl = new Label("Username");
        userLbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setPrefHeight(40);
        usernameField.setMaxWidth(280);
        usernameField.setStyle("-fx-font-size:14px; -fx-background-radius:6; -fx-border-color:#ccc; -fx-border-radius:6;");

        Label passLbl = new Label("Password");
        passLbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setPrefHeight(40);
        passwordField.setMaxWidth(280);
        passwordField.setStyle("-fx-font-size:14px; -fx-background-radius:6; -fx-border-color:#ccc; -fx-border-radius:6;");

        Label errorLabel = new Label("");
        errorLabel.setTextFill(Color.web(HelloApplication.RED));
        errorLabel.setFont(Font.font("Arial", 13));

        Button loginBtn = HelloApplication.styledButton("Sign In", HelloApplication.BLUE, HelloApplication.WHITE);
        loginBtn.setPrefWidth(280);
        loginBtn.setPrefHeight(42);

        loginBtn.setOnAction(e -> {
            String u = usernameField.getText().trim();
            String p = passwordField.getText().trim();
            if (u.isEmpty() || p.isEmpty()) {
                errorLabel.setText("Please enter both username and password.");
                return;
            }
            DataStore.User user = DataStore.login(u, p);
            if (user != null) {
                DataStore.currentUser = user;
                HelloApplication.showDashboard();
            } else {
                errorLabel.setText("Invalid username or password. Try again.");
                passwordField.clear();
            }
        });

        usernameField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> loginBtn.fire());

        right.getChildren().addAll(loginTitle, loginSub, userLbl, usernameField, passLbl, passwordField, errorLabel, loginBtn);

        // ── ROOT ──────────────────────────────────────────────────
        HBox root = new HBox(left, right);
        HBox.setHgrow(right, Priority.ALWAYS);
        return new Scene(root, 900, 580);
    }
}
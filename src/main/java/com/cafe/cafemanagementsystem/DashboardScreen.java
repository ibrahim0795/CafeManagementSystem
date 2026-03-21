package com.cafe.cafemanagementsystem;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

public class DashboardScreen {

    public Scene getScene() {
        boolean isManager = "Manager".equals(DataStore.currentUser.role);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:#f4f6f9;");

        HBox topBar = HelloApplication.topBar("Dashboard");

        // ── WELCOME ───────────────────────────────────────────────
        VBox welcomeBox = new VBox(4);
        welcomeBox.setPadding(new Insets(24, 24, 8, 24));
        welcomeBox.setStyle("-fx-background-color:#f4f6f9;");

        Label welcome = new Label("Good day, " + DataStore.currentUser.username + "!");
        welcome.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        welcome.setTextFill(Color.web("#1a3a6b"));

        Label sub = new Label("Role: " + DataStore.currentUser.role + "   |   Select a module to get started.");
        sub.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        sub.setTextFill(Color.web("#666666"));

        welcomeBox.getChildren().addAll(welcome, sub);

        // ── STAT CARDS ────────────────────────────────────────────
        long lowStock = DataStore.inventory.stream().filter(DataStore.InventoryItem::isLowStock).count();
        double revenue = DataStore.orders.stream().mapToDouble(o -> o.total).sum();

        HBox statsRow = new HBox(14);
        statsRow.setPadding(new Insets(12, 24, 8, 24));
        statsRow.setStyle("-fx-background-color:#f4f6f9;");
        statsRow.getChildren().addAll(
                statCard("Total Menu Items", String.valueOf(DataStore.menuItems.size()), "#1a3a6b"),
                statCard("Orders Today",     String.valueOf(DataStore.orders.size()),    "#1a7a4a"),
                statCard("Low Stock Alerts", String.valueOf(lowStock),                  "#c0392b"),
                statCard("Revenue Today",    String.format("$%.2f", revenue),           "#7d3c98")
        );

        // ── MODULE SECTION ────────────────────────────────────────
        Label modulesLabel = new Label("Modules");
        modulesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        modulesLabel.setTextFill(Color.web("#1a3a6b"));
        modulesLabel.setPadding(new Insets(8, 24, 4, 24));

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        grid.setPadding(new Insets(8, 24, 24, 24));
        grid.setStyle("-fx-background-color:#f4f6f9;");

        grid.add(moduleCard(
                "Create New Order",
                "Select items and place a customer order",
                "#1a3a6b",
                e -> HelloApplication.showNewOrder()), 0, 0);

        if (isManager) {
            grid.add(moduleCard(
                    "Menu Management",
                    "Add, edit, or remove menu items",
                    "#2e5fa3",
                    e -> HelloApplication.showMenu()), 1, 0);

            grid.add(moduleCard(
                    "Inventory Management",
                    "Track and update ingredient stock levels",
                    "#1a7a4a",
                    e -> HelloApplication.showInventory()), 0, 1);

            grid.add(moduleCard(
                    "Order History",
                    "View and filter all past orders",
                    "#7d3c98",
                    e -> HelloApplication.showOrderHistory()), 1, 1);
        } else {
            VBox noteBox = new VBox(6);
            noteBox.setPadding(new Insets(16));

            Label noteLine1 = new Label("Menu Management, Inventory, and");
            noteLine1.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
            noteLine1.setTextFill(Color.web("#888888"));

            Label noteLine2 = new Label("Order History are for Managers only.");
            noteLine2.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
            noteLine2.setTextFill(Color.web("#888888"));

            noteBox.getChildren().addAll(noteLine1, noteLine2);
            grid.add(noteBox, 1, 0);
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:#f4f6f9; -fx-background:#f4f6f9;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(topBar, welcomeBox, statsRow, modulesLabel, scroll);
        return new Scene(root, 950, 680);
    }

    // ── STAT CARD ─────────────────────────────────────────────────
    private VBox statCard(String label, String value, String accentColor) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle(
                "-fx-background-color:#ffffff;" +
                        "-fx-background-radius:10;" +
                        "-fx-border-color:" + accentColor + ";" +
                        "-fx-border-width:0 0 0 5;" +
                        "-fx-border-radius:0 10 10 0;"
        );

        Label valLbl = new Label(value);
        valLbl.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        valLbl.setTextFill(Color.web(accentColor));

        Label lbl = new Label(label);
        lbl.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        lbl.setTextFill(Color.web("#666666"));

        card.getChildren().addAll(valLbl, lbl);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    // ── MODULE CARD ───────────────────────────────────────────────
    private VBox moduleCard(String title, String desc, String accentColor,
                            javafx.event.EventHandler<javafx.event.ActionEvent> action) {

        // Title label — dark text on white background
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web(accentColor));
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(220);
        titleLabel.setStyle("-fx-background-color:transparent;");

        // Description label
        Label descLabel = new Label(desc);
        descLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        descLabel.setTextFill(Color.web("#555555"));
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(220);
        descLabel.setStyle("-fx-background-color:transparent;");

        // Open button
        Button openBtn = new Button("Open \u2192");
        openBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        openBtn.setTextFill(Color.WHITE);
        openBtn.setStyle(
                "-fx-background-color:" + accentColor + ";" +
                        "-fx-text-fill:#ffffff;" +
                        "-fx-padding:7 18;" +
                        "-fx-background-radius:6;" +
                        "-fx-cursor:hand;"
        );
        openBtn.setOnAction(action);
        openBtn.setOnMouseEntered(e -> openBtn.setOpacity(0.82));
        openBtn.setOnMouseExited(e  -> openBtn.setOpacity(1.0));

        VBox card = new VBox(10, titleLabel, descLabel, openBtn);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(22, 22, 18, 22));
        card.setPrefWidth(260);
        card.setPrefHeight(155);
        // White background explicitly — no color inheritance
        card.setStyle(
                "-fx-background-color:#ffffff;" +
                        "-fx-background-radius:12;" +
                        "-fx-border-color:#dddddd;" +
                        "-fx-border-width:1;" +
                        "-fx-border-radius:12;"
        );
        return card;
    }
}
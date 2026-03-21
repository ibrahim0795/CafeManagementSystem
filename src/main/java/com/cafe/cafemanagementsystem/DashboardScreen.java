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
        root.setStyle("-fx-background-color:" + HelloApplication.BG + ";");

        // ── TOP BAR ───────────────────────────────────────────────
        HBox topBar = HelloApplication.topBar("Dashboard");

        // ── WELCOME ───────────────────────────────────────────────
        VBox welcomeBox = new VBox(4);
        welcomeBox.setPadding(new Insets(24, 24, 8, 24));

        Label welcome = new Label("Good day, " + DataStore.currentUser.username + "!");
        welcome.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        welcome.setTextFill(Color.web(HelloApplication.BLUE));

        Label sub = new Label("Role: " + DataStore.currentUser.role + "   |   Select a module to get started.");
        sub.setFont(Font.font("Arial", 14));
        sub.setTextFill(Color.GRAY);

        welcomeBox.getChildren().addAll(welcome, sub);

        // ── STAT CARDS ────────────────────────────────────────────
        long lowStock = DataStore.inventory.stream().filter(DataStore.InventoryItem::isLowStock).count();
        double revenue = DataStore.orders.stream().mapToDouble(o -> o.total).sum();

        HBox statsRow = new HBox(14);
        statsRow.setPadding(new Insets(12, 24, 8, 24));
        statsRow.getChildren().addAll(
                statCard("Total Menu Items",  String.valueOf(DataStore.menuItems.size()), "#1a3a6b"),
                statCard("Orders Today",      String.valueOf(DataStore.orders.size()),    "#1a7a4a"),
                statCard("Low Stock Alerts",  String.valueOf(lowStock),                  "#c0392b"),
                statCard("Revenue Today",     String.format("$%.2f", revenue),           "#7d3c98")
        );

        // ── MODULE CARDS ──────────────────────────────────────────
        Label modulesLabel = new Label("Modules");
        modulesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        modulesLabel.setTextFill(Color.web(HelloApplication.BLUE));
        modulesLabel.setPadding(new Insets(8, 24, 4, 24));

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        grid.setPadding(new Insets(8, 24, 24, 24));

        grid.add(moduleCard("Create New Order",
                "Select items and place a customer order",
                "#1a3a6b", e -> HelloApplication.showNewOrder()), 0, 0);

        if (isManager) {
            grid.add(moduleCard("Menu Management",
                    "Add, edit, or remove menu items",
                    "#2e5fa3", e -> HelloApplication.showMenu()), 1, 0);

            grid.add(moduleCard("Inventory Management",
                    "Track and update ingredient stock levels",
                    "#1a7a4a", e -> HelloApplication.showInventory()), 0, 1);

            grid.add(moduleCard("Order History",
                    "View and filter all past orders",
                    "#7d3c98", e -> HelloApplication.showOrderHistory()), 1, 1);
        } else {
            Label note = new Label("Menu Management, Inventory, and Order History\nare available to Managers only.");
            note.setTextFill(Color.GRAY);
            note.setFont(Font.font("Arial", 13));
            note.setPadding(new Insets(12, 0, 0, 4));
            grid.add(note, 1, 0);
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(topBar, welcomeBox, statsRow, modulesLabel, scroll);
        return new Scene(root, 950, 680);
    }

    // ── STAT CARD ─────────────────────────────────────────────────
    private VBox statCard(String label, String value, String color) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:10;" +
                        "-fx-border-color:" + color + ";" +
                        "-fx-border-width:0 0 0 5;" +
                        "-fx-border-radius:0 10 10 0;"
        );

        Label valLbl = new Label(value);
        valLbl.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        valLbl.setTextFill(Color.web(color));

        Label lbl = new Label(label);
        lbl.setFont(Font.font("Arial", 12));
        lbl.setTextFill(Color.GRAY);

        card.getChildren().addAll(valLbl, lbl);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    // ── MODULE CARD ───────────────────────────────────────────────
    private VBox moduleCard(String title, String desc, String color,
                            javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(22));
        card.setPrefWidth(260);
        card.setPrefHeight(150);
        card.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:12;" +
                        "-fx-border-color:#e0e0e0;" +
                        "-fx-border-width:1;" +
                        "-fx-border-radius:12;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        titleLabel.setTextFill(Color.web(color));
        titleLabel.setWrapText(true);

        Label descLabel = new Label(desc);
        descLabel.setFont(Font.font("Arial", 12));
        descLabel.setTextFill(Color.GRAY);
        descLabel.setWrapText(true);

        Button openBtn = HelloApplication.styledButton("Open →", color, HelloApplication.WHITE);
        openBtn.setOnAction(action);

        card.getChildren().addAll(titleLabel, descLabel, openBtn);
        return card;
    }
}
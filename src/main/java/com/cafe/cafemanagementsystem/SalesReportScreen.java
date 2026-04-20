package com.cafe.cafemanagementsystem;

import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import java.util.*;
import java.util.stream.*;

public class SalesReportScreen {

    public Scene getScene() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:#f4f6f9;");

        HBox topBar = HelloApplication.topBar("Sales Report");

        // ── COMPUTE STATS ─────────────────────────────────────────
        List<DataStore.Order> orders = DataStore.orders;
        double totalRevenue = orders.stream().mapToDouble(o -> o.total).sum();
        int totalOrders    = orders.size();
        double avgOrder    = totalOrders > 0 ? totalRevenue / totalOrders : 0;

        // Top 5 items by quantity sold
        Map<String, int[]> itemSales = new LinkedHashMap<>();
        Map<String, double[]> itemRevenue = new LinkedHashMap<>();
        for (DataStore.Order order : orders) {
            for (DataStore.OrderLine line : order.lines) {
                String name = line.item.getName();
                itemSales.computeIfAbsent(name, k -> new int[]{0})[0] += line.quantity;
                itemRevenue.computeIfAbsent(name, k -> new double[]{0})[0] += line.subtotal();
            }
        }

        List<Map.Entry<String, int[]>> top5 = itemSales.entrySet().stream()
                .sorted((a, b) -> b.getValue()[0] - a.getValue()[0])
                .limit(5)
                .collect(Collectors.toList());

        // ── SUMMARY CARDS ROW ─────────────────────────────────────
        HBox cardsRow = new HBox(16);
        cardsRow.setPadding(new Insets(20, 24, 12, 24));
        cardsRow.setStyle("-fx-background-color:#f4f6f9;");
        cardsRow.getChildren().addAll(
                summaryCard("Total Revenue",    String.format("$%.2f", totalRevenue), "#1a3a6b"),
                summaryCard("Total Orders",     String.valueOf(totalOrders),           "#1a7a4a"),
                summaryCard("Avg Order Value",  String.format("$%.2f", avgOrder),     "#7d3c98"),
                summaryCard("Menu Items",       String.valueOf(DataStore.menuItems.size()), "#e67e22")
        );

        // ── TOP SELLING ITEMS TABLE ───────────────────────────────
        Label topTitle = new Label("Top Selling Items");
        topTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        topTitle.setTextFill(Color.web("#1a3a6b"));
        topTitle.setPadding(new Insets(0, 24, 6, 24));

        TableView<TopItem> topTable = new TableView<>();
        topTable.setStyle("-fx-font-size:13px;");
        topTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        topTable.setPrefHeight(220);

        TableColumn<TopItem, Integer> rankCol = new TableColumn<>("Rank");
        rankCol.setCellValueFactory(new PropertyValueFactory<>("rank"));
        rankCol.setPrefWidth(60);

        TableColumn<TopItem, String> nameCol = new TableColumn<>("Item Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<TopItem, Integer> qtyCol = new TableColumn<>("Qty Sold");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("qtySold"));
        qtyCol.setPrefWidth(100);

        TableColumn<TopItem, String> revCol = new TableColumn<>("Revenue");
        revCol.setCellValueFactory(new PropertyValueFactory<>("revenue"));
        revCol.setPrefWidth(120);

        topTable.getColumns().addAll(rankCol, nameCol, qtyCol, revCol);

        ObservableList<TopItem> topData = FXCollections.observableArrayList();
        if (top5.isEmpty()) {
            topTable.setPlaceholder(new Label("No sales data available yet. Create some orders first."));
        } else {
            for (int i = 0; i < top5.size(); i++) {
                String name = top5.get(i).getKey();
                int qty = top5.get(i).getValue()[0];
                double rev = itemRevenue.getOrDefault(name, new double[]{0})[0];
                topData.add(new TopItem(i + 1, name, qty, String.format("$%.2f", rev)));
            }
        }
        topTable.setItems(topData);

        // ── ALL ORDERS TABLE ──────────────────────────────────────
        Label ordersTitle = new Label("All Orders");
        ordersTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        ordersTitle.setTextFill(Color.web("#1a3a6b"));
        ordersTitle.setPadding(new Insets(16, 24, 6, 24));

        TableView<DataStore.Order> ordersTable = new TableView<>();
        ordersTable.setStyle("-fx-font-size:13px;");
        ordersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ordersTable.setPlaceholder(new Label("No orders yet."));

        TableColumn<DataStore.Order, Integer> idCol = new TableColumn<>("Order ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<DataStore.Order, String> dtCol = new TableColumn<>("Date & Time");
        dtCol.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        TableColumn<DataStore.Order, String> cashierCol = new TableColumn<>("Cashier");
        cashierCol.setCellValueFactory(new PropertyValueFactory<>("cashier"));
        TableColumn<DataStore.Order, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalCol.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("$%.2f", v));
            }
        });

        ordersTable.getColumns().addAll(idCol, dtCol, cashierCol, totalCol);
        ordersTable.setItems(FXCollections.observableArrayList(DataStore.orders));

        // ── LAYOUT ────────────────────────────────────────────────
        VBox content = new VBox(0,
                cardsRow,
                topTitle, new Insets(0, 24, 0, 24) != null ? withPadding(topTable, new Insets(0, 24, 0, 24)) : topTable,
                ordersTitle, withPadding(ordersTable, new Insets(0, 24, 16, 24))
        );
        VBox.setVgrow(ordersTable, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:#f4f6f9; -fx-background:#f4f6f9;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(topBar, scroll);
        return new Scene(root, 950, 700);
    }

    private VBox withPadding(javafx.scene.Node node, Insets insets) {
        VBox box = new VBox(node);
        box.setPadding(insets);
        VBox.setVgrow(node, Priority.ALWAYS);
        return box;
    }

    private VBox summaryCard(String label, String value, String color) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(18, 20, 18, 20));
        card.setStyle(
                "-fx-background-color:#ffffff;" +
                        "-fx-background-radius:10;" +
                        "-fx-border-color:" + color + ";" +
                        "-fx-border-width:0 0 0 5;" +
                        "-fx-border-radius:0 10 10 0;"
        );

        Label valLbl = new Label(value);
        valLbl.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        valLbl.setTextFill(Color.web(color));

        Label lbl = new Label(label);
        lbl.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        lbl.setTextFill(Color.web("#666666"));

        card.getChildren().addAll(valLbl, lbl);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    // ── TOP ITEM MODEL ────────────────────────────────────────────
    public static class TopItem {
        private final javafx.beans.property.SimpleIntegerProperty rank, qtySold;
        private final javafx.beans.property.SimpleStringProperty  name, revenue;

        public TopItem(int rank, String name, int qtySold, String revenue) {
            this.rank    = new javafx.beans.property.SimpleIntegerProperty(rank);
            this.name    = new javafx.beans.property.SimpleStringProperty(name);
            this.qtySold = new javafx.beans.property.SimpleIntegerProperty(qtySold);
            this.revenue = new javafx.beans.property.SimpleStringProperty(revenue);
        }

        public int    getRank()    { return rank.get(); }
        public String getName()    { return name.get(); }
        public int    getQtySold() { return qtySold.get(); }
        public String getRevenue() { return revenue.get(); }
    }
}
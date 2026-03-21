package com.cafe.cafemanagementsystem;

import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import java.util.stream.Collectors;

public class OrderHistoryScreen {

    private final TableView<DataStore.Order>       table = new TableView<>();
    private final ObservableList<DataStore.Order>  data  = FXCollections.observableArrayList();
    private final TextArea detailArea   = new TextArea();
    private final Label    summaryLabel = new Label();
    private final Label    statusLabel  = new Label("Showing all orders");

    public Scene getScene() {
        data.setAll(DataStore.orders);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:" + HelloApplication.BG + ";");

        HBox topBar = HelloApplication.topBar("Order History");

        // ── FILTER BAR ────────────────────────────────────────────
        Label filterLabel = new Label("Filter by date (yyyy-MM-dd):");
        filterLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        TextField fromField = new TextField();
        fromField.setPromptText("From date");
        fromField.setPrefWidth(130);
        fromField.setStyle("-fx-font-size:13px; -fx-background-radius:6; -fx-border-color:#ccc; -fx-border-radius:6;");

        TextField toField = new TextField();
        toField.setPromptText("To date");
        toField.setPrefWidth(130);
        toField.setStyle("-fx-font-size:13px; -fx-background-radius:6; -fx-border-color:#ccc; -fx-border-radius:6;");

        Button filterBtn = HelloApplication.styledButton("Apply Filter", HelloApplication.BLUE, HelloApplication.WHITE);
        Button clearBtn  = HelloApplication.styledButton("Show All",     "#888", HelloApplication.WHITE);

        filterBtn.setOnAction(e -> {
            String from = fromField.getText().trim();
            String to   = toField.getText().trim();
            if (from.isEmpty() && to.isEmpty()) {
                data.setAll(DataStore.orders);
                statusLabel.setText("Showing all orders.");
                updateSummary();
                return;
            }
            var filtered = DataStore.orders.stream().filter(o -> {
                String d = o.dateTime.substring(0, 10);
                return (from.isEmpty() || d.compareTo(from) >= 0) &&
                        (to.isEmpty()   || d.compareTo(to)   <= 0);
            }).collect(Collectors.toList());
            data.setAll(filtered);
            statusLabel.setText("Found " + filtered.size() + " order(s).");
            updateSummary();
        });

        clearBtn.setOnAction(e -> {
            fromField.clear(); toField.clear();
            data.setAll(DataStore.orders);
            statusLabel.setText("Showing all orders.");
            updateSummary();
        });

        statusLabel.setFont(Font.font("Arial", 13));
        statusLabel.setTextFill(Color.GRAY);

        HBox filterBar = new HBox(10, filterLabel, fromField, new Label("to"), toField, filterBtn, clearBtn, statusLabel);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(12, 16, 12, 16));
        filterBar.setStyle("-fx-background-color:white; -fx-border-color:#e0e0e0; -fx-border-width:0 0 1 0;");

        // ── TABLE ─────────────────────────────────────────────────
        TableColumn<DataStore.Order, Integer> idCol = new TableColumn<>("Order ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(90);

        TableColumn<DataStore.Order, String> dtCol = new TableColumn<>("Date & Time");
        dtCol.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        dtCol.setPrefWidth(160);

        TableColumn<DataStore.Order, String> cashierCol = new TableColumn<>("Cashier");
        cashierCol.setCellValueFactory(new PropertyValueFactory<>("cashier"));
        cashierCol.setPrefWidth(110);

        TableColumn<DataStore.Order, Integer> itemsCol = new TableColumn<>("Items");
        itemsCol.setCellValueFactory(new PropertyValueFactory<>("itemsCount"));
        itemsCol.setPrefWidth(65);

        TableColumn<DataStore.Order, Double> totalCol = new TableColumn<>("Total ($)");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalCol.setPrefWidth(100);
        totalCol.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("$%.2f", v));
            }
        });

        TableColumn<DataStore.Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(80);
        statusCol.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (!empty && v != null) {
                    setText(v);
                    setStyle("-fx-text-fill:#1a7a4a; -fx-font-weight:bold;");
                } else { setText(null); setStyle(""); }
            }
        });

        table.getColumns().addAll(idCol, dtCol, cashierCol, itemsCol, totalCol, statusCol);
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-font-size:13px;");
        table.setPlaceholder(new Label("No orders found."));
        VBox.setVgrow(table, Priority.ALWAYS);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) showDetail(sel);
        });

        // ── SUMMARY ROW ───────────────────────────────────────────
        summaryLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        summaryLabel.setTextFill(Color.web(HelloApplication.BLUE));
        updateSummary();

        HBox summaryRow = new HBox(summaryLabel);
        summaryRow.setPadding(new Insets(8, 16, 8, 16));
        summaryRow.setStyle("-fx-background-color:#e8f0fc;");

        // ── DETAIL PANEL ──────────────────────────────────────────
        Label detailTitle = new Label("Order Detail");
        detailTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        detailTitle.setTextFill(Color.web(HelloApplication.BLUE));

        detailArea.setEditable(false);
        detailArea.setFont(Font.font("Courier New", 12));
        detailArea.setPrefHeight(200);
        detailArea.setPromptText("Select an order above to view its full details.");

        VBox detailPane = new VBox(6, detailTitle, detailArea);
        detailPane.setPadding(new Insets(10, 16, 12, 16));

        VBox tableSection = new VBox(0, table, summaryRow);
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox mainContent = new VBox(0, tableSection, detailPane);
        VBox.setVgrow(tableSection, Priority.ALWAYS);
        mainContent.setPadding(new Insets(12, 16, 4, 16));
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        root.getChildren().addAll(topBar, filterBar, mainContent);
        return new Scene(root, 950, 700);
    }

    private void showDetail(DataStore.Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Order ID  : #%d%n", order.id));
        sb.append(String.format("Cashier   : %s%n", order.cashier));
        sb.append(String.format("Date/Time : %s%n", order.dateTime));
        sb.append(String.format("Status    : %s%n", order.status));
        sb.append("─".repeat(52)).append("\n");
        sb.append(String.format("%-24s %5s %9s %9s%n", "Item", "Qty", "Price", "Subtotal"));
        sb.append("─".repeat(52)).append("\n");
        for (DataStore.OrderLine line : order.lines) {
            String name = line.item.getName();
            if (name.length() > 22) name = name.substring(0, 22);
            sb.append(String.format("%-24s %5d %9s %9s%n",
                    name, line.quantity,
                    String.format("$%.2f", line.item.getPrice()),
                    String.format("$%.2f", line.subtotal())));
        }
        double tax = order.total * 0.05;
        sb.append("─".repeat(52)).append("\n");
        sb.append(String.format("%-38s %9s%n", "Subtotal:", String.format("$%.2f", order.total - tax)));
        sb.append(String.format("%-38s %9s%n", "Tax (5%):", String.format("$%.2f", tax)));
        sb.append(String.format("%-38s %9s%n", "GRAND TOTAL:", String.format("$%.2f", order.total)));
        detailArea.setText(sb.toString());
    }

    private void updateSummary() {
        double total = data.stream().mapToDouble(o -> o.total).sum();
        summaryLabel.setText(String.format("  %d orders  |  Total Revenue: $%.2f", data.size(), total));
    }
}
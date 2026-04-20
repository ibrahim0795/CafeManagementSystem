package com.cafe.cafemanagementsystem;

import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import java.util.ArrayList;
import java.util.List;

public class NewOrderScreen {

    private final List<DataStore.OrderLine> currentOrder = new ArrayList<>();
    private final ObservableList<OrderRow>  orderData    = FXCollections.observableArrayList();
    private final TableView<OrderRow>       orderTable   = new TableView<>();
    private final Label totalLabel  = new Label("Total: $0.00");
    private final Label statusLabel = new Label("");

    public static class OrderRow {
        private final javafx.beans.property.SimpleStringProperty  itemName;
        private final javafx.beans.property.SimpleStringProperty  category;
        private final javafx.beans.property.SimpleIntegerProperty quantity;
        private final javafx.beans.property.SimpleDoubleProperty  unitPrice;
        private final javafx.beans.property.SimpleDoubleProperty  subtotal;

        public OrderRow(DataStore.OrderLine line) {
            this.itemName  = new javafx.beans.property.SimpleStringProperty(line.item.getName());
            this.category  = new javafx.beans.property.SimpleStringProperty(line.item.getCategory());
            this.quantity  = new javafx.beans.property.SimpleIntegerProperty(line.quantity);
            this.unitPrice = new javafx.beans.property.SimpleDoubleProperty(line.item.getPrice());
            this.subtotal  = new javafx.beans.property.SimpleDoubleProperty(line.subtotal());
        }

        public String getItemName()  { return itemName.get(); }
        public String getCategory()  { return category.get(); }
        public int    getQuantity()  { return quantity.get(); }
        public double getUnitPrice() { return unitPrice.get(); }
        public double getSubtotal()  { return subtotal.get(); }
        public javafx.beans.property.StringProperty  itemNameProperty()  { return itemName; }
        public javafx.beans.property.StringProperty  categoryProperty()  { return category; }
        public javafx.beans.property.IntegerProperty quantityProperty()  { return quantity; }
        public javafx.beans.property.DoubleProperty  unitPriceProperty() { return unitPrice; }
        public javafx.beans.property.DoubleProperty  subtotalProperty()  { return subtotal; }
    }

    public Scene getScene() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:#f4f6f9;");
        HBox topBar = HelloApplication.topBar("Create New Order");

        // ── LEFT: MENU ────────────────────────────────────────────
        Label menuTitle = new Label("Select Items");
        menuTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        menuTitle.setTextFill(Color.web("#1a3a6b"));

        TableView<DataStore.MenuItem> menuTable = new TableView<>();
        menuTable.setStyle("-fx-font-size:13px;");
        menuTable.setItems(FXCollections.observableArrayList(DataStore.menuItems));
        menuTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        menuTable.setPlaceholder(new Label("No menu items. Add items in Menu Management."));
        VBox.setVgrow(menuTable, Priority.ALWAYS);

        TableColumn<DataStore.MenuItem, String> nameCol = new TableColumn<>("Item");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<DataStore.MenuItem, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        TableColumn<DataStore.MenuItem, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("$%.2f", v));
            }
        });
        menuTable.getColumns().addAll(nameCol, catCol, priceCol);

        Label qtyLabel = new Label("Quantity:");
        qtyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        Spinner<Integer> qtySpinner = new Spinner<>(1, 50, 1);
        qtySpinner.setPrefWidth(90); qtySpinner.setStyle("-fx-font-size:13px;");

        Button addToOrderBtn = new Button("+ Add to Order");
        addToOrderBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        addToOrderBtn.setStyle("-fx-background-color:#1a3a6b;-fx-text-fill:white;-fx-padding:7 16;-fx-background-radius:6;-fx-cursor:hand;");

        addToOrderBtn.setOnAction(e -> {
            DataStore.MenuItem selected = menuTable.getSelectionModel().getSelectedItem();
            if (selected == null) { status("Select a menu item first.", "#c0392b"); return; }
            int qty = qtySpinner.getValue();
            for (DataStore.OrderLine line : currentOrder) {
                if (line.item.getId() == selected.getId()) {
                    line.quantity += qty;
                    refreshOrderTable();
                    status("Quantity updated: " + selected.getName(), "#1a7a4a");
                    return;
                }
            }
            currentOrder.add(new DataStore.OrderLine(selected, qty));
            refreshOrderTable();
            status("Added: " + selected.getName(), "#1a7a4a");
        });

        HBox addRow = new HBox(10, qtyLabel, qtySpinner, addToOrderBtn);
        addRow.setAlignment(Pos.CENTER_LEFT); addRow.setPadding(new Insets(10, 0, 0, 0));
        VBox leftPane = new VBox(10, menuTitle, menuTable, addRow);
        leftPane.setPadding(new Insets(16)); VBox.setVgrow(leftPane, Priority.ALWAYS);

        // ── RIGHT: CURRENT ORDER ──────────────────────────────────
        Label orderTitle = new Label("Current Order");
        orderTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        orderTitle.setTextFill(Color.web("#1a3a6b"));

        TableColumn<OrderRow, String>  oNameCol = new TableColumn<>("Item");
        oNameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        TableColumn<OrderRow, Integer> oQtyCol  = new TableColumn<>("Qty");
        oQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity")); oQtyCol.setPrefWidth(55);
        TableColumn<OrderRow, Double>  oUnitCol = new TableColumn<>("Unit Price");
        oUnitCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        oUnitCol.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(Double v, boolean empty) { super.updateItem(v, empty); setText(empty || v==null?null:String.format("$%.2f",v)); }
        });
        TableColumn<OrderRow, Double> oSubCol = new TableColumn<>("Subtotal");
        oSubCol.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        oSubCol.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(Double v, boolean empty) { super.updateItem(v, empty); setText(empty || v==null?null:String.format("$%.2f",v)); }
        });

        orderTable.getColumns().addAll(oNameCol, oQtyCol, oUnitCol, oSubCol);
        orderTable.setItems(orderData);
        orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        orderTable.setStyle("-fx-font-size:13px;");
        orderTable.setPlaceholder(new Label("No items added yet."));
        VBox.setVgrow(orderTable, Priority.ALWAYS);

        Button removeBtn = new Button("✕ Remove Selected");
        removeBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        removeBtn.setStyle("-fx-background-color:#c0392b;-fx-text-fill:white;-fx-padding:7 14;-fx-background-radius:6;-fx-cursor:hand;");
        removeBtn.setOnAction(e -> {
            int idx = orderTable.getSelectionModel().getSelectedIndex();
            if (idx >= 0) { currentOrder.remove(idx); refreshOrderTable(); status("Removed.", "#c0392b"); }
            else status("Select an item to remove.", "#c0392b");
        });

        totalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        totalLabel.setTextFill(Color.web("#1a3a6b"));
        totalLabel.setPadding(new Insets(6, 0, 6, 0));

        Button confirmBtn    = new Button("✔ Confirm & Generate Receipt");
        Button clearOrderBtn = new Button("Clear Order");
        confirmBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        confirmBtn.setStyle("-fx-background-color:#1a7a4a;-fx-text-fill:white;-fx-padding:8 16;-fx-background-radius:6;-fx-cursor:hand;");
        clearOrderBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        clearOrderBtn.setStyle("-fx-background-color:#888;-fx-text-fill:white;-fx-padding:8 16;-fx-background-radius:6;-fx-cursor:hand;");

        // ── CONFIRM WITH DB SAVE ──────────────────────────────────
        confirmBtn.setOnAction(e -> {
            if (currentOrder.isEmpty()) { status("Add at least one item.", "#c0392b"); return; }
            DataStore.Order order = new DataStore.Order(
                    DataStore.nextOrderId++, DataStore.currentUser.username, currentOrder
            );
            // Save to SQLite
            DatabaseManager.saveOrder(order);
            // Reload orders from DB to keep in sync
            DataStore.orders = DatabaseManager.loadOrders();
            showReceipt(order);
            currentOrder.clear(); refreshOrderTable();
            status("Order #" + order.id + " saved to database!", "#1a7a4a");
        });

        clearOrderBtn.setOnAction(e -> { currentOrder.clear(); refreshOrderTable(); status("Cleared.", "#888"); });
        statusLabel.setFont(Font.font("Arial", 13));

        HBox actionRow = new HBox(10, confirmBtn, clearOrderBtn);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        VBox rightPane = new VBox(10, orderTitle, orderTable, removeBtn, totalLabel, actionRow, statusLabel);
        rightPane.setPadding(new Insets(16)); rightPane.setPrefWidth(430);

        HBox content = new HBox(0, leftPane, new Separator(Orientation.VERTICAL), rightPane);
        HBox.setHgrow(leftPane, Priority.ALWAYS); VBox.setVgrow(content, Priority.ALWAYS);
        root.getChildren().addAll(topBar, content);
        return new Scene(root, 1050, 680);
    }

    private void refreshOrderTable() {
        orderData.clear();
        currentOrder.forEach(line -> orderData.add(new OrderRow(line)));
        double total = currentOrder.stream().mapToDouble(DataStore.OrderLine::subtotal).sum();
        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    private void status(String msg, String color) { statusLabel.setText(msg); statusLabel.setTextFill(Color.web(color)); }

    private void showReceipt(DataStore.Order order) {
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Receipt — Order #" + order.id);
        VBox box = new VBox(8); box.setPadding(new Insets(30)); box.setStyle("-fx-background-color:white;");

        Label header = new Label("☕  ByteBrew Cafe");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 20)); header.setTextFill(Color.web("#1a3a6b"));
        Label sub = new Label("Cafe Management System"); sub.setTextFill(Color.GRAY); sub.setFont(Font.font("Arial", 12));

        box.getChildren().addAll(header, sub, new Separator(),
                mono("Order ID  : #" + order.id), mono("Cashier   : " + order.cashier), mono("Date/Time : " + order.dateTime),
                new Separator(), monoBold(String.format("%-22s %4s %8s", "Item","Qty","Subtotal")), new Separator());

        for (DataStore.OrderLine line : order.lines) {
            String name = line.item.getName(); if (name.length()>20) name=name.substring(0,20);
            box.getChildren().add(mono(String.format("%-22s x%-3d $%6.2f", name, line.quantity, line.subtotal())));
        }
        double tax = order.total * 0.05;
        box.getChildren().addAll(new Separator(),
                mono(String.format("%-30s $%6.2f", "Subtotal:", order.total - tax)),
                mono(String.format("%-30s $%6.2f", "Tax (5%):", tax)),
                monoBold(String.format("%-30s $%6.2f", "GRAND TOTAL:", order.total)),
                new Separator());

        Label thanks = new Label("Thank you for visiting ByteBrew Cafe!"); thanks.setTextFill(Color.GRAY); thanks.setFont(Font.font("Arial", 12));
        Button closeBtn = new Button("Close");
        closeBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        closeBtn.setStyle("-fx-background-color:#1a3a6b;-fx-text-fill:white;-fx-padding:7 18;-fx-background-radius:6;-fx-cursor:hand;");
        closeBtn.setOnAction(e -> stage.close());
        box.getChildren().addAll(thanks, closeBtn);

        ScrollPane sp = new ScrollPane(box); sp.setFitToWidth(true);
        stage.setScene(new Scene(sp, 420, 520)); stage.show();
    }

    private Label mono(String text)     { Label l=new Label(text); l.setFont(Font.font("Courier New",13)); return l; }
    private Label monoBold(String text) { Label l=new Label(text); l.setFont(Font.font("Courier New",FontWeight.BOLD,13)); l.setTextFill(Color.web("#1a3a6b")); return l; }
}
package com.cafe.cafemanagementsystem;

import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

public class InventoryScreen {

    private final TableView<DataStore.InventoryItem>       table    = new TableView<>();
    private final ObservableList<DataStore.InventoryItem>  data     = FXCollections.observableArrayList();
    private final TextField nameField      = new TextField();
    private final TextField quantityField  = new TextField();
    private final TextField unitField      = new TextField();
    private final TextField thresholdField = new TextField();
    private final Label     statusLabel    = new Label("");

    public Scene getScene() {
        data.setAll(DataStore.inventory);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:" + HelloApplication.BG + ";");

        HBox topBar = HelloApplication.topBar("Inventory Management");

        // ── TABLE ─────────────────────────────────────────────────
        TableColumn<DataStore.InventoryItem, String> nameCol = new TableColumn<>("Ingredient");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(180);

        TableColumn<DataStore.InventoryItem, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(90);

        TableColumn<DataStore.InventoryItem, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));
        unitCol.setPrefWidth(80);

        TableColumn<DataStore.InventoryItem, Integer> threshCol = new TableColumn<>("Low Stock At");
        threshCol.setCellValueFactory(new PropertyValueFactory<>("lowStockThreshold"));
        threshCol.setPrefWidth(110);

        TableColumn<DataStore.InventoryItem, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null); setStyle("");
                } else {
                    DataStore.InventoryItem item = (DataStore.InventoryItem) getTableRow().getItem();
                    if (item.isLowStock()) {
                        setText("⚠ LOW STOCK");
                        setStyle("-fx-text-fill:#c0392b; -fx-font-weight:bold;");
                    } else {
                        setText("✓ OK");
                        setStyle("-fx-text-fill:#1a7a4a; -fx-font-weight:bold;");
                    }
                }
            }
        });

        table.getColumns().addAll(nameCol, qtyCol, unitCol, threshCol, statusCol);
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No inventory items."));
        table.setStyle("-fx-font-size:13px;");
        VBox.setVgrow(table, Priority.ALWAYS);

        // Low stock row highlight
        table.setRowFactory(tv -> new TableRow<>() {
            protected void updateItem(DataStore.InventoryItem item, boolean empty) {
                super.updateItem(item, empty);
                setStyle(item != null && item.isLowStock() ? "-fx-background-color:#fff5f5;" : "");
            }
        });

        // ── FORM ──────────────────────────────────────────────────
        styleField(nameField,      "e.g. Coffee Beans");
        styleField(quantityField,  "e.g. 50");
        styleField(unitField,      "e.g. kg");
        styleField(thresholdField, "e.g. 10");

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(10);
        form.setPadding(new Insets(16));
        form.setStyle("-fx-background-color:white; -fx-background-radius:10;");

        form.add(boldLabel("Name:"),         0, 0); form.add(nameField,      1, 0);
        form.add(boldLabel("Quantity:"),     0, 1); form.add(quantityField,  1, 1);
        form.add(boldLabel("Unit:"),         0, 2); form.add(unitField,      1, 2);
        form.add(boldLabel("Low Stock At:"), 0, 3); form.add(thresholdField, 1, 3);

        ColumnConstraints c1 = new ColumnConstraints(110);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c1, c2);

        Button addBtn    = HelloApplication.styledButton("+ Add",    HelloApplication.GREEN, HelloApplication.WHITE);
        Button updateBtn = HelloApplication.styledButton("✎ Update", HelloApplication.BLUE,  HelloApplication.WHITE);
        Button deleteBtn = HelloApplication.styledButton("✕ Delete", HelloApplication.RED,   HelloApplication.WHITE);
        Button clearBtn  = HelloApplication.styledButton("Clear",    "#888",                 HelloApplication.WHITE);

        statusLabel.setFont(Font.font("Arial", 13));

        HBox btnRow = new HBox(10, addBtn, updateBtn, deleteBtn, clearBtn, statusLabel);
        btnRow.setAlignment(Pos.CENTER_LEFT);
        btnRow.setPadding(new Insets(10, 16, 16, 16));

        // ── SELECTION ─────────────────────────────────────────────
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                nameField.setText(sel.getName());
                quantityField.setText(String.valueOf(sel.getQuantity()));
                unitField.setText(sel.getUnit());
                thresholdField.setText(String.valueOf(sel.getLowStockThreshold()));
            }
        });

        // ── ACTIONS ───────────────────────────────────────────────
        addBtn.setOnAction(e -> {
            if (!validate()) return;
            DataStore.InventoryItem item = new DataStore.InventoryItem(
                    DataStore.inventory.size() + 1,
                    nameField.getText().trim(),
                    Integer.parseInt(quantityField.getText().trim()),
                    unitField.getText().trim(),
                    Integer.parseInt(thresholdField.getText().trim())
            );
            DataStore.inventory.add(item);
            data.setAll(DataStore.inventory);
            clearForm();
            status("Item added.", HelloApplication.GREEN);
        });

        updateBtn.setOnAction(e -> {
            DataStore.InventoryItem sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { status("Select an item to update.", HelloApplication.RED); return; }
            if (!validate()) return;
            sel.setName(nameField.getText().trim());
            sel.setQuantity(Integer.parseInt(quantityField.getText().trim()));
            sel.setUnit(unitField.getText().trim());
            sel.setLowStockThreshold(Integer.parseInt(thresholdField.getText().trim()));
            data.setAll(DataStore.inventory);
            table.refresh();
            clearForm();
            status("Updated.", HelloApplication.GREEN);
        });

        deleteBtn.setOnAction(e -> {
            DataStore.InventoryItem sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { status("Select an item to delete.", HelloApplication.RED); return; }
            DataStore.inventory.remove(sel);
            data.setAll(DataStore.inventory);
            clearForm();
            status("Deleted.", HelloApplication.RED);
        });

        clearBtn.setOnAction(e -> clearForm());

        // ── LAYOUT ────────────────────────────────────────────────
        Label title = boldLabel("Stock Levels");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.web(HelloApplication.BLUE));

        Label legend = new Label("⚠ Red rows = low stock items");
        legend.setFont(Font.font("Arial", 12));
        legend.setTextFill(Color.web("#c0392b"));

        Label formTitle = boldLabel("Add / Update Stock");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        formTitle.setTextFill(Color.web(HelloApplication.BLUE));

        VBox leftPane = new VBox(6, title, legend, table);
        leftPane.setPadding(new Insets(16));
        VBox.setVgrow(leftPane, Priority.ALWAYS);

        VBox rightPane = new VBox(10, formTitle, form, btnRow);
        rightPane.setPadding(new Insets(16));
        rightPane.setPrefWidth(320);

        HBox content = new HBox(0, leftPane, new Separator(javafx.geometry.Orientation.VERTICAL), rightPane);
        HBox.setHgrow(leftPane, Priority.ALWAYS);
        VBox.setVgrow(content, Priority.ALWAYS);

        root.getChildren().addAll(topBar, content);
        return new Scene(root, 950, 680);
    }

    private Label boldLabel(String t) {
        Label l = new Label(t);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        return l;
    }

    private void styleField(TextField f, String prompt) {
        f.setPromptText(prompt);
        f.setPrefHeight(34);
        f.setStyle("-fx-font-size:13px; -fx-background-radius:6; -fx-border-color:#ccc; -fx-border-radius:6;");
    }

    private boolean validate() {
        if (nameField.getText().trim().isEmpty() || unitField.getText().trim().isEmpty()) {
            status("Name and Unit are required.", HelloApplication.RED); return false;
        }
        try {
            Integer.parseInt(quantityField.getText().trim());
            Integer.parseInt(thresholdField.getText().trim());
        } catch (Exception ex) {
            status("Quantity and threshold must be whole numbers.", HelloApplication.RED); return false;
        }
        return true;
    }

    private void clearForm() {
        nameField.clear(); quantityField.clear(); unitField.clear(); thresholdField.clear();
        table.getSelectionModel().clearSelection();
    }

    private void status(String msg, String color) {
        statusLabel.setText(msg);
        statusLabel.setTextFill(Color.web(color));
    }
}
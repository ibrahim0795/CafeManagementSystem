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

    private final TableView<DataStore.InventoryItem>      table = new TableView<>();
    private final ObservableList<DataStore.InventoryItem> data  = FXCollections.observableArrayList();
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

        // FIX for vanishing row: use a cell factory on the Status column only.
        // DO NOT use a rowFactory with background color — it conflicts with
        // JavaFX's selection highlight and makes the selected row invisible.
        // Instead we color only the Status cell text, and use a CSS stylesheet
        // trick to tint the entire row via the status cell's parent row lookup.
        TableColumn<DataStore.InventoryItem, Boolean> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(120);
        statusCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isLowStock())
        );
        statusCol.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(Boolean isLow, boolean empty) {
                super.updateItem(isLow, empty);
                if (empty || isLow == null) {
                    setText(null);
                    setStyle("");
                } else if (isLow) {
                    setText("⚠ LOW STOCK");
                    setStyle("-fx-text-fill:#c0392b; -fx-font-weight:bold;");
                } else {
                    setText("✓ OK");
                    setStyle("-fx-text-fill:#1a7a4a; -fx-font-weight:bold;");
                }
            }
        });

        table.getColumns().addAll(nameCol, qtyCol, unitCol, threshCol, statusCol);
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No inventory items."));
        table.setStyle("-fx-font-size:13px;");
        VBox.setVgrow(table, Priority.ALWAYS);

        // FIX: Row background via CSS on the row — but use pseudoClass trick so
        // selected rows still show the blue highlight on top.
        // We apply a subtle pink tint only when NOT selected.
        table.setRowFactory(tv -> new TableRow<DataStore.InventoryItem>() {
            @Override
            protected void updateItem(DataStore.InventoryItem item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null && item.isLowStock() && !isSelected()) {
                    setStyle("-fx-background-color:#fff0f0;");
                } else {
                    setStyle("");
                }
            }

            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                // Re-run updateItem logic when selection changes
                DataStore.InventoryItem item = getItem();
                if (item != null && item.isLowStock() && !selected) {
                    setStyle("-fx-background-color:#fff0f0;");
                } else {
                    setStyle("");
                }
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

        ColumnConstraints c1 = new ColumnConstraints(120);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c1, c2);

        // FIX: explicit actionButton() with visible text
        Button addBtn    = actionButton("+ Add Item", "#1a7a4a");
        Button updateBtn = actionButton("Update",     "#1a3a6b");
        Button deleteBtn = actionButton("Delete",     "#c0392b");
        Button clearBtn  = actionButton("Clear",      "#888888");

        statusLabel.setFont(Font.font("Arial", 13));

        HBox btnRow = new HBox(10, addBtn, updateBtn, deleteBtn, clearBtn);
        btnRow.setAlignment(Pos.CENTER_LEFT);
        btnRow.setPadding(new Insets(12, 16, 4, 16));

        HBox statusRow = new HBox(statusLabel);
        statusRow.setPadding(new Insets(0, 16, 16, 16));

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
            status("Item added.", "#1a7a4a");
        });

        updateBtn.setOnAction(e -> {
            DataStore.InventoryItem sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { status("Select an item to update.", "#c0392b"); return; }
            if (!validate()) return;
            sel.setName(nameField.getText().trim());
            sel.setQuantity(Integer.parseInt(quantityField.getText().trim()));
            sel.setUnit(unitField.getText().trim());
            sel.setLowStockThreshold(Integer.parseInt(thresholdField.getText().trim()));
            data.setAll(DataStore.inventory);
            table.refresh();
            clearForm();
            status("Updated successfully.", "#1a7a4a");
        });

        deleteBtn.setOnAction(e -> {
            DataStore.InventoryItem sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { status("Select an item to delete.", "#c0392b"); return; }
            DataStore.inventory.remove(sel);
            data.setAll(DataStore.inventory);
            clearForm();
            status("Deleted.", "#c0392b");
        });

        clearBtn.setOnAction(e -> clearForm());

        // ── LAYOUT ────────────────────────────────────────────────
        Label tableTitle = new Label("Stock Levels");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        tableTitle.setTextFill(Color.web(HelloApplication.BLUE));

        Label legend = new Label("⚠  Red rows = low stock items");
        legend.setFont(Font.font("Arial", 12));
        legend.setTextFill(Color.web("#c0392b"));

        Label formTitle = new Label("Add / Update Stock");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        formTitle.setTextFill(Color.web(HelloApplication.BLUE));

        VBox leftPane = new VBox(6, tableTitle, legend, table);
        leftPane.setPadding(new Insets(16));
        VBox.setVgrow(leftPane, Priority.ALWAYS);

        VBox rightPane = new VBox(10, formTitle, form, btnRow, statusRow);
        rightPane.setPadding(new Insets(16));
        rightPane.setPrefWidth(340);

        HBox content = new HBox(0, leftPane,
                new Separator(javafx.geometry.Orientation.VERTICAL), rightPane);
        HBox.setHgrow(leftPane, Priority.ALWAYS);
        VBox.setVgrow(content, Priority.ALWAYS);

        root.getChildren().addAll(topBar, content);
        return new Scene(root, 950, 680);
    }

    private Button actionButton(String label, String bgColor) {
        Button btn = new Button(label);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setMinWidth(Region.USE_PREF_SIZE);
        btn.setPrefHeight(34);
        btn.setStyle(
                "-fx-background-color:" + bgColor + ";" +
                        "-fx-text-fill:white;" +
                        "-fx-padding:6 14;" +
                        "-fx-background-radius:6;" +
                        "-fx-cursor:hand;"
        );
        btn.setOnMouseEntered(e -> btn.setOpacity(0.85));
        btn.setOnMouseExited(e  -> btn.setOpacity(1.0));
        return btn;
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
            status("Name and Unit are required.", "#c0392b"); return false;
        }
        try {
            Integer.parseInt(quantityField.getText().trim());
            Integer.parseInt(thresholdField.getText().trim());
        } catch (Exception ex) {
            status("Quantity and threshold must be whole numbers.", "#c0392b"); return false;
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
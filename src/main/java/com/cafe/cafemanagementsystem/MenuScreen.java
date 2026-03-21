package com.cafe.cafemanagementsystem;

import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

public class MenuScreen {

    private final TableView<DataStore.MenuItem>      table     = new TableView<>();
    private final ObservableList<DataStore.MenuItem> data      = FXCollections.observableArrayList();
    private final TextField nameField     = new TextField();
    private final TextField categoryField = new TextField();
    private final TextField priceField    = new TextField();
    private final Label     statusLabel   = new Label("");

    public Scene getScene() {
        data.setAll(DataStore.menuItems);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:" + HelloApplication.BG + ";");

        HBox topBar = HelloApplication.topBar("Menu Management");

        // ── TABLE ─────────────────────────────────────────────────
        TableColumn<DataStore.MenuItem, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<DataStore.MenuItem, String> nameCol = new TableColumn<>("Item Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<DataStore.MenuItem, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        catCol.setPrefWidth(130);

        TableColumn<DataStore.MenuItem, Double> priceCol = new TableColumn<>("Price ($)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(100);
        priceCol.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("$%.2f", v));
            }
        });

        table.getColumns().addAll(idCol, nameCol, catCol, priceCol);
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No menu items."));
        table.setStyle("-fx-font-size:13px;");
        VBox.setVgrow(table, Priority.ALWAYS);

        // ── FORM ──────────────────────────────────────────────────
        styleField(nameField,     "e.g. Espresso");
        styleField(categoryField, "e.g. Coffee");
        styleField(priceField,    "e.g. 2.50");

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(10);
        form.setPadding(new Insets(16));
        form.setStyle("-fx-background-color:white; -fx-background-radius:10;");

        form.add(boldLabel("Item Name:"), 0, 0); form.add(nameField,     1, 0);
        form.add(boldLabel("Category:"),  0, 1); form.add(categoryField, 1, 1);
        form.add(boldLabel("Price ($):"), 0, 2); form.add(priceField,    1, 2);

        ColumnConstraints c1 = new ColumnConstraints(110);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c1, c2);

        // FIX: Use actionButton() instead of styledButton() so text is never clipped
        Button addBtn    = actionButton("+ Add Item", "#1a7a4a");
        Button updateBtn = actionButton("Update",     "#1a3a6b");
        Button deleteBtn = actionButton("Delete",     "#c0392b");
        Button clearBtn  = actionButton("Clear",      "#888888");

        statusLabel.setFont(Font.font("Arial", 13));
        statusLabel.setPadding(new Insets(0, 0, 0, 8));

        HBox btnRow = new HBox(10, addBtn, updateBtn, deleteBtn, clearBtn);
        btnRow.setAlignment(Pos.CENTER_LEFT);
        btnRow.setPadding(new Insets(12, 16, 4, 16));

        HBox statusRow = new HBox(statusLabel);
        statusRow.setPadding(new Insets(0, 16, 16, 16));

        // ── SELECTION ─────────────────────────────────────────────
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                nameField.setText(sel.getName());
                categoryField.setText(sel.getCategory());
                priceField.setText(String.valueOf(sel.getPrice()));
            }
        });

        // ── ACTIONS ───────────────────────────────────────────────
        addBtn.setOnAction(e -> {
            if (!validate()) return;
            DataStore.MenuItem item = new DataStore.MenuItem(
                    DataStore.nextMenuId++,
                    nameField.getText().trim(),
                    categoryField.getText().trim(),
                    Double.parseDouble(priceField.getText().trim())
            );
            DataStore.menuItems.add(item);
            data.setAll(DataStore.menuItems);
            clearForm();
            status("Item added successfully.", "#1a7a4a");
        });

        updateBtn.setOnAction(e -> {
            DataStore.MenuItem sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { status("Select an item to update.", "#c0392b"); return; }
            if (!validate()) return;
            sel.setName(nameField.getText().trim());
            sel.setCategory(categoryField.getText().trim());
            sel.setPrice(Double.parseDouble(priceField.getText().trim()));
            data.setAll(DataStore.menuItems);
            clearForm();
            status("Item updated.", "#1a7a4a");
        });

        deleteBtn.setOnAction(e -> {
            DataStore.MenuItem sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { status("Select an item to delete.", "#c0392b"); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete '" + sel.getName() + "'?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    DataStore.menuItems.remove(sel);
                    data.setAll(DataStore.menuItems);
                    clearForm();
                    status("Item deleted.", "#c0392b");
                }
            });
        });

        clearBtn.setOnAction(e -> clearForm());

        // ── LAYOUT ────────────────────────────────────────────────
        Label tableTitle = new Label("Menu Items");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        tableTitle.setTextFill(Color.web(HelloApplication.BLUE));

        Label formTitle = new Label("Add / Edit Item");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        formTitle.setTextFill(Color.web(HelloApplication.BLUE));

        VBox leftPane = new VBox(8, tableTitle, table);
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

    // FIX: Build buttons manually so JavaFX never clips the label text
    private Button actionButton(String label, String bgColor) {
        Button btn = new Button(label);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setMinWidth(Region.USE_PREF_SIZE);   // never shrink below pref
        btn.setMaxWidth(Double.MAX_VALUE);
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
        if (nameField.getText().trim().isEmpty() || categoryField.getText().trim().isEmpty()) {
            status("Name and Category are required.", "#c0392b"); return false;
        }
        try { Double.parseDouble(priceField.getText().trim()); }
        catch (NumberFormatException ex) {
            status("Enter a valid price (e.g. 2.50).", "#c0392b"); return false;
        }
        return true;
    }

    private void clearForm() {
        nameField.clear(); categoryField.clear(); priceField.clear();
        table.getSelectionModel().clearSelection();
    }

    private void status(String msg, String color) {
        statusLabel.setText(msg);
        statusLabel.setTextFill(Color.web(color));
    }
}
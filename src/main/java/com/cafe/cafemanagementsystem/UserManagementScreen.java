package com.cafe.cafemanagementsystem;

import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

public class UserManagementScreen {

    private final TableView<DataStore.User>      table = new TableView<>();
    private final ObservableList<DataStore.User> data  = FXCollections.observableArrayList();
    private final TextField     usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final ComboBox<String> roleBox    = new ComboBox<>();
    private final Label statusLabel           = new Label("");

    public Scene getScene() {
        data.setAll(DataStore.users);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:#f4f6f9;");
        HBox topBar = HelloApplication.topBar("User Management");

        // ── TABLE ─────────────────────────────────────────────────
        TableColumn<DataStore.User, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().username));
        userCol.setPrefWidth(200);
        TableColumn<DataStore.User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().role));
        roleCol.setPrefWidth(150);
        TableColumn<DataStore.User, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(150);
        statusCol.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow()==null || getTableRow().getItem()==null) { setText(null); setStyle(""); return; }
                DataStore.User u = (DataStore.User) getTableRow().getItem();
                boolean isCurrent = DataStore.currentUser != null && u.username.equals(DataStore.currentUser.username);
                if (isCurrent) { setText("● Active (You)"); setStyle("-fx-text-fill:#1a7a4a;-fx-font-weight:bold;"); }
                else           { setText("● Registered");   setStyle("-fx-text-fill:#1a3a6b;"); }
            }
        });

        table.getColumns().addAll(userCol, roleCol, statusCol);
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No users found."));
        table.setStyle("-fx-font-size:13px;");
        VBox.setVgrow(table, Priority.ALWAYS);

        // ── FORM ──────────────────────────────────────────────────
        styleField(usernameField, "New username");
        passwordField.setPromptText("New password"); passwordField.setPrefHeight(34);
        passwordField.setStyle("-fx-font-size:13px;-fx-background-radius:6;-fx-border-color:#ccc;-fx-border-radius:6;");
        roleBox.getItems().addAll("Cashier", "Manager"); roleBox.setValue("Cashier"); roleBox.setPrefHeight(34);

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(10); form.setPadding(new Insets(16));
        form.setStyle("-fx-background-color:white;-fx-background-radius:10;");
        form.add(boldLabel("Username:"), 0, 0); form.add(usernameField, 1, 0);
        form.add(boldLabel("Password:"), 0, 1); form.add(passwordField, 1, 1);
        form.add(boldLabel("Role:"),     0, 2); form.add(roleBox,       1, 2);
        ColumnConstraints c1 = new ColumnConstraints(100);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c1, c2);

        Button addBtn    = actionButton("+ Add User",       "#1a7a4a");
        Button deleteBtn = actionButton("Delete Selected",  "#c0392b");
        Button clearBtn  = actionButton("Clear",            "#888888");
        statusLabel.setFont(Font.font("Arial", 13));

        HBox btnRow    = new HBox(10, addBtn, deleteBtn, clearBtn);
        btnRow.setAlignment(Pos.CENTER_LEFT); btnRow.setPadding(new Insets(10, 16, 4, 16));
        HBox statusRow = new HBox(statusLabel); statusRow.setPadding(new Insets(0, 16, 16, 16));

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) { usernameField.setText(sel.username); roleBox.setValue(sel.role); }
        });

        // ── ACTIONS WITH DB ───────────────────────────────────────
        addBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String role     = roleBox.getValue();
            if (username.isEmpty() || password.isEmpty()) { status("All fields are required.", "#c0392b"); return; }
            boolean exists = DataStore.users.stream().anyMatch(u -> u.username.equalsIgnoreCase(username));
            if (exists) { status("Username already exists.", "#c0392b"); return; }
            boolean saved = DatabaseManager.addUser(username, password, role);
            if (saved) {
                DataStore.users = DatabaseManager.loadUsers();
                data.setAll(DataStore.users); clearForm();
                status("User '" + username + "' added and saved.", "#1a7a4a");
            } else {
                status("Failed to add user (duplicate?).", "#c0392b");
            }
        });

        deleteBtn.setOnAction(e -> {
            DataStore.User sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { status("Select a user to delete.", "#c0392b"); return; }
            if (DataStore.currentUser != null && sel.username.equals(DataStore.currentUser.username)) {
                status("Cannot delete your own active account.", "#c0392b"); return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete user '" + sel.username + "'?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    DatabaseManager.deleteUser(sel.username);
                    DataStore.users = DatabaseManager.loadUsers();
                    data.setAll(DataStore.users); clearForm();
                    status("User deleted from database.", "#c0392b");
                }
            });
        });

        clearBtn.setOnAction(e -> clearForm());

        // ── LAYOUT ────────────────────────────────────────────────
        Label tableTitle = new Label("System Users");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16)); tableTitle.setTextFill(Color.web("#1a3a6b"));
        Label note = new Label("ℹ  You cannot delete your own currently active account.");
        note.setFont(Font.font("Arial", 12)); note.setTextFill(Color.web("#888888")); note.setPadding(new Insets(0,0,8,0));
        Label formTitle = new Label("Add New User");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16)); formTitle.setTextFill(Color.web("#1a3a6b"));

        VBox leftPane  = new VBox(8, tableTitle, note, table);
        leftPane.setPadding(new Insets(16)); VBox.setVgrow(leftPane, Priority.ALWAYS);
        VBox rightPane = new VBox(10, formTitle, form, btnRow, statusRow);
        rightPane.setPadding(new Insets(16)); rightPane.setPrefWidth(320);

        HBox content = new HBox(0, leftPane, new Separator(Orientation.VERTICAL), rightPane);
        HBox.setHgrow(leftPane, Priority.ALWAYS); VBox.setVgrow(content, Priority.ALWAYS);
        root.getChildren().addAll(topBar, content);
        return new Scene(root, 950, 680);
    }

    private Button actionButton(String label, String bg) {
        Button btn = new Button(label); btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setMinWidth(Region.USE_PREF_SIZE); btn.setPrefHeight(34);
        btn.setStyle("-fx-background-color:"+bg+";-fx-text-fill:white;-fx-padding:6 14;-fx-background-radius:6;-fx-cursor:hand;");
        btn.setOnMouseEntered(e -> btn.setOpacity(0.85)); btn.setOnMouseExited(e -> btn.setOpacity(1.0));
        return btn;
    }
    private Label boldLabel(String t) { Label l=new Label(t); l.setFont(Font.font("Arial",FontWeight.BOLD,13)); return l; }
    private void styleField(TextField f, String p) { f.setPromptText(p); f.setPrefHeight(34); f.setStyle("-fx-font-size:13px;-fx-background-radius:6;-fx-border-color:#ccc;-fx-border-radius:6;"); }
    private void clearForm() { usernameField.clear(); passwordField.clear(); roleBox.setValue("Cashier"); table.getSelectionModel().clearSelection(); }
    private void status(String msg, String color) { statusLabel.setText(msg); statusLabel.setTextFill(Color.web(color)); }
}
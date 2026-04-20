package com.cafe.cafemanagementsystem;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseManager — SQLite persistence layer for the Cafe Management System.
 * The database file (cafe.db) is created automatically in the app's working directory.
 * All CRUD operations for Users, MenuItems, InventoryItems, and Orders are handled here.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:cafe.db";
    private static Connection connection;

    // ─────────────────────────────────────────────────────────────
    // CONNECTION
    // ─────────────────────────────────────────────────────────────

    public static void connect() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
                connection.createStatement().execute("PRAGMA foreign_keys = ON");
                System.out.println("Connected to SQLite database: cafe.db");
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    public static void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // TABLE CREATION
    // ─────────────────────────────────────────────────────────────

    public static void createTables() {
        try (Statement stmt = connection.createStatement()) {

            // Users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    role     TEXT NOT NULL
                )
            """);

            // Menu items table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS menu_items (
                    id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    name     TEXT NOT NULL,
                    category TEXT NOT NULL,
                    price    REAL NOT NULL
                )
            """);

            // Inventory table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS inventory (
                    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
                    name                TEXT NOT NULL,
                    quantity            INTEGER NOT NULL,
                    unit                TEXT NOT NULL,
                    low_stock_threshold INTEGER NOT NULL
                )
            """);

            // Orders table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    id        INTEGER PRIMARY KEY,
                    cashier   TEXT NOT NULL,
                    date_time TEXT NOT NULL,
                    status    TEXT NOT NULL,
                    total     REAL NOT NULL
                )
            """);

            // Order lines table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS order_lines (
                    id           INTEGER PRIMARY KEY AUTOINCREMENT,
                    order_id     INTEGER NOT NULL,
                    menu_item_id INTEGER NOT NULL,
                    quantity     INTEGER NOT NULL,
                    unit_price   REAL NOT NULL,
                    FOREIGN KEY (order_id) REFERENCES orders(id)
                )
            """);

            System.out.println("All tables created/verified.");
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // SEED DEFAULT DATA (only if tables are empty)
    // ─────────────────────────────────────────────────────────────

    public static void seedDefaultData() {
        try {
            // Seed users
            ResultSet rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM users");
            if (rs.getInt(1) == 0) {
                String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    Object[][] users = {
                            {"manager", "manager123", "Manager"},
                            {"cashier", "cashier123", "Cashier"}
                    };
                    for (Object[] u : users) {
                        ps.setString(1, (String) u[0]);
                        ps.setString(2, (String) u[1]);
                        ps.setString(3, (String) u[2]);
                        ps.executeUpdate();
                    }
                }
                System.out.println("Default users seeded.");
            }

            // Seed menu items
            rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM menu_items");
            if (rs.getInt(1) == 0) {
                String sql = "INSERT INTO menu_items (name, category, price) VALUES (?, ?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    Object[][] items = {
                            {"Espresso",         "Coffee",  2.50},
                            {"Cappuccino",       "Coffee",  3.50},
                            {"Latte",            "Coffee",  3.75},
                            {"Green Tea",        "Tea",     2.00},
                            {"Chai Tea",         "Tea",     2.25},
                            {"Croissant",        "Bakery",  2.80},
                            {"Blueberry Muffin", "Bakery",  2.50},
                            {"Club Sandwich",    "Food",    5.50},
                            {"Caesar Salad",     "Food",    6.00},
                            {"Orange Juice",     "Drinks",  2.75},
                    };
                    for (Object[] item : items) {
                        ps.setString(1, (String) item[0]);
                        ps.setString(2, (String) item[1]);
                        ps.setDouble(3, (Double) item[2]);
                        ps.executeUpdate();
                    }
                }
                System.out.println("Default menu items seeded.");
            }

            // Seed inventory
            rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM inventory");
            if (rs.getInt(1) == 0) {
                String sql = "INSERT INTO inventory (name, quantity, unit, low_stock_threshold) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    Object[][] inv = {
                            {"Coffee Beans",  80, "kg",     10},
                            {"Milk",          40, "liters",  8},
                            {"Sugar",         25, "kg",      5},
                            {"Tea Bags",       8, "boxes",  10},
                            {"Flour",         15, "kg",      5},
                            {"Butter",         4, "kg",      5},
                            {"Orange Juice",  12, "liters",  5},
                            {"Bread",          3, "loaves",  5},
                    };
                    for (Object[] i : inv) {
                        ps.setString(1, (String) i[0]);
                        ps.setInt(2,    (Integer) i[1]);
                        ps.setString(3, (String) i[2]);
                        ps.setInt(4,    (Integer) i[3]);
                        ps.executeUpdate();
                    }
                }
                System.out.println("Default inventory seeded.");
            }

        } catch (SQLException e) {
            System.err.println("Error seeding data: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // USERS
    // ─────────────────────────────────────────────────────────────

    public static List<DataStore.User> loadUsers() {
        List<DataStore.User> list = new ArrayList<>();
        try (ResultSet rs = connection.createStatement()
                .executeQuery("SELECT username, password, role FROM users ORDER BY id")) {
            while (rs.next()) {
                list.add(new DataStore.User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            System.err.println("loadUsers error: " + e.getMessage());
        }
        return list;
    }

    public static boolean addUser(String username, String password, String role) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("addUser error: " + e.getMessage());
            return false; // likely duplicate username
        }
    }

    public static void deleteUser(String username) {
        String sql = "DELETE FROM users WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("deleteUser error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // MENU ITEMS
    // ─────────────────────────────────────────────────────────────

    public static List<DataStore.MenuItem> loadMenuItems() {
        List<DataStore.MenuItem> list = new ArrayList<>();
        try (ResultSet rs = connection.createStatement()
                .executeQuery("SELECT id, name, category, price FROM menu_items ORDER BY id")) {
            while (rs.next()) {
                list.add(new DataStore.MenuItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            System.err.println("loadMenuItems error: " + e.getMessage());
        }
        return list;
    }

    public static int addMenuItem(String name, String category, double price) {
        String sql = "INSERT INTO menu_items (name, category, price) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, category);
            ps.setDouble(3, price);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println("addMenuItem error: " + e.getMessage());
        }
        return -1;
    }

    public static void updateMenuItem(int id, String name, String category, double price) {
        String sql = "UPDATE menu_items SET name=?, category=?, price=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, category);
            ps.setDouble(3, price);
            ps.setInt(4, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("updateMenuItem error: " + e.getMessage());
        }
    }

    public static void deleteMenuItem(int id) {
        String sql = "DELETE FROM menu_items WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("deleteMenuItem error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // INVENTORY
    // ─────────────────────────────────────────────────────────────

    public static List<DataStore.InventoryItem> loadInventory() {
        List<DataStore.InventoryItem> list = new ArrayList<>();
        try (ResultSet rs = connection.createStatement()
                .executeQuery("SELECT id, name, quantity, unit, low_stock_threshold FROM inventory ORDER BY id")) {
            while (rs.next()) {
                list.add(new DataStore.InventoryItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getString("unit"),
                        rs.getInt("low_stock_threshold")
                ));
            }
        } catch (SQLException e) {
            System.err.println("loadInventory error: " + e.getMessage());
        }
        return list;
    }

    public static int addInventoryItem(String name, int quantity, String unit, int threshold) {
        String sql = "INSERT INTO inventory (name, quantity, unit, low_stock_threshold) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setInt(2, quantity);
            ps.setString(3, unit);
            ps.setInt(4, threshold);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println("addInventoryItem error: " + e.getMessage());
        }
        return -1;
    }

    public static void updateInventoryItem(int id, String name, int quantity, String unit, int threshold) {
        String sql = "UPDATE inventory SET name=?, quantity=?, unit=?, low_stock_threshold=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, quantity);
            ps.setString(3, unit);
            ps.setInt(4, threshold);
            ps.setInt(5, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("updateInventoryItem error: " + e.getMessage());
        }
    }

    public static void deleteInventoryItem(int id) {
        String sql = "DELETE FROM inventory WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("deleteInventoryItem error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ORDERS
    // ─────────────────────────────────────────────────────────────

    public static List<DataStore.Order> loadOrders() {
        List<DataStore.Order> list = new ArrayList<>();
        try {
            ResultSet rs = connection.createStatement()
                    .executeQuery("SELECT id, cashier, date_time, status, total FROM orders ORDER BY id DESC");

            while (rs.next()) {
                int orderId = rs.getInt("id");

                // Load order lines for this order
                List<DataStore.OrderLine> lines = new ArrayList<>();
                PreparedStatement linePs = connection.prepareStatement(
                        "SELECT ol.quantity, ol.unit_price, mi.id AS mi_id, mi.name, mi.category, mi.price " +
                                "FROM order_lines ol " +
                                "JOIN menu_items mi ON ol.menu_item_id = mi.id " +
                                "WHERE ol.order_id = ?"
                );
                linePs.setInt(1, orderId);
                ResultSet lineRs = linePs.executeQuery();
                while (lineRs.next()) {
                    DataStore.MenuItem item = new DataStore.MenuItem(
                            lineRs.getInt("mi_id"),
                            lineRs.getString("name"),
                            lineRs.getString("category"),
                            lineRs.getDouble("price")
                    );
                    lines.add(new DataStore.OrderLine(item, lineRs.getInt("quantity")));
                }

                // Reconstruct order from DB
                DataStore.Order order = new DataStore.Order(orderId, rs.getString("cashier"), lines);
                order.dateTime = rs.getString("date_time");
                order.status   = rs.getString("status");
                order.total    = rs.getDouble("total");
                list.add(order);
            }
        } catch (SQLException e) {
            System.err.println("loadOrders error: " + e.getMessage());
        }
        return list;
    }

    public static void saveOrder(DataStore.Order order) {
        String orderSql = "INSERT OR REPLACE INTO orders (id, cashier, date_time, status, total) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(orderSql)) {
            ps.setInt(1,    order.id);
            ps.setString(2, order.cashier);
            ps.setString(3, order.dateTime);
            ps.setString(4, order.status);
            ps.setDouble(5, order.total);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("saveOrder (header) error: " + e.getMessage());
            return;
        }

        String lineSql = "INSERT INTO order_lines (order_id, menu_item_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(lineSql)) {
            for (DataStore.OrderLine line : order.lines) {
                ps.setInt(1,    order.id);
                ps.setInt(2,    line.item.getId());
                ps.setInt(3,    line.quantity);
                ps.setDouble(4, line.item.getPrice());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("saveOrder (lines) error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // NEXT ORDER ID
    // ─────────────────────────────────────────────────────────────

    public static int getNextOrderId() {
        try (ResultSet rs = connection.createStatement()
                .executeQuery("SELECT COALESCE(MAX(id), 1000) + 1 FROM orders")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("getNextOrderId error: " + e.getMessage());
        }
        return 1001;
    }

    public static int getNextMenuId() {
        try (ResultSet rs = connection.createStatement()
                .executeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM menu_items")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("getNextMenuId error: " + e.getMessage());
        }
        return 1;
    }
}
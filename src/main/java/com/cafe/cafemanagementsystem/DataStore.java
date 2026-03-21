package com.cafe.cafemanagementsystem;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DataStore {

    // ─── USER ─────────────────────────────────────────────────────
    public static class User {
        public String username, password, role;
        public User(String u, String p, String r) { username=u; password=p; role=r; }
    }

    // ─── MENU ITEM with JavaFX Properties ────────────────────────
    public static class MenuItem {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty  name, category;
        private final SimpleDoubleProperty  price;

        public MenuItem(int id, String name, String category, double price) {
            this.id       = new SimpleIntegerProperty(id);
            this.name     = new SimpleStringProperty(name);
            this.category = new SimpleStringProperty(category);
            this.price    = new SimpleDoubleProperty(price);
        }

        public int    getId()       { return id.get(); }
        public String getName()     { return name.get(); }
        public String getCategory() { return category.get(); }
        public double getPrice()    { return price.get(); }

        public void setName(String v)     { name.set(v); }
        public void setCategory(String v) { category.set(v); }
        public void setPrice(double v)    { price.set(v); }

        public IntegerProperty idProperty()       { return id; }
        public StringProperty  nameProperty()     { return name; }
        public StringProperty  categoryProperty() { return category; }
        public DoubleProperty  priceProperty()    { return price; }
    }

    // ─── INVENTORY ITEM with JavaFX Properties ───────────────────
    public static class InventoryItem {
        private final SimpleIntegerProperty id, quantity, lowStockThreshold;
        private final SimpleStringProperty  name, unit;

        public InventoryItem(int id, String name, int qty, String unit, int threshold) {
            this.id                = new SimpleIntegerProperty(id);
            this.name              = new SimpleStringProperty(name);
            this.quantity          = new SimpleIntegerProperty(qty);
            this.unit              = new SimpleStringProperty(unit);
            this.lowStockThreshold = new SimpleIntegerProperty(threshold);
        }

        public int    getId()                { return id.get(); }
        public String getName()              { return name.get(); }
        public int    getQuantity()          { return quantity.get(); }
        public String getUnit()              { return unit.get(); }
        public int    getLowStockThreshold() { return lowStockThreshold.get(); }

        public void setName(String v)              { name.set(v); }
        public void setQuantity(int v)             { quantity.set(v); }
        public void setUnit(String v)              { unit.set(v); }
        public void setLowStockThreshold(int v)    { lowStockThreshold.set(v); }

        public boolean isLowStock() { return quantity.get() <= lowStockThreshold.get(); }

        public IntegerProperty idProperty()                { return id; }
        public StringProperty  nameProperty()              { return name; }
        public IntegerProperty quantityProperty()          { return quantity; }
        public StringProperty  unitProperty()              { return unit; }
        public IntegerProperty lowStockThresholdProperty() { return lowStockThreshold; }
    }

    // ─── ORDER LINE ───────────────────────────────────────────────
    public static class OrderLine {
        public MenuItem item;
        public int quantity;
        public OrderLine(MenuItem item, int qty) { this.item=item; this.quantity=qty; }
        public double subtotal() { return item.getPrice() * quantity; }
    }

    // ─── ORDER with JavaFX Properties ────────────────────────────
    public static class Order {
        public int    id;
        public String cashier, dateTime, status;
        public List<OrderLine> lines;
        public double total;

        public Order(int id, String cashier, List<OrderLine> lines) {
            this.id       = id;
            this.cashier  = cashier;
            this.lines    = new ArrayList<>(lines);
            this.dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            this.status   = "Billed";
            this.total    = lines.stream().mapToDouble(OrderLine::subtotal).sum();
        }

        public IntegerProperty idProperty()        { return new SimpleIntegerProperty(id); }
        public StringProperty  cashierProperty()   { return new SimpleStringProperty(cashier); }
        public StringProperty  dateTimeProperty()  { return new SimpleStringProperty(dateTime); }
        public StringProperty  statusProperty()    { return new SimpleStringProperty(status); }
        public DoubleProperty  totalProperty()     { return new SimpleDoubleProperty(total); }
        public IntegerProperty itemsCountProperty(){ return new SimpleIntegerProperty(lines.size()); }
    }

    // ─── GLOBAL DATA STORE ────────────────────────────────────────
    public static List<User>          users     = new ArrayList<>();
    public static List<MenuItem>      menuItems = new ArrayList<>();
    public static List<InventoryItem> inventory = new ArrayList<>();
    public static List<Order>         orders    = new ArrayList<>();
    public static int  nextMenuId  = 1;
    public static int  nextOrderId = 1001;
    public static User currentUser = null;

    // Call this from HelloApplication.start() to guarantee data loads
    public static void init() {
        if (!users.isEmpty()) return;

        users.add(new User("manager", "manager123", "Manager"));
        users.add(new User("cashier", "cashier123", "Cashier"));

        menuItems.add(new MenuItem(nextMenuId++, "Espresso",         "Coffee",  2.50));
        menuItems.add(new MenuItem(nextMenuId++, "Cappuccino",       "Coffee",  3.50));
        menuItems.add(new MenuItem(nextMenuId++, "Latte",            "Coffee",  3.75));
        menuItems.add(new MenuItem(nextMenuId++, "Green Tea",        "Tea",     2.00));
        menuItems.add(new MenuItem(nextMenuId++, "Chai Tea",         "Tea",     2.25));
        menuItems.add(new MenuItem(nextMenuId++, "Croissant",        "Bakery",  2.80));
        menuItems.add(new MenuItem(nextMenuId++, "Blueberry Muffin", "Bakery",  2.50));
        menuItems.add(new MenuItem(nextMenuId++, "Club Sandwich",    "Food",    5.50));
        menuItems.add(new MenuItem(nextMenuId++, "Caesar Salad",     "Food",    6.00));
        menuItems.add(new MenuItem(nextMenuId++, "Orange Juice",     "Drinks",  2.75));

        inventory.add(new InventoryItem(1, "Coffee Beans",    80, "kg",      10));
        inventory.add(new InventoryItem(2, "Milk",            40, "liters",   8));
        inventory.add(new InventoryItem(3, "Sugar",           25, "kg",       5));
        inventory.add(new InventoryItem(4, "Tea Bags",         8, "boxes",   10));
        inventory.add(new InventoryItem(5, "Flour",           15, "kg",       5));
        inventory.add(new InventoryItem(6, "Butter",           4, "kg",       5));
        inventory.add(new InventoryItem(7, "Orange Juice",    12, "liters",   5));
        inventory.add(new InventoryItem(8, "Bread",            3, "loaves",   5));
    }

    public static User login(String username, String password) {
        return users.stream()
                .filter(u -> u.username.equals(username) && u.password.equals(password))
                .findFirst().orElse(null);
    }
}
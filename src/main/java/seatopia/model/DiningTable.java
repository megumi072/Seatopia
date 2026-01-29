package seatopia.model;

public class DiningTable {
    private int id;
    private int restaurantId;
    private String name;
    private int capacity;
    private boolean active;

    public DiningTable(int id, int restaurantId, String name,
                       int capacity, boolean active) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.name = name;
        this.capacity = capacity;
        this.active = active;
    }

    public DiningTable(int restaurantId, String name, int capacity) {
        this(0, restaurantId, name, capacity, true);
    }

    public int getId() {
        return id;
    }

    public int getRestaurantId() {
        return restaurantId;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isActive() {
        return active;
    }
}

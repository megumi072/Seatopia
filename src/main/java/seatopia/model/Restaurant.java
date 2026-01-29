package seatopia.model;

public class Restaurant {
    private int id;
    private int userId;
    private String name;
    private String address;
    private String cuisineType;
    private String openingHours;

    public Restaurant(int id, int userId, String name,
                      String address, String cuisineType, String openingHours) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.address = address;
        this.cuisineType = cuisineType;
        this.openingHours = openingHours;
    }

    public Restaurant(int userId, String name,
                      String address, String cuisineType, String openingHours) {
        this(0, userId, name, address, cuisineType, openingHours);
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public String getOpeningHours() {
        return openingHours;
    }
}

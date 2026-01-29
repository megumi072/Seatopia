package seatopia.model;

public class Client {
    private int id;
    private int userId;
    private String name;
    private String phone;
    private double rating;
    private int noShowCount;
    private int completedCount;

    public Client(int id, int userId, String name, String phone,
                  double rating, int noShowCount, int completedCount) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.rating = rating;
        this.noShowCount = noShowCount;
        this.completedCount = completedCount;
    }

    public Client(int userId, String name, String phone) {
        this(0, userId, name, phone, 5.0, 0, 0);
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

    public String getPhone() {
        return phone;
    }

    public double getRating() {
        return rating;
    }

    public int getNoShowCount() {
        return noShowCount;
    }

    public int getCompletedCount() {
        return completedCount;
    }
}

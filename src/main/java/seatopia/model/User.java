package seatopia.model;

public class User {
    private int id;
    private String email;
    private String passwordHash;
    private UserRole role;

    public User(int id, String email, String passwordHash, UserRole role) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public User(String email, String passwordHash, UserRole role) {
        this(0, email, passwordHash, role);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }
}

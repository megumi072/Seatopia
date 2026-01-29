package seatopia.model;

import java.time.LocalDateTime;

public class Reservation {
    private int id;
    private int restaurantId;
    private int tableId;
    private int clientId;
    private LocalDateTime dateTime;
    private int peopleCount;
    private ReservationStatus status;

    public Reservation(int id, int restaurantId, int tableId, int clientId,
                       LocalDateTime dateTime, int peopleCount, ReservationStatus status) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.tableId = tableId;
        this.clientId = clientId;
        this.dateTime = dateTime;
        this.peopleCount = peopleCount;
        this.status = status;
    }

    public Reservation(int restaurantId, int tableId, int clientId,
                       LocalDateTime dateTime, int peopleCount) {
        this(0, restaurantId, tableId, clientId, dateTime, peopleCount, ReservationStatus.PENDING);
    }

    public int getId() {
        return id;
    }

    public int getRestaurantId() {
        return restaurantId;
    }

    public int getTableId() {
        return tableId;
    }

    public int getClientId() {
        return clientId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public int getPeopleCount() {
        return peopleCount;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}

package seatopia.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnumsTest {

    @Test
    void reservationStatus_containsPendingConfirmedCanceled() {
        assertNotNull(ReservationStatus.valueOf("PENDING"));
        assertNotNull(ReservationStatus.valueOf("CONFIRMED"));
        assertNotNull(ReservationStatus.valueOf("CANCELED"));
    }

    @Test
    void userRole_containsClientRestaurant() {
        assertNotNull(UserRole.valueOf("CLIENT"));
        assertNotNull(UserRole.valueOf("RESTAURANT"));
    }
}

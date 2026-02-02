package seatopia.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReservationServiceValidationTest {

    @Test
    void createReservation_throws_whenPeopleCountZeroOrNegative() {
        ReservationService service = new ReservationService();

        Exception ex = assertThrows(Exception.class, () ->
                service.createReservation(
                        1,
                        1,
                        LocalDateTime.now().plusDays(1),
                        0
                )
        );

        assertTrue(ex.getMessage().toLowerCase().contains("numărul de persoane"));
    }

    @Test
    void createReservation_throws_whenDateTimeNull() {
        ReservationService service = new ReservationService();

        Exception ex = assertThrows(Exception.class, () ->
                service.createReservation(
                        1,
                        1,
                        null,
                        2
                )
        );

        assertTrue(ex.getMessage().toLowerCase().contains("data/ora"));
    }

    @Test
    void createReservation_throws_whenReservationInThePast() {
        ReservationService service = new ReservationService();

        Exception ex = assertThrows(Exception.class, () ->
                service.createReservation(
                        1,
                        1,
                        LocalDateTime.now().minusMinutes(10),
                        2
                )
        );

        assertTrue(ex.getMessage().toLowerCase().contains("trecut"));
    }

    @Test
    void cancelReservation_throws_whenIdInvalidOrNotCancelable() {

        ReservationService service = new ReservationService();

        Exception ex = assertThrows(Exception.class, () ->
                service.cancelReservation(999999)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("nu poate fi anulată")
                || ex.getMessage().toLowerCase().contains("nu poate fi anulata"));
    }
}

package seatopia.service;

import seatopia.model.DiningTable;
import seatopia.model.Reservation;
import seatopia.model.ReservationStatus;
import seatopia.model.Restaurant;
import seatopia.repo.ClientRepo;
import seatopia.repo.ReservationRepo;
import seatopia.repo.RestaurantRepo;
import seatopia.repo.TableRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationService {

    private final RestaurantRepo restaurantRepo = new RestaurantRepo();
    private final TableRepo tableRepo = new TableRepo();
    private final ReservationRepo reservationRepo = new ReservationRepo();
    private final ClientRepo clientRepo = new ClientRepo();

    public List<Restaurant> listRestaurants() throws Exception {
        return restaurantRepo.findAll();
    }

    public List<Reservation> getClientReservations(int clientId) throws Exception {
        return reservationRepo.findByClientId(clientId);
    }

    public Reservation createReservation(int restaurantId, int clientId,
                                         LocalDateTime dateTime, int peopleCount) throws Exception {

        List<DiningTable> tables = tableRepo.findActiveByRestaurantId(restaurantId);

        for (DiningTable t : tables) {
            if (t.getCapacity() >= peopleCount && !reservationRepo.existsConflict(t.getId(), dateTime)) {

                Reservation reservation = new Reservation(
                        0,
                        restaurantId,
                        t.getId(),
                        clientId,
                        dateTime,
                        peopleCount,
                        ReservationStatus.CONFIRMED
                );

                return reservationRepo.save(reservation);
            }
        }

        throw new Exception("Nu există masă disponibilă pentru acel moment.");
    }

    public DiningTable addTable(int restaurantId, String name, int capacity) throws Exception {
        if (name == null || name.isBlank()) throw new Exception("Numele mesei este obligatoriu.");
        return tableRepo.save(new DiningTable(restaurantId, name.trim(), capacity));
    }

    public List<DiningTable> listActiveTables(int restaurantId) throws Exception {
        return tableRepo.findActiveByRestaurantId(restaurantId);
    }

    public List<Reservation> getRestaurantReservationsForDay(int restaurantId, LocalDate day) throws Exception {
        return reservationRepo.findByRestaurantAndDay(restaurantId, day);
    }

    public void markCompleted(int reservationId, int clientId) throws Exception {
        boolean ok = reservationRepo.updateStatusIfCurrent(
                reservationId, ReservationStatus.COMPLETED, ReservationStatus.CONFIRMED
        );
        if (!ok) {
            throw new Exception("Rezervarea nu poate fi marcată COMPLETED (deja finalizată/anulată sau nu e CONFIRMED).");
        }
        clientRepo.incrementCompleted(clientId);
    }

    public void markNoShow(int reservationId, int clientId) throws Exception {
        boolean ok = reservationRepo.updateStatusIfCurrent(
                reservationId, ReservationStatus.NO_SHOW, ReservationStatus.CONFIRMED
        );
        if (!ok) {
            throw new Exception("Rezervarea nu poate fi marcată NO_SHOW (deja finalizată/anulată sau nu e CONFIRMED).");
        }
        clientRepo.incrementNoShowAndUpdateRating(clientId);
    }
}

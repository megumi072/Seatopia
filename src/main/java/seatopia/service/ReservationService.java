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
import org.apache.commons.text.StringEscapeUtils;


public class ReservationService {

    private final RestaurantRepo restaurantRepo = new RestaurantRepo();
    private final TableRepo tableRepo = new TableRepo();
    private final ReservationRepo reservationRepo = new ReservationRepo();
    private final ClientRepo clientRepo = new ClientRepo();
    private final EmailService emailService = new EmailService();

    public List<Restaurant> listRestaurants() throws Exception {
        return restaurantRepo.findAll();
    }

    public List<Reservation> getClientReservations(int clientId) throws Exception {
        return reservationRepo.findByClientId(clientId);
    }

    public Reservation createReservation(int restaurantId, int clientId,
                                         LocalDateTime dateTime, int peopleCount) throws Exception {
        if (peopleCount <= 0) throw new Exception("Numărul de persoane trebuie să fie > 0.");
        if (dateTime == null) throw new Exception("Data/ora este obligatorie.");
        if (dateTime.isBefore(java.time.LocalDateTime.now())) throw new Exception("Nu poți face rezervare în trecut.");
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
                        ReservationStatus.PENDING
                );

                return reservationRepo.save(reservation);
            }
        }

        throw new Exception("Nu există masă disponibilă pentru acel moment.");
    }

    public DiningTable addTable(int restaurantId, String name, int capacity) throws Exception {
        if (name == null || name.isBlank()) throw new Exception("Numele mesei este obligatoriu.");
        String clean = name.trim();
        if (capacity <= 0) throw new Exception("Capacitatea trebuie să fie > 0.");

        if (tableRepo.existsByRestaurantAndName(restaurantId, clean)) {
            throw new Exception("Există deja o masă cu acest nume.");
        }

        return tableRepo.save(new DiningTable(restaurantId, clean, capacity));
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
    public void cancelReservation(int reservationId) throws Exception {
        boolean ok = reservationRepo.updateStatusIfCurrent(
                reservationId, ReservationStatus.CANCELED, ReservationStatus.CONFIRMED
        );

        if (!ok) {
            ok = reservationRepo.updateStatusIfCurrent(
                    reservationId, ReservationStatus.CANCELED, ReservationStatus.PENDING
            );
        }

        if (!ok) {
            throw new Exception("Rezervarea nu poate fi anulată (deja finalizată/anulată sau nu e activă).");
        }
    }
    public void confirmReservation(int reservationId) throws Exception {
        boolean ok = reservationRepo.updateStatusIfCurrent(
                reservationId, ReservationStatus.CONFIRMED, ReservationStatus.PENDING
        );

        if (!ok) {
            throw new Exception("Rezervarea nu poate fi confirmată (nu este PENDING).");
        }

        Reservation r = reservationRepo.findById(reservationId);
        if (r == null) return;

        Restaurant rest = restaurantRepo.findById(r.getRestaurantId());
        String clientEmail = clientRepo.findEmailByClientId(r.getClientId());
        if (rest == null || clientEmail == null || clientEmail.isBlank()) return;

        // 🔎 găsim masa ca să avem numele ei
        DiningTable table = null;
        for (DiningTable t : tableRepo.findActiveByRestaurantId(r.getRestaurantId())) {
            if (t.getId() == r.getTableId()) {
                table = t;
                break;
            }
        }

        String tableName = (table == null)
                ? ("Masa #" + r.getTableId())
                : table.getName();

        int ppl = r.getPeopleCount();
        String persoane = (ppl == 1) ? "1 persoană" : (ppl + " persoane");

        String date = r.getDateTime().toLocalDate().toString();
        String time = String.format("%02d:%02d", r.getDateTime().getHour(), r.getDateTime().getMinute());

        String restNameSafe = escapeHtml(rest.getName());
        String tableNameSafe = escapeHtml(tableName);

        emailService.sendEmail(
                clientEmail,
                "Rezervarea ta a fost confirmată 🎉",
                """
                <div style="font-family:Segoe UI,Arial,sans-serif; font-size:14px; line-height:1.6;">
                  <h2 style="margin:0 0 10px;">Rezervare confirmată ✅</h2>
                  <p>Rezervarea ta a fost confirmată cu succes!</p>
    
                  <div style="padding:12px; border:1px solid #eee; border-radius:10px;">
                    <p style="margin:0;"><b>Restaurant:</b> %s</p>
                    <p style="margin:0;"><b>Data:</b> %s</p>
                    <p style="margin:0;"><b>Ora:</b> %s</p>
                    <p style="margin:0;"><b>Persoane:</b> %s</p>
                    <p style="margin:0;"><b>Masa:</b> %s</p>
                  </div>
    
                  <p style="margin-top:14px;">Vă așteptăm cu drag! 😊</p>
                  <p style="margin-top:18px;"><b>— %s & Echipa Seatopia</b></p>
                </div>
                """.formatted(
                        restNameSafe,
                        date,
                        time,
                        persoane,
                        tableNameSafe,
                        restNameSafe
                )
        );
    }



    public String formatReservationForClient(Reservation r) throws Exception {
        Restaurant rest = restaurantRepo.findById(r.getRestaurantId());
        String restName = (rest == null) ? ("Restaurant #" + r.getRestaurantId()) : rest.getName();
        String restAddr = (rest == null || rest.getAddress() == null) ? "" : (" — " + rest.getAddress());

        return "#" + r.getId()
                + " | " + restName + restAddr
                + " | " + r.getDateTime()
                + " | " + r.getPeopleCount() + " persoane"
                + " | status=" + r.getStatus();
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
    public void cancelPendingReservation(int reservationId, String message) throws Exception {
        if (message == null) message = "";
        message = message.trim();
        if (message.length() > 200) {
            message = message.substring(0, 200);
        }

        boolean ok = reservationRepo.cancelIfPending(reservationId, message);
        if (!ok) {
            throw new Exception("Doar rezervările PENDING pot fi anulate.");
        }
    }
    public void rejectPendingReservation(int reservationId, String message) throws Exception {
        if (message == null) message = "";
        message = message.trim();
        if (message.length() > 200) message = message.substring(0, 200);

        boolean ok = reservationRepo.cancelIfPending(reservationId, message);
        if (!ok) {
            throw new Exception("Rezervarea nu poate fi respinsă (nu este PENDING).");
        }
    }
    public String formatReservationForRestaurant(Reservation r) throws Exception {
        String tableText = "tableId=" + r.getTableId();
        for (DiningTable t : tableRepo.findActiveByRestaurantId(r.getRestaurantId())) {
            if (t.getId() == r.getTableId()) {
                tableText = t.getName() + " (cap " + t.getCapacity() + ")";
                break;
            }
        }
        var client = clientRepo.findById(r.getClientId());
        String clientText = (client == null) ? ("clientId=" + r.getClientId())
                : (client.getName() + " (" + client.getPhone() + ")");

        String base = "#" + r.getId()
                + " | " + r.getDateTime()
                + " | " + tableText
                + " | " + clientText
                + " | " + r.getPeopleCount() + " persoane"
                + " | " + r.getStatus();

        if (r.getStatus() == ReservationStatus.CANCELED) {
            String msg = reservationRepo.getCancelMessage(r.getId());
            if (msg != null && !msg.isBlank()) {
                base += " | motiv: " + msg.trim();
            }
        }

        return base;
    }
    public void updateTable(int tableId, int restaurantId, String name, int capacity) throws Exception {
        if (name == null || name.isBlank()) throw new Exception("Numele mesei este obligatoriu.");
        String clean = name.trim();
        if (capacity <= 0) throw new Exception("Capacitatea trebuie să fie > 0.");

        if (tableRepo.existsByRestaurantAndNameExcludingId(restaurantId, clean, tableId)) {
            throw new Exception("Există deja o masă cu acest nume.");
        }

        tableRepo.update(tableId, clean, capacity);
    }


    public void removeTable(int tableId) throws Exception {
        tableRepo.deactivate(tableId);
    }
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

}

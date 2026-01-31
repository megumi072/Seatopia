package seatopia.service;

import seatopia.service.EmailService;
import seatopia.model.User;
import seatopia.model.UserRole;
import seatopia.repo.ClientRepo;
import seatopia.repo.RestaurantRepo;
import seatopia.repo.UserRepo;
import seatopia.model.Client;
import seatopia.model.Restaurant;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class AuthService {

    private final UserRepo userRepo = new UserRepo();
    private final ClientRepo clientRepo = new ClientRepo();
    private final RestaurantRepo restaurantRepo = new RestaurantRepo();
    private final EmailService emailService = new EmailService();

    public static class Session {
        private final User user;
        private final Integer clientId;
        private final Integer restaurantId;

        public Session(User user, Integer clientId, Integer restaurantId) {
            this.user = user;
            this.clientId = clientId;
            this.restaurantId = restaurantId;
        }

        public User getUser() {
            return user;
        }

        public Integer getClientId() {
            return clientId;
        }

        public Integer getRestaurantId() {
            return restaurantId;
        }
    }

    public Session login(String email, String password) throws Exception {
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new Exception("Email inexistent");
        }

        String hash = hashPassword(password);
        if (!hash.equals(user.getPasswordHash())) {
            throw new Exception("Parolă incorectă");
        }

        if (user.getRole() == UserRole.CLIENT) {
            Client client = clientRepo.findByUserId(user.getId());
            if (client == null) throw new Exception("Profil client lipsă (DB inconsistent).");
            return new Session(user, client.getId(), null);
        } else {
            Restaurant restaurant = restaurantRepo.findByUserId(user.getId());
            if (restaurant == null) throw new Exception("Profil restaurant lipsă (DB inconsistent).");
            return new Session(user, null, restaurant.getId());
        }

    }

    public Session registerClient(String email, String password,
                                  String name, String phone) throws Exception {

        if (userRepo.findByEmail(email) != null) {
            throw new Exception("Email deja folosit");
        }
        User user = new User(email, hashPassword(password), UserRole.CLIENT);
        userRepo.save(user);

        Client client = new Client(user.getId(), name, phone);
        Client savedClient = clientRepo.save(client);
        emailService.sendEmail(
                email,
                "Bun venit pe Seatopia 🍽️",
                """
                <div style="font-family:Segoe UI,Arial,sans-serif; font-size:14px; line-height:1.6;">
                  <h2 style="margin:0 0 10px;">Bun venit pe Seatopia, %s 👋</h2>
                  <p>Contul tău a fost creat cu succes.</p>
                  <p>De acum poți trimite cereri de rezervare și vei primi confirmări pe email.</p>
                  <p style="margin-top:14px;">Îți dorim experiențe culinare minunate! 🍝✨</p>
                  <p style="margin-top:18px;"><b>— Echipa Seatopia</b></p>
                </div>
                """.formatted(escapeHtml(name))
        );

        if (savedClient == null) throw new Exception("Eroare la creare client.");

        return new Session(user, savedClient.getId(), null);

    }

    public Session registerRestaurant(String email, String password,
                                      String name, String address,
                                      String cuisineType, String openingHours) throws Exception {

        if (userRepo.findByEmail(email) != null) {
            throw new Exception("Email deja folosit");
        }

        User user = new User(email, hashPassword(password), UserRole.RESTAURANT);
        userRepo.save(user);

        Restaurant restaurant = new Restaurant(
                user.getId(), name, address, cuisineType, openingHours
        );
        restaurantRepo.save(restaurant);

        emailService.sendEmail(
                email,
                "Bun venit pe Seatopia 🍽️",
                """
                <div style="font-family:Segoe UI,Arial,sans-serif; font-size:14px; line-height:1.6;">
                  <h2>Bun venit pe Seatopia, %s! 🎉</h2>
    
                  <p>Restaurantul tău a fost creat cu succes pe platforma Seatopia.</p>
    
                  <div style="padding:12px; border:1px solid #eee; border-radius:10px;">
                    <p style="margin:0;"><b>Nume:</b> %s</p>
                    <p style="margin:0;"><b>Adresă:</b> %s</p>
                    <p style="margin:0;"><b>Tip bucătărie:</b> %s</p>
                    <p style="margin:0;"><b>Program:</b> %s</p>
                  </div>
    
                  <p style="margin-top:14px;">
                    De acum poți adăuga mese, gestiona rezervări și comunica direct cu clienții tăi.
                  </p>
    
                  <p style="margin-top:18px;">
                    Mult succes! 🚀<br>
                    <b>— Echipa Seatopia</b>
                  </p>
                </div>
                """.formatted(
                        name,
                        name,
                        address,
                        cuisineType == null || cuisineType.isBlank() ? "-" : cuisineType,
                        openingHours == null || openingHours.isBlank() ? "-" : openingHours
                )
        );

        return new Session(user, null, restaurant.getId());
    }


    private String hashPassword(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

}

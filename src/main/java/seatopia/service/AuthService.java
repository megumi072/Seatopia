package seatopia.service;

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
            return new Session(user, client.getId(), null);
        } else {
            Restaurant restaurant = restaurantRepo.findByUserId(user.getId());
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
        clientRepo.save(client);

        return new Session(user, client.getId(), null);
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
}

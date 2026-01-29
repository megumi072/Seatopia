package seatopia.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seatopia.model.Restaurant;
import seatopia.service.AuthService;
import seatopia.service.ReservationService;

import java.time.LocalDateTime;

public class ClientView {

    private final BorderPane root = new BorderPane();
    private final ReservationService reservationService = new ReservationService();

    public ClientView(Stage stage, AuthService.Session session) {
        root.setPadding(new Insets(15));

        Button logoutBtn = new Button("Logout");
        Label title = new Label("Client Dashboard");
        HBox top = new HBox(10, title, logoutBtn);
        top.setPadding(new Insets(5));
        root.setTop(top);

        ListView<Restaurant> restaurantsList = new ListView<>();
        restaurantsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Restaurant r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) {
                    setText("");
                } else {
                    String cuisine = (r.getCuisineType() == null || r.getCuisineType().isBlank()) ? "" : (" | " + r.getCuisineType());
                    setText(r.getName() + " — " + r.getAddress() + cuisine);
                }
            }
        });

        TextField dateTimeField = new TextField("2026-01-14T19:30");
        Spinner<Integer> peopleSpinner = new Spinner<>(1, 20, 2);

        Button reserveBtn = new Button("Rezervă");
        Button myReservationsBtn = new Button("Rezervările mele");

        Label message = new Label();

        VBox right = new VBox(10,
                new Label("Creează rezervare"),
                new Label("DateTime (YYYY-MM-DDTHH:MM)"),
                dateTimeField,
                new Label("Nr persoane"),
                peopleSpinner,
                reserveBtn,
                myReservationsBtn,
                message
        );
        right.setPadding(new Insets(10));
        right.setPrefWidth(320);

        root.setCenter(restaurantsList);
        root.setRight(right);

        refreshRestaurants(restaurantsList, message);

        logoutBtn.setOnAction(e -> stage.getScene().setRoot(new LoginView(stage).getRoot()));

        reserveBtn.setOnAction(e -> {
            Restaurant selected = restaurantsList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                message.setText("Selectează un restaurant.");
                return;
            }

            try {
                LocalDateTime dt = LocalDateTime.parse(dateTimeField.getText().trim());
                int people = peopleSpinner.getValue();

                var reservation = reservationService.createReservation(
                        selected.getId(),
                        session.getClientId(),
                        dt,
                        people
                );

                message.setText("Rezervare creată: #" + reservation.getId() +
                        " | tableId=" + reservation.getTableId() +
                        " | " + reservation.getStatus());
            } catch (Exception ex) {
                message.setText("Eroare: " + ex.getMessage());
            }
        });

        myReservationsBtn.setOnAction(e ->
                stage.getScene().setRoot(new MyReservationsView(stage, session).getRoot())
        );
    }

    private void refreshRestaurants(ListView<Restaurant> restaurantsList, Label message) {
        try {
            restaurantsList.getItems().setAll(reservationService.listRestaurants());
            if (restaurantsList.getItems().isEmpty()) {
                message.setText("Nu există restaurante încă. Creează unul din cont de restaurant.");
            }
        } catch (Exception ex) {
            message.setText("Eroare la încărcare restaurante: " + ex.getMessage());
        }
    }

    public Parent getRoot() {
        return root;
    }
}

package seatopia.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import seatopia.model.Restaurant;
import seatopia.service.AuthService;
import seatopia.service.ReservationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ClientView {

    private final BorderPane root = new BorderPane();
    private final ReservationService reservationService = new ReservationService();

    private final ObservableList<Restaurant> allRestaurants = FXCollections.observableArrayList();

    public ClientView(Stage stage, AuthService.Session session) {
        root.setPadding(new Insets(18));

        Label title = new Label("Client Dashboard");
        title.getStyleClass().add("h2");

        Button myResBtn = new Button("Rezervările mele");
        myResBtn.getStyleClass().addAll("button", "secondary");

        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().addAll("button", "secondary");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(10, title, spacer, myResBtn, logoutBtn);
        top.getStyleClass().add("topbar");
        root.setTop(top);

        TextField searchField = new TextField();
        searchField.setPromptText("Caută restaurant (nume / adresă / cuisine)...");

        ListView<Restaurant> restaurantsList = new ListView<>();
        restaurantsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Restaurant r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) {
                    setText("");
                } else {
                    String cuisine = (r.getCuisineType() == null || r.getCuisineType().isBlank())
                            ? ""
                            : (" • " + r.getCuisineType());
                    setText(r.getName() + cuisine + "\n" + r.getAddress());
                }
            }
        });

        searchField.textProperty().addListener((obs, oldV, newV) ->
                applyRestaurantFilter(restaurantsList, newV)
        );

        Label restaurantsTitle = new Label("Restaurante");
        restaurantsTitle.getStyleClass().add("h2");

        VBox restaurantsCard = new VBox(10, restaurantsTitle, searchField, restaurantsList);
        restaurantsCard.getStyleClass().add("card");
        VBox.setVgrow(restaurantsList, Priority.ALWAYS);

        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
        datePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) return;
                setDisable(item.isBefore(LocalDate.now()));
            }
        });

        var hours = FXCollections.observableArrayList(
                "00","01","02","03","04","05","06","07","08","09",
                "10","11","12","13","14","15","16","17","18","19",
                "20","21","22","23"
        );
        var minutes = FXCollections.observableArrayList("00", "15", "30", "45");

        ComboBox<String> hourBox = new ComboBox<>(hours);
        hourBox.setValue("19");

        ComboBox<String> minuteBox = new ComboBox<>(minutes);
        minuteBox.setValue("30");

        Spinner<Integer> peopleSpinner = new Spinner<>(1, 20, 2);
        peopleSpinner.setEditable(true);

        Button reserveBtn = new Button("Trimite cererea (PENDING)");
        reserveBtn.getStyleClass().addAll("button", "primary");

        Label message = new Label(" ");
        message.getStyleClass().add("message");

        Label formTitle = new Label("Creează rezervare");
        formTitle.getStyleClass().add("h2");

        HBox timeRow = new HBox(8, hourBox, new Label(":"), minuteBox);

        VBox form = new VBox(10,
                formTitle,
                new Label("Data"), datePicker,
                new Label("Ora"), timeRow,
                new Label("Nr persoane"), peopleSpinner,
                reserveBtn,
                message
        );
        form.getStyleClass().add("card");
        form.setPrefWidth(320);

        HBox content = new HBox(14, restaurantsCard, form);
        HBox.setHgrow(restaurantsCard, Priority.ALWAYS);
        root.setCenter(content);
        refreshRestaurants(restaurantsList, message);

        logoutBtn.setOnAction(e -> stage.getScene().setRoot(new LoginView(stage).getRoot()));

        myResBtn.setOnAction(e ->
                stage.getScene().setRoot(new MyReservationsView(stage, session).getRoot())
        );

        reserveBtn.setOnAction(e -> {
            Restaurant selected = restaurantsList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                message.setText("Selectează un restaurant.");
                return;
            }

            try {
                LocalDate d = datePicker.getValue();
                if (d == null) throw new Exception("Selectează o dată.");

                int hour = Integer.parseInt(hourBox.getValue());
                int minute = Integer.parseInt(minuteBox.getValue());
                LocalDateTime dt = LocalDateTime.of(d, LocalTime.of(hour, minute));

                int people = peopleSpinner.getValue();

                var reservation = reservationService.createReservation(
                        selected.getId(),
                        session.getClientId(),
                        dt,
                        people
                );

                message.setText("Cerere trimisă (PENDING): #" + reservation.getId() + " • " + reservation.getDateTime());
            } catch (Exception ex) {
                message.setText("Eroare: " + ex.getMessage());
            }
        });
    }

    private void refreshRestaurants(ListView<Restaurant> restaurantsList, Label message) {
        try {
            allRestaurants.setAll(reservationService.listRestaurants());
            applyRestaurantFilter(restaurantsList, ""); // show all
            if (allRestaurants.isEmpty()) {
                message.setText("Nu există restaurante încă. Creează unul din cont de restaurant.");
            } else {
                message.setText(" ");
            }
        } catch (Exception ex) {
            message.setText("Eroare la încărcare restaurante: " + ex.getMessage());
        }
    }

    private void applyRestaurantFilter(ListView<Restaurant> restaurantsList, String q) {
        String query = (q == null) ? "" : q.trim().toLowerCase();
        if (query.isEmpty()) {
            restaurantsList.getItems().setAll(allRestaurants);
            return;
        }

        restaurantsList.getItems().setAll(
                allRestaurants.filtered(r -> {
                    String name = safe(r.getName());
                    String addr = safe(r.getAddress());
                    String cui = safe(r.getCuisineType());
                    return name.contains(query) || addr.contains(query) || cui.contains(query);
                })
        );
    }

    private String safe(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    public Parent getRoot() {
        return root;
    }
}

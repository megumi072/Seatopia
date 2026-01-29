package seatopia.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seatopia.model.DiningTable;
import seatopia.model.Reservation;
import seatopia.model.ReservationStatus;
import seatopia.service.AuthService;
import seatopia.service.ReservationService;

import java.time.LocalDate;

public class RestaurantView {

    private final BorderPane root = new BorderPane();
    private final ReservationService reservationService = new ReservationService();

    public RestaurantView(Stage stage, AuthService.Session session) {
        root.setPadding(new Insets(15));

        Label title = new Label("Restaurant Dashboard");
        Button logoutBtn = new Button("Logout");
        HBox top = new HBox(10, title, logoutBtn);
        root.setTop(top);

        ListView<DiningTable> tablesList = new ListView<>();
        tablesList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(DiningTable t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? "" : t.getName() + " | cap=" + t.getCapacity());
            }
        });

        TextField tableNameField = new TextField();
        Spinner<Integer> capacitySpinner = new Spinner<>(1, 20, 4);
        Button addTableBtn = new Button("Adaugă masă");

        VBox left = new VBox(10,
                new Label("Mese"),
                tablesList,
                new Label("Nume masă"),
                tableNameField,
                new Label("Capacitate"),
                capacitySpinner,
                addTableBtn
        );
        left.setPadding(new Insets(10));
        left.setPrefWidth(300);

        ListView<Reservation> reservationsList = new ListView<>();
        reservationsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Reservation r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? "" :
                        "#" + r.getId()
                                + " | " + r.getDateTime()
                                + " | tableId=" + r.getTableId()
                                + " | clientId=" + r.getClientId()
                                + " | ppl=" + r.getPeopleCount()
                                + " | " + r.getStatus());
            }
        });

        DatePicker dayPicker = new DatePicker(LocalDate.now());
        Button refreshBtn = new Button("Refresh");
        Button completedBtn = new Button("Mark COMPLETED");
        Button noShowBtn = new Button("Mark NO_SHOW");

        Label message = new Label();

        HBox actionsTop = new HBox(10, new Label("Zi:"), dayPicker, refreshBtn);
        HBox actionsBottom = new HBox(10, completedBtn, noShowBtn);

        VBox center = new VBox(10,
                new Label("Rezervări"),
                actionsTop,
                reservationsList,
                actionsBottom,
                message
        );
        center.setPadding(new Insets(10));

        root.setLeft(left);
        root.setCenter(center);

        refreshTables(session.getRestaurantId(), tablesList, message);
        refreshReservations(session.getRestaurantId(), reservationsList, message, dayPicker.getValue());

        logoutBtn.setOnAction(e -> stage.getScene().setRoot(new LoginView(stage).getRoot()));

        addTableBtn.setOnAction(e -> {
            try {
                reservationService.addTable(
                        session.getRestaurantId(),
                        tableNameField.getText().trim(),
                        capacitySpinner.getValue()
                );
                tableNameField.clear();
                refreshTables(session.getRestaurantId(), tablesList, message);
                message.setText("Masă adăugată.");
            } catch (Exception ex) {
                message.setText("Eroare: " + ex.getMessage());
            }
        });

        refreshBtn.setOnAction(e ->
                refreshReservations(session.getRestaurantId(), reservationsList, message, dayPicker.getValue())
        );

        dayPicker.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                refreshReservations(session.getRestaurantId(), reservationsList, message, newV);
            }
        });

        reservationsList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, r) -> {
            if (r == null) {
                completedBtn.setDisable(true);
                noShowBtn.setDisable(true);
                return;
            }
            boolean terminal = r.getStatus() == ReservationStatus.COMPLETED
                    || r.getStatus() == ReservationStatus.NO_SHOW
                    || r.getStatus() == ReservationStatus.CANCELED;
            completedBtn.setDisable(terminal);
            noShowBtn.setDisable(terminal);
        });
        completedBtn.setDisable(true);
        noShowBtn.setDisable(true);

        completedBtn.setOnAction(e -> {
            Reservation r = reservationsList.getSelectionModel().getSelectedItem();
            if (r == null) {
                message.setText("Selectează o rezervare.");
                return;
            }
            try {
                reservationService.markCompleted(r.getId(), r.getClientId());
                refreshReservations(session.getRestaurantId(), reservationsList, message, dayPicker.getValue());
                message.setText("Rezervare marcată COMPLETED.");
            } catch (Exception ex) {
                message.setText("Eroare: " + ex.getMessage());
            }
        });

        noShowBtn.setOnAction(e -> {
            Reservation r = reservationsList.getSelectionModel().getSelectedItem();
            if (r == null) {
                message.setText("Selectează o rezervare.");
                return;
            }
            try {
                reservationService.markNoShow(r.getId(), r.getClientId());
                refreshReservations(session.getRestaurantId(), reservationsList, message, dayPicker.getValue());
                message.setText("Rezervare marcată NO_SHOW (rating client scăzut).");
            } catch (Exception ex) {
                message.setText("Eroare: " + ex.getMessage());
            }
        });
    }

    private void refreshTables(int restaurantId, ListView<DiningTable> list, Label msg) {
        try {
            list.getItems().setAll(reservationService.listActiveTables(restaurantId));
        } catch (Exception ex) {
            msg.setText("Eroare mese: " + ex.getMessage());
        }
    }

    private void refreshReservations(int restaurantId, ListView<Reservation> list, Label msg, LocalDate day) {
        try {
            if (day == null) day = LocalDate.now();
            list.getItems().setAll(reservationService.getRestaurantReservationsForDay(restaurantId, day));
        } catch (Exception ex) {
            msg.setText("Eroare rezervări: " + ex.getMessage());
        }
    }

    public Parent getRoot() {
        return root;
    }
}

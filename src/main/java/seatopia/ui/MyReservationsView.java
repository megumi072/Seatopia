package seatopia.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import seatopia.model.Reservation;
import seatopia.model.ReservationStatus;
import seatopia.service.AuthService;
import seatopia.service.ReservationService;

public class MyReservationsView {

    private final BorderPane root = new BorderPane();
    private final ReservationService reservationService = new ReservationService();

    public MyReservationsView(Stage stage, AuthService.Session session) {
        root.setPadding(new Insets(18));

        Label title = new Label("Rezervările mele");
        title.getStyleClass().add("h2");

        Button backBtn = new Button("Înapoi");
        backBtn.getStyleClass().addAll("button", "secondary");

        Button cancelBtn = new Button("Anulează rezervarea");
        cancelBtn.getStyleClass().addAll("button", "danger");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(10, title, spacer, cancelBtn, backBtn);
        top.getStyleClass().add("topbar");
        root.setTop(top);

        ListView<Reservation> list = new ListView<>();
        list.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Reservation r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) {
                    setText("");
                } else {
                    try {
                        setText(reservationService.formatReservationForClient(r));
                    } catch (Exception ex) {
                        setText("#" + r.getId()
                                + " • " + r.getDateTime()
                                + " | " + r.getPeopleCount() + " persoane"
                                + " • " + r.getStatus());
                    }
                }
            }
        });

        Label message = new Label(" ");
        message.getStyleClass().add("message");

        VBox card = new VBox(10, list, message);
        card.getStyleClass().add("card");
        VBox.setVgrow(list, Priority.ALWAYS);

        root.setCenter(card);

        refresh(list, message, session);

        backBtn.setOnAction(e ->
                stage.getScene().setRoot(new ClientView(stage, session).getRoot())
        );

        cancelBtn.setDisable(true);
        list.getSelectionModel().selectedItemProperty().addListener((obs, oldV, r) -> {
            if (r == null) {
                cancelBtn.setDisable(true);
                return;
            }
            boolean canCancel = r.getStatus() == ReservationStatus.PENDING
                    || r.getStatus() == ReservationStatus.CONFIRMED;
            cancelBtn.setDisable(!canCancel);
        });

        cancelBtn.setOnAction(e -> {
            Reservation r = list.getSelectionModel().getSelectedItem();
            if (r == null) {
                showMessage(message, "Selectează o rezervare.");
                return;
            }

            boolean canCancel = r.getStatus() == ReservationStatus.PENDING
                    || r.getStatus() == ReservationStatus.CONFIRMED;

            if (!canCancel) {
                showMessage(message, "Rezervarea nu poate fi anulată (este deja finalizată/anulată).");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmare anulare");
            confirm.setHeaderText("Sigur vrei să anulezi rezervarea #" + r.getId() + "?");
            confirm.setContentText("Rezervarea va fi marcată drept CANCELED.");

            var result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) return;

            try {
                reservationService.cancelReservation(r.getId());
                showMessage(message, "Rezervarea #" + r.getId() + " a fost anulată.");
                refresh(list, message, session);
            } catch (Exception ex) {
                showMessage(message, "Eroare: " + ex.getMessage());
            }
        });
    }

    private void refresh(ListView<Reservation> list, Label message, AuthService.Session session) {
        try {
            list.getItems().setAll(reservationService.getClientReservations(session.getClientId()));
            list.getSelectionModel().clearSelection();

            if (list.getItems().isEmpty()) {
                showMessage(message, "Nu ai rezervări încă.");
            } else {

                message.setVisible(false);
                message.setManaged(false);
                message.setText("");
            }
        } catch (Exception ex) {
            showMessage(message, "Eroare: " + ex.getMessage());
        }
    }

    private void showMessage(Label message, String text) {
        message.setText(text);
        message.setVisible(true);
        message.setManaged(true);
    }

    public Parent getRoot() {
        return root;
    }
}

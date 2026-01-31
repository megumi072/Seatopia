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

        Button cancelBtn = new Button("Anulează (doar PENDING)");
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

        Label message = new Label();
        message.getStyleClass().add("message");
        message.setVisible(false);
        message.setManaged(false);

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
            cancelBtn.setDisable(r == null || r.getStatus() != ReservationStatus.PENDING);
        });

        cancelBtn.setOnAction(e -> {
            Reservation r = list.getSelectionModel().getSelectedItem();
            if (r == null) {
                message.setText("Selectează o rezervare.");
                return;
            }
            if (r.getStatus() != ReservationStatus.PENDING) {
                message.setText("Doar rezervările PENDING pot fi anulate.");
                return;
            }

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Anulare rezervare");
            dialog.setHeaderText("Mesaj (opțional) pentru anulare:");
            dialog.setContentText("Mesaj:");

            var result = dialog.showAndWait();
            if (result.isEmpty()) return;

            try {
                reservationService.cancelPendingReservation(r.getId(), result.get());
                message.setText("Rezervarea #" + r.getId() + " a fost anulată.");
                refresh(list, message, session);
            } catch (Exception ex) {
                message.setText("Eroare: " + ex.getMessage());
            }
        });
    }

    private void refresh(ListView<Reservation> list, Label message, AuthService.Session session) {
        try {
            list.getItems().setAll(reservationService.getClientReservations(session.getClientId()));
            list.getSelectionModel().clearSelection();
            if (list.getItems().isEmpty()) message.setText("Nu ai rezervări încă.");
        } catch (Exception ex) {
            message.setText("Eroare: " + ex.getMessage());
        }
    }

    public Parent getRoot() {
        return root;
    }
}

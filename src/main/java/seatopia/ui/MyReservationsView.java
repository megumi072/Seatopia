package seatopia.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import seatopia.model.Reservation;
import seatopia.service.AuthService;
import seatopia.service.ReservationService;

public class MyReservationsView {

    private final BorderPane root = new BorderPane();
    private final ReservationService reservationService = new ReservationService();

    public MyReservationsView(Stage stage, AuthService.Session session) {
        root.setPadding(new Insets(15));

        Button backBtn = new Button("Înapoi");
        Label title = new Label("Rezervările mele");
        HBox top = new HBox(10, title, backBtn);
        root.setTop(top);

        ListView<Reservation> list = new ListView<>();
        list.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Reservation r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) {
                    setText("");
                } else {
                    setText("#" + r.getId()
                            + " | restId=" + r.getRestaurantId()
                            + " | " + r.getDateTime()
                            + " | ppl=" + r.getPeopleCount()
                            + " | status=" + r.getStatus());
                }
            }
        });

        Label message = new Label();
        root.setCenter(list);
        root.setBottom(message);

        try {
            list.getItems().setAll(reservationService.getClientReservations(session.getClientId()));
        } catch (Exception ex) {
            message.setText("Eroare: " + ex.getMessage());
        }

        backBtn.setOnAction(e -> stage.getScene().setRoot(new ClientView(stage, session).getRoot()));
    }

    public Parent getRoot() {
        return root;
    }
}

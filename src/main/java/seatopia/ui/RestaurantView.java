package seatopia.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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

    private final ObservableList<DiningTable> allTables = FXCollections.observableArrayList();

    public RestaurantView(Stage stage, AuthService.Session session) {
        root.setPadding(new Insets(18));

        Label title = new Label("Restaurant Dashboard");
        title.getStyleClass().add("h2");

        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().addAll("button", "secondary");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(10, title, spacer, logoutBtn);
        top.getStyleClass().add("topbar");
        root.setTop(top);

        ListView<DiningTable> tablesList = new ListView<>();
        tablesList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(DiningTable t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? "" : t.getName() + " • cap " + t.getCapacity());
            }
        });

        TextField tableSearchField = new TextField();
        tableSearchField.setPromptText("Caută masă (ex: Masa 1)...");

        tableSearchField.textProperty().addListener((obs, oldV, newV) ->
                applyTableFilter(tablesList, newV)
        );

        TextField tableNameField = new TextField();
        tableNameField.setPromptText("ex: Masa 1");

        Spinner<Integer> capacitySpinner = new Spinner<>(1, 20, 4);
        capacitySpinner.setEditable(true);

        Button addTableBtn = new Button("Adaugă masă");
        addTableBtn.getStyleClass().addAll("button", "primary");

        Button updateTableBtn = new Button("Modifică");
        updateTableBtn.getStyleClass().addAll("button", "secondary");

        Button removeTableBtn = new Button("Șterge masă");
        removeTableBtn.getStyleClass().addAll("button", "danger");

        HBox tableActions = new HBox(10, updateTableBtn, removeTableBtn);

        VBox left = new VBox(10,
                labelH2("Mese"),
                tableSearchField,
                tablesList,
                new Label("Nume masă"), tableNameField,
                new Label("Capacitate"), capacitySpinner,
                addTableBtn,
                tableActions
        );
        left.getStyleClass().add("card");
        left.setPrefWidth(340);
        VBox.setVgrow(tablesList, Priority.ALWAYS);

        ListView<Reservation> reservationsList = new ListView<>();
        reservationsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Reservation r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) {
                    setText("");
                } else {
                    try {
                        setText(reservationService.formatReservationForRestaurant(r));
                    } catch (Exception ex) {
                        setText("#" + r.getId()
                                + " • " + r.getDateTime()
                                + " • tableId=" + r.getTableId()
                                + " • clientId=" + r.getClientId()
                                + " • " + r.getPeopleCount() + " persoane"
                                + " • " + r.getStatus());
                    }
                }
            }
        });

        DatePicker dayPicker = new DatePicker(LocalDate.now());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().addAll("button", "secondary");

        Button confirmBtn = new Button("Confirmă");
        confirmBtn.getStyleClass().addAll("button", "primary");

        Button rejectBtn = new Button("Respinge");
        rejectBtn.getStyleClass().addAll("button", "danger");

        Button completedBtn = new Button("COMPLETED");
        completedBtn.getStyleClass().addAll("button", "secondary");

        Button noShowBtn = new Button("NO_SHOW");
        noShowBtn.getStyleClass().addAll("button", "secondary");

        Label message = new Label(" ");
        message.getStyleClass().add("message");

        HBox actionsTop = new HBox(10, new Label("Zi:"), dayPicker, refreshBtn);
        HBox actionsBottom = new HBox(10, confirmBtn, rejectBtn, completedBtn, noShowBtn);

        VBox center = new VBox(10,
                labelH2("Rezervări"),
                actionsTop,
                reservationsList,
                actionsBottom,
                message
        );
        center.getStyleClass().add("card");
        VBox.setVgrow(reservationsList, Priority.ALWAYS);

        HBox content = new HBox(14, left, center);
        HBox.setHgrow(center, Priority.ALWAYS);
        root.setCenter(content);

        refreshTables(session.getRestaurantId(), tablesList, message, tableSearchField.getText());
        refreshReservations(session.getRestaurantId(), reservationsList, message, dayPicker.getValue());

        logoutBtn.setOnAction(e -> stage.getScene().setRoot(new LoginView(stage).getRoot()));

        updateTableBtn.setDisable(true);
        removeTableBtn.setDisable(true);

        tablesList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, t) -> {
            if (t == null) {
                updateTableBtn.setDisable(true);
                removeTableBtn.setDisable(true);
                return;
            }

            tableNameField.setText(t.getName());
            capacitySpinner.getValueFactory().setValue(t.getCapacity());

            updateTableBtn.setDisable(false);
            removeTableBtn.setDisable(false);
        });

        addTableBtn.setOnAction(e -> {
            try {
                reservationService.addTable(
                        session.getRestaurantId(),
                        tableNameField.getText().trim(),
                        capacitySpinner.getValue()
                );
                tableNameField.clear();
                tablesList.getSelectionModel().clearSelection();
                refreshTables(session.getRestaurantId(), tablesList, message, tableSearchField.getText());
                message.setText("Masă adăugată.");
            } catch (Exception ex) {
                message.setText("Eroare: " + ex.getMessage());
            }
        });

        updateTableBtn.setOnAction(e -> {
            DiningTable t = tablesList.getSelectionModel().getSelectedItem();
            if (t == null) {
                message.setText("Selectează o masă.");
                return;
            }
            try {
                reservationService.updateTable(
                        t.getId(),
                        session.getRestaurantId(),
                        tableNameField.getText().trim(),
                        capacitySpinner.getValue()
                );
                refreshTables(session.getRestaurantId(), tablesList, message, tableSearchField.getText());
                message.setText("Masă modificată.");
            } catch (Exception ex) {
                message.setText("Eroare: " + ex.getMessage());
            }
        });

        removeTableBtn.setOnAction(e -> {
            DiningTable t = tablesList.getSelectionModel().getSelectedItem();
            if (t == null) {
                message.setText("Selectează o masă.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmare");
            confirm.setHeaderText("Scoți masa \"" + t.getName() + "\"?");
            confirm.setContentText("Masa va fi dezactivată (nu se mai poate rezerva), dar istoricul rezervărilor rămâne.");

            var result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) return;

            try {
                reservationService.removeTable(t.getId());
                tableNameField.clear();
                tablesList.getSelectionModel().clearSelection();
                refreshTables(session.getRestaurantId(), tablesList, message, tableSearchField.getText());
                message.setText("Masă ștearsă (dezactivată).");
            } catch (Exception ex) {
                message.setText("Eroare: " + ex.getMessage());
            }
        });

        refreshBtn.setOnAction(e ->
                refreshReservations(session.getRestaurantId(), reservationsList, message, dayPicker.getValue())
        );

        dayPicker.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) refreshReservations(session.getRestaurantId(), reservationsList, message, newV);
        });

        confirmBtn.setDisable(true);
        rejectBtn.setDisable(true);
        completedBtn.setDisable(true);
        noShowBtn.setDisable(true);

        reservationsList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, r) -> {
            if (r == null) {
                confirmBtn.setDisable(true);
                rejectBtn.setDisable(true);
                completedBtn.setDisable(true);
                noShowBtn.setDisable(true);
                return;
            }

            boolean isPending = r.getStatus() == ReservationStatus.PENDING;
            boolean isConfirmed = r.getStatus() == ReservationStatus.CONFIRMED;

            confirmBtn.setDisable(!isPending);
            rejectBtn.setDisable(!isPending);

            completedBtn.setDisable(!isConfirmed);
            noShowBtn.setDisable(!isConfirmed);
        });

        confirmBtn.setOnAction(e -> {
            Reservation r = reservationsList.getSelectionModel().getSelectedItem();
            if (r == null) {
                message.setText("Selectează o rezervare.");
                return;
            }
            try {
                reservationService.confirmReservation(r.getId());
                refreshReservations(session.getRestaurantId(), reservationsList, message, dayPicker.getValue());
                message.setText("Rezervare confirmată.");
            } catch (Exception ex) {
                message.setText("Eroare: " + ex.getMessage());
            }
        });

        rejectBtn.setOnAction(e -> {
            Reservation r = reservationsList.getSelectionModel().getSelectedItem();
            if (r == null) {
                message.setText("Selectează o rezervare.");
                return;
            }
            if (r.getStatus() != ReservationStatus.PENDING) {
                message.setText("Doar rezervările PENDING pot fi respinse.");
                return;
            }

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Respinge rezervare");
            dialog.setHeaderText("Mesaj (opțional) pentru respingere:");
            dialog.setContentText("Mesaj:");

            var result = dialog.showAndWait();
            if (result.isEmpty()) return;

            try {
                reservationService.rejectPendingReservation(r.getId(), result.get());
                refreshReservations(session.getRestaurantId(), reservationsList, message, dayPicker.getValue());
                message.setText("Rezervare respinsă.");
            } catch (Exception ex) {
                message.setText("Eroare: " + ex.getMessage());
            }
        });

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

    private Label labelH2(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("h2");
        return l;
    }

    private void refreshTables(int restaurantId, ListView<DiningTable> list, Label msg, String currentQuery) {
        try {
            allTables.setAll(reservationService.listActiveTables(restaurantId));
            applyTableFilter(list, currentQuery);
        } catch (Exception ex) {
            msg.setText("Eroare mese: " + ex.getMessage());
        }
    }

    private void applyTableFilter(ListView<DiningTable> list, String q) {
        String query = (q == null) ? "" : q.trim().toLowerCase();
        if (query.isEmpty()) {
            list.getItems().setAll(allTables);
            return;
        }

        list.getItems().setAll(
                allTables.filtered(t ->
                        t.getName() != null && t.getName().toLowerCase().contains(query)
                )
        );
    }

    private void refreshReservations(int restaurantId, ListView<Reservation> list, Label msg, LocalDate day) {
        try {
            if (day == null) day = LocalDate.now();
            list.getItems().setAll(reservationService.getRestaurantReservationsForDay(restaurantId, day));
            list.getSelectionModel().clearSelection();
        } catch (Exception ex) {
            msg.setText("Eroare rezervări: " + ex.getMessage());
        }
    }

    public Parent getRoot() {
        return root;
    }
}

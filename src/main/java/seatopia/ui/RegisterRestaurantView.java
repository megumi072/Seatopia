package seatopia.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seatopia.service.AuthService;

public class RegisterRestaurantView {

    private final BorderPane root = new BorderPane();
    private final AuthService authService = new AuthService();

    public RegisterRestaurantView(Stage stage) {
        root.setPadding(new Insets(20));

        Label title = new Label("Register Restaurant");

        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();
        TextField nameField = new TextField();
        TextField addressField = new TextField();
        TextField cuisineField = new TextField();
        TextField hoursField = new TextField();

        Label message = new Label();

        Button registerBtn = new Button("Creează restaurant");
        Button backBtn = new Button("Înapoi");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        form.addRow(0, new Label("Email:"), emailField);
        form.addRow(1, new Label("Parolă:"), passwordField);
        form.addRow(2, new Label("Nume restaurant:"), nameField);
        form.addRow(3, new Label("Adresă:"), addressField);
        form.addRow(4, new Label("Tip bucătărie:"), cuisineField);
        form.addRow(5, new Label("Program:"), hoursField);

        HBox buttons = new HBox(10, registerBtn, backBtn);
        VBox box = new VBox(12, title, form, buttons, message);
        box.setPadding(new Insets(10));

        root.setCenter(box);

        registerBtn.setOnAction(e -> {
            try {
                var session = authService.registerRestaurant(
                        emailField.getText().trim(),
                        passwordField.getText(),
                        nameField.getText().trim(),
                        addressField.getText().trim(),
                        cuisineField.getText().trim(),
                        hoursField.getText().trim()
                );

                stage.getScene().setRoot(new RestaurantView(stage, session).getRoot());
            } catch (Exception ex) {
                message.setText("Eroare: " + ex.getMessage());
            }
        });

        backBtn.setOnAction(e ->
                stage.getScene().setRoot(new LoginView(stage).getRoot())
        );
    }

    public Parent getRoot() {
        return root;
    }
}

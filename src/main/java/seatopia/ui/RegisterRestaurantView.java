package seatopia.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import seatopia.service.AuthService;

public class RegisterRestaurantView {

    private final BorderPane root = new BorderPane();
    private final AuthService authService = new AuthService();

    public RegisterRestaurantView(Stage stage) {
        root.setPadding(new Insets(24));

        Label title = new Label("Creează cont Restaurant");
        title.getStyleClass().add("title");

        TextField emailField = new TextField();
        emailField.setPromptText("ex: contact@restaurant.ro");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("minim 6 caractere");

        TextField nameField = new TextField();
        nameField.setPromptText("ex: Trattoria Roma");

        TextField addressField = new TextField();
        addressField.setPromptText("ex: Str. Victoriei 10, București");

        TextField cuisineField = new TextField();
        cuisineField.setPromptText("ex: Italian / Romanian / Sushi");

        Label cuisineHint = new Label("Tip bucătărie: 1–3 cuvinte (ex: Italian, Asian Fusion).");
        cuisineHint.getStyleClass().add("hint");

        TextField hoursField = new TextField();
        hoursField.setPromptText("HH:MM-HH:MM (ex: 10:00-22:00)");

        Label message = new Label(" ");
        message.getStyleClass().add("message");


        Button registerBtn = new Button("Creează restaurant");
        registerBtn.getStyleClass().addAll("button", "primary");

        Button backBtn = new Button("Înapoi");
        backBtn.getStyleClass().addAll("button", "secondary");

        HBox buttons = new HBox(10, registerBtn, backBtn);

        VBox card = new VBox(12,
                title, new Separator(),
                new Label("Email"), emailField,
                new Label("Parolă"), passwordField,
                new Separator(),
                new Label("Nume restaurant"), nameField,
                new Label("Adresă"), addressField,
                new Label("Tip bucătărie"), cuisineField, cuisineHint,
                new Label("Program"), hoursField,
                buttons,
                message
        );
        card.getStyleClass().add("card");
        card.setMaxWidth(520);

        root.setCenter(new StackPane(card));

        registerBtn.setOnAction(e -> {
            try {
                String email = emailField.getText().trim();
                String pass = passwordField.getText();
                String name = nameField.getText().trim();
                String address = addressField.getText().trim();
                String cuisine = cuisineField.getText().trim();
                String hours = hoursField.getText().trim();

                if (!isValidEmail(email)) {
                    showMessage(message, "Email invalid. Exemplu: nume@gmail.com");
                    return;
                }
                if (pass == null || pass.length() < 6) {
                    showMessage(message, "Parola trebuie să aibă minim 6 caractere.");
                    return;
                }
                if (name.isBlank()) {
                    showMessage(message, "Numele restaurantului este obligatoriu.");
                    return;
                }
                if (address.isBlank()) {
                    showMessage(message, "Adresa este obligatorie.");
                    return;
                }

                if (!hours.matches("^([01]\\d|2[0-3]):[0-5]\\d-([01]\\d|2[0-3]):[0-5]\\d$")) {
                    showMessage(message, "Program invalid. Folosește HH:MM-HH:MM (ex: 10:00-22:00).");
                    return;
                }

                authService.registerRestaurant(email, pass, name, address, cuisine, hours);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succes");
                alert.setHeaderText("Cont restaurant creat cu succes!");
                alert.setContentText("Te poți autentifica acum.");
                alert.showAndWait();

                stage.getScene().setRoot(new LoginView(stage).getRoot());

            } catch (Exception ex) {
                showMessage(message, "Eroare: " + ex.getMessage());
            }
        });


        backBtn.setOnAction(e ->
                stage.getScene().setRoot(new LoginView(stage).getRoot())
        );

        hoursField.setOnAction(e -> registerBtn.fire());
        passwordField.setOnAction(e -> registerBtn.fire());
    }
    private boolean isValidEmail(String email) {
        if (email == null) return false;
        String e = email.trim();
        if (e.isEmpty()) return false;
        return e.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");
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

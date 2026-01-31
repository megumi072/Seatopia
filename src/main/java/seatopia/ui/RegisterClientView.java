package seatopia.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import seatopia.service.AuthService;

public class RegisterClientView {

    private final BorderPane root = new BorderPane();
    private final AuthService authService = new AuthService();

    public RegisterClientView(Stage stage) {
        root.setPadding(new Insets(24));

        Label title = new Label("Creează cont Client");
        title.getStyleClass().add("title");

        TextField emailField = new TextField();
        emailField.setPromptText("ex: nume@email.com");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("minim 6 caractere");

        TextField nameField = new TextField();
        nameField.setPromptText("ex: Mădălina Popescu");

        TextField phoneField = new TextField();
        phoneField.setPromptText("07XXXXXXXX");

        Label message = new Label();
        message.getStyleClass().add("message");
        message.setVisible(false);
        message.setManaged(false);


        Button registerBtn = new Button("Creează cont");
        registerBtn.getStyleClass().addAll("button", "primary");

        Button backBtn = new Button("Înapoi");
        backBtn.getStyleClass().addAll("button", "secondary");

        HBox buttons = new HBox(10, registerBtn, backBtn);

        VBox card = new VBox(12,
                title, new Separator(),
                new Label("Email"), emailField,
                new Label("Parolă"), passwordField,
                new Separator(),
                new Label("Nume"), nameField,
                new Label("Telefon"), phoneField,
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
                String phone = phoneField.getText().trim();

                if (!isValidEmail(email)) {
                    message.setText("Email invalid. Exemplu: nume@gmail.com");
                    return;
                }
                if (pass == null || pass.length() < 6) throw new Exception("Parola trebuie să aibă minim 6 caractere.");
                if (name.isBlank()) throw new Exception("Numele este obligatoriu.");
                if (!phone.matches("^07\\d{8}$")) throw new Exception("Telefon invalid. Exemplu: 07XXXXXXXX");

                var session = authService.registerClient(email, pass, name, phone);
                stage.getScene().setRoot(new ClientView(stage, session).getRoot());
            } catch (Exception ex) {
                message.setText("Eroare: " + ex.getMessage());
            }
        });

        backBtn.setOnAction(e ->
                stage.getScene().setRoot(new LoginView(stage).getRoot())
        );

        phoneField.setOnAction(e -> registerBtn.fire());
        passwordField.setOnAction(e -> registerBtn.fire());
    }
    private boolean isValidEmail(String email) {
        if (email == null) return false;
        String e = email.trim();
        if (e.isEmpty()) return false;
        return e.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");
    }


    public Parent getRoot() {
        return root;
    }
}

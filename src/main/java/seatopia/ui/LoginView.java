package seatopia.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import seatopia.model.UserRole;
import seatopia.service.AuthService;

public class LoginView {

    private final BorderPane root = new BorderPane();
    private final AuthService authService = new AuthService();

    public LoginView(Stage stage) {
        root.setPadding(new Insets(24));

        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setMaxWidth(520);

        Label title = new Label("Seatopia");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Autentifică-te sau creează un cont nou.");
        subtitle.getStyleClass().add("subtitle");

        TextField emailField = new TextField();
        emailField.setPromptText("Email (ex: nume@email.com)");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Parolă");

        Label message = new Label();
        message.getStyleClass().add("message");
        message.setVisible(false);
        message.setManaged(false);


        Button loginBtn = new Button("Login");
        loginBtn.getStyleClass().addAll("button", "primary");

        Button registerClientBtn = new Button("Client");
        registerClientBtn.getStyleClass().addAll("button", "secondary");

        Button registerRestaurantBtn = new Button("Restaurant");
        registerRestaurantBtn.getStyleClass().addAll("button", "secondary");

        HBox registerRow = new HBox(10,
                new Label("Creează cont:"),
                registerClientBtn,
                registerRestaurantBtn
        );
        registerRow.setPadding(new Insets(6, 0, 0, 0));

        HBox buttons = new HBox(10, loginBtn);
        buttons.setPadding(new Insets(6, 0, 0, 0));

        card.getChildren().addAll(
                title, subtitle, new Separator(),
                new Label("Email"), emailField,
                new Label("Parolă"), passwordField,
                buttons,
                registerRow,
                message
        );

        StackPane center = new StackPane(card);
        root.setCenter(center);

        loginBtn.setOnAction(e -> {
            try {
                String email = emailField.getText().trim();
                String pass = passwordField.getText();

                if (email.isBlank()) throw new Exception("Email obligatoriu.");
                if (pass == null || pass.isBlank()) throw new Exception("Parola obligatorie.");

                var session = authService.login(email, pass);

                if (session.getUser().getRole() == UserRole.CLIENT) {
                    stage.getScene().setRoot(new ClientView(stage, session).getRoot());
                } else {
                    stage.getScene().setRoot(new RestaurantView(stage, session).getRoot());
                }
            } catch (Exception ex) {
                message.setText("Eroare: " + ex.getMessage());
            }
        });

        registerClientBtn.setOnAction(e ->
                stage.getScene().setRoot(new RegisterClientView(stage).getRoot())
        );

        registerRestaurantBtn.setOnAction(e ->
                stage.getScene().setRoot(new RegisterRestaurantView(stage).getRoot())
        );

        passwordField.setOnAction(e -> loginBtn.fire());
        emailField.setOnAction(e -> loginBtn.fire());
    }

    public Parent getRoot() {
        return root;
    }
}

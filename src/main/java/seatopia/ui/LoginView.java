package seatopia.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seatopia.model.UserRole;
import seatopia.service.AuthService;

public class LoginView {

    private final BorderPane root = new BorderPane();
    private final AuthService authService = new AuthService();

    public LoginView(Stage stage) {
        root.setPadding(new Insets(20));

        Label title = new Label("Seatopia - Login");

        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();

        Label message = new Label();

        Button loginBtn = new Button("Login");
        Button registerClientBtn = new Button("Register Client");
        Button registerRestaurantBtn = new Button("Register Restaurant");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        form.addRow(0, new Label("Email:"), emailField);
        form.addRow(1, new Label("Parola:"), passwordField);

        HBox buttons = new HBox(10, loginBtn, registerClientBtn, registerRestaurantBtn);

        VBox box = new VBox(12, title, form, buttons, message);
        box.setPadding(new Insets(10));

        root.setCenter(box);

        loginBtn.setOnAction(e -> {
            try {
                var session = authService.login(emailField.getText().trim(), passwordField.getText());

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
    }

    public Parent getRoot() {
        return root;
    }
}

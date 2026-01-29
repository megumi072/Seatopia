package seatopia;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import seatopia.db.Database;
import seatopia.ui.LoginView;

public class SeatopiaApp extends Application {

    @Override
    public void start(Stage stage) {
        Database.init();

        var loginView = new LoginView(stage);
        stage.setTitle("Seatopia");
        stage.setScene(new Scene(loginView.getRoot(), 900, 600));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

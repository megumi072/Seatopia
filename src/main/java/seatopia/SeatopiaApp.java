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

        Scene scene = new Scene(loginView.getRoot(), 900, 600);

        var css = getClass().getResource("/app.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        } else {
            System.out.println("WARNING: app.css not found");
        }

        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

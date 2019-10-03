package org.github.otymko.phoenixbsl.views;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class SettingsFormApplication extends Application {
  @Override
  public void start(Stage primaryStage) throws Exception {

    primaryStage.setTitle("Настройки");
    GridPane gridPane = createFormPane();
    Scene scene = new Scene(gridPane, 800, 500);
    primaryStage.setScene(scene);
    primaryStage.show();

  }

  private GridPane createFormPane() {
    GridPane gridPane = new GridPane();
    return gridPane;
  }

  private void addUIControls(GridPane gridPane) {
  }

}

package org.github.otymko.phoenixbsl.views;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class StageBarController {

  private Stage owner;
  private Parent rootElement;

  private double xOffset = 0;
  private double yOffset = 0;

  @FXML
  private Button btnClose;

  public StageBarController() {}

  public void setOwner(Stage owner) {
    this.owner = owner;
  }

  public void setRootElement(Parent root) {
    this.rootElement = root;
    root.setOnMouseDragged(event -> handleMouseDragged(event));
    root.setOnMousePressed(event -> handleMousePressed(event));
  }

  @FXML
  private void handleCloseAction(ActionEvent event) {
    owner.close();
  }

  @FXML
  private void handleRestoreAction(ActionEvent event) {
    owner.setMaximized(!owner.isMaximized());
  }

  @FXML
  private void handleMinimizeAction(ActionEvent event) {
    owner.setIconified(true);
  }

  @FXML
  private void handleMousePressed(MouseEvent event) {
    xOffset = event.getSceneX();
    yOffset = event.getSceneY();
  }

  @FXML
  private void handleMouseDragged(MouseEvent event) {
    owner.setX(event.getScreenX() - xOffset);
    owner.setY(event.getScreenY() - yOffset);
  }

  @FXML
  private void handleMouseClicked(MouseEvent event) {
    if (event.getClickCount() == 2) {
      owner.setMaximized(!owner.isMaximized());
    }
  }

}

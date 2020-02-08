package org.github.otymko.phoenixbsl.views;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.github.otymko.phoenixbsl.core.ConfigurationApp;

@Getter
@Slf4j
public class SettingStageController {

  private ConfigurationApp configuration;

  @FXML
  private JFXCheckBox usePathToJarBSLLS;

  @FXML
  private TextField pathToJava;

  @FXML
  private TextField pathToBSLLS;

  @FXML
  private Label labelVersion;

  @FXML
  private Hyperlink linkPathToLogs;

  @FXML
  private JFXButton btnSaveSetting;

  public void setConfiguration(ConfigurationApp configuration) {
    this.configuration = configuration;
  }

  @FXML
  private void handleSelectUsePathToJarBSLLSAction(ActionEvent event) {
    configuration.setUsePathToJarBSLLS(usePathToJarBSLLS.isSelected());
  }

  @FXML
  private void handlePathToJavaAction(ActionEvent event) {
    configuration.setPathToJava(pathToJava.getText());
  }

  @FXML
  private void handlePathToBSLLSAction(ActionEvent event) {
    configuration.setPathToBSLLS(pathToBSLLS.getText());
  }

}

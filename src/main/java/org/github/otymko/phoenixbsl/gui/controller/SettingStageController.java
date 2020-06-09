package org.github.otymko.phoenixbsl.gui.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.github.otymko.phoenixbsl.model.Configuration;

@Getter
@Slf4j
public class SettingStageController {

  private Configuration configuration;

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

  @FXML
  private JFXCheckBox useCustomBSLLSConfiguration;

  @FXML
  private TextField pathToBSLLSConfiguration;

  @FXML
  private JFXCheckBox useGroupIssuesBySeverity;

  public void setConfiguration(Configuration configuration) {
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

  @FXML
  private void handleSelectUseCustomBSLLSConfiguration(ActionEvent event) {
    configuration.setUseCustomBSLLSConfiguration(useCustomBSLLSConfiguration.isSelected());
  }

  @FXML
  private void handlePathToBSLLSConfiguration(ActionEvent event) {
    configuration.setPathToBSLLSConfiguration(pathToBSLLSConfiguration.getText());
  }
}

package org.github.otymko.phoenixbsl.gui;

import com.jfoenix.assets.JFoenixResources;
import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.svg.SVGGlyph;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.lsp4j.Diagnostic;
import org.github.otymko.phoenixbsl.gui.stage.IssuesStage;
import org.github.otymko.phoenixbsl.model.Configuration;
import org.github.otymko.phoenixbsl.PhoenixCore;
import org.github.otymko.phoenixbsl.logic.event.EventListener;
import org.github.otymko.phoenixbsl.logic.event.EventManager;
import org.github.otymko.phoenixbsl.gui.controller.SettingStageController;

import java.awt.*;
import java.io.IOException;
import java.util.List;

@Slf4j
public class MainGUI implements EventListener {

  private IssuesStage issuesStage;
  private Stage settingStage;

  private SettingStageController controllerStages;

  public MainGUI(Stage mainStage) {
    Platform.setImplicitExit(false);
    initIssuesStage(mainStage);
    initializeEvents();
  }

  @Override
  public void showIssuesStage() {
    showIssuesStageImpl();
  }

  @Override
  public void updateIssues(List<Diagnostic> diagnostics) {
    issuesStage.lineOffset = PhoenixCore.getInstance().currentOffset;
    showIssuesStageImpl();
    Platform.runLater(() -> issuesStage.updateIssues(diagnostics));
  }

  @Override
  public void showSettingStage() {
    Platform.runLater(() -> {
      try {
        startSettingStage();
      } catch (IOException e) {
        LOGGER.error("Не удалось запустить SettingStage", e);
      }
    });
  }

  public void initIssuesStage(Stage owner) {
    issuesStage = new IssuesStage(owner);
    issuesStage.setIconified(true);
    issuesStage.show();
  }

  private void initializeEvents() {
    EventManager eventManager = PhoenixCore.getInstance().getEventManager();
    eventManager.subscribe(EventManager.EVENT_UPDATE_ISSUES, this);
    eventManager.subscribe(EventManager.SHOW_ISSUE_STAGE, this);
    eventManager.subscribe(EventManager.SHOW_SETTING_STAGE, this);
  }

  private void showIssuesStageImpl() {
    Platform.runLater(() -> {
        if (!issuesStage.isShowing()) {
          initIssuesStage(new Stage());
        }
        issuesStage.setIconified(false);
        issuesStage.requestFocus();
        issuesStage.toFront();
      }
    );
  }

  private void startSettingStage() throws IOException {

    if (settingStage != null && settingStage.isShowing()) {
      Platform.runLater(() -> {
        settingStage.setIconified(false);
        settingStage.toFront();
        settingStage.show();
      });
      return;
    }

    settingStage = new Stage();
    settingStage.setResizable(false);

    FXMLLoader loader = new FXMLLoader(PhoenixCore.class.getResource("/SettingStage.fxml"));
    Parent root = loader.load();

    controllerStages = loader.getController();
    controllerStages.setConfiguration(PhoenixCore.getInstance().getConfiguration());

    JFXDecorator decorator = new JFXDecorator(settingStage, root, false, false, false);
    decorator.setCustomMaximize(false);
    decorator.setGraphic(new SVGGlyph(""));

    var scene = new Scene(decorator, 600, 534);
    final ObservableList<String> stylesheets = scene.getStylesheets();
    stylesheets.addAll(JFoenixResources.load("/theme.css").toExternalForm());
    settingStage.setScene(scene);

    var pathToLog = PhoenixCore.getInstance().getPathToLogs();

    var link = controllerStages.getLinkPathToLogs();
    link.setText(pathToLog.toString());

    link.setOnAction(event -> {
      Desktop desktop = null;
      if (Desktop.isDesktopSupported()) {
        desktop = Desktop.getDesktop();
        try {
          desktop.open(pathToLog.toFile());
        } catch (IOException e) {
          LOGGER.error("Не удалось открыть каталог с логами", e);
        }
      }
    });

    fillSettingValueFromConfiguration(PhoenixCore.getInstance().getConfiguration());

    var btnSaveSetting = controllerStages.getBtnSaveSetting();
    btnSaveSetting.setOnAction(event -> {
      // сохраним configuration в файл
      processSaveSettings();
      settingStage.close();
      PhoenixCore.getInstance().restartProcessBSLLS();
    });

    controllerStages.getLabelVersion().setText(PhoenixCore.getInstance().getVersionBSLLS());

    settingStage.show();

  }

  private void processSaveSettings() {

    var configuration = PhoenixCore.getInstance().getConfiguration();

    var usePathToJarBSLLS = controllerStages.getUsePathToJarBSLLS();
    configuration.setUsePathToJarBSLLS(usePathToJarBSLLS.isSelected());

    var pathToJava = controllerStages.getPathToJava();
    configuration.setPathToJava(pathToJava.getText());

    var pathToBSLLS = controllerStages.getPathToBSLLS();
    configuration.setPathToBSLLS(pathToBSLLS.getText());

    var useCustomBSLLSConfiguration = controllerStages.getUseCustomBSLLSConfiguration();
    configuration.setUseCustomBSLLSConfiguration(useCustomBSLLSConfiguration.isSelected());

    var pathToBSLLSConfiguration = controllerStages.getPathToBSLLSConfiguration();
    configuration.setPathToBSLLSConfiguration(pathToBSLLSConfiguration.getText());

    var useGroupIssuesBySeverity = controllerStages.getUseGroupIssuesBySeverity();
    configuration.setUseGroupIssuesBySeverity(useGroupIssuesBySeverity.isSelected());

    PhoenixCore.getInstance().writeConfiguration(configuration);
  }

  private void fillSettingValueFromConfiguration(Configuration configuration) {

    var usePathToJarBSLLS = controllerStages.getUsePathToJarBSLLS();
    usePathToJarBSLLS.setSelected(configuration.isUsePathToJarBSLLS());

    var pathToJava = controllerStages.getPathToJava();
    pathToJava.setText(configuration.getPathToJava());

    var pathToBSLLS = controllerStages.getPathToBSLLS();
    pathToBSLLS.setText(configuration.getPathToBSLLS());

    var useCustomBSLLSConfiguration = controllerStages.getUseCustomBSLLSConfiguration();
    useCustomBSLLSConfiguration.setSelected(configuration.isUseCustomBSLLSConfiguration());

    var pathToBSLLSConfiguration = controllerStages.getPathToBSLLSConfiguration();
    pathToBSLLSConfiguration.setText(configuration.getPathToBSLLSConfiguration());

    var useGroupIssuesBySeverity = controllerStages.getUseGroupIssuesBySeverity();
    useGroupIssuesBySeverity.setSelected(configuration.isUseGroupIssuesBySeverity());

  }

}

package org.github.otymko.phoenixbsl.views;

import com.jfoenix.assets.JFoenixResources;
import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.svg.SVGGlyph;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.lsp4j.Diagnostic;
import org.github.otymko.phoenixbsl.core.ConfigurationApp;
import org.github.otymko.phoenixbsl.core.PhoenixApp;
import org.github.otymko.phoenixbsl.events.EventListener;
import org.github.otymko.phoenixbsl.events.EventManager;

import java.awt.*;
import java.io.IOException;
import java.util.List;

@Slf4j
public class MainApplication extends Application implements EventListener {

  private IssuesStage issuesStage;
  private Stage settingStage;

  private SettingStageController controllerStages;

  public MainApplication() {

    Platform.setImplicitExit(false);

    EventManager eventManager = PhoenixApp.getInstance().getEventManager();
    eventManager.subscribe(EventManager.EVENT_UPDATE_ISSUES, this);
    eventManager.subscribe(EventManager.SHOW_ISSUE_STAGE, this);
    eventManager.subscribe(EventManager.SHOW_SETTING_STAGE, this);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    initIssuesStage(primaryStage);
  }

  @Override
  public void showIssuesStage() {
    showIssuesStageImpl();
  }

  public void initIssuesStage(Stage owner) {
    issuesStage = new IssuesStage(owner);
    issuesStage.setIconified(true);
    issuesStage.show();
  }

  @Override
  public void updateIssues(List<Diagnostic> diagnostics) {
    issuesStage.lineOffset = PhoenixApp.getInstance().currentOffset;
    showIssuesStageImpl();
    Platform.runLater(() -> issuesStage.updateIssues(diagnostics));
  }

  public static void main(String[] args) {
    launch(args);
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

    FXMLLoader loader = new FXMLLoader(PhoenixApp.class.getResource("/SettingStage.fxml"));
    Parent root = loader.load();

    controllerStages = loader.getController();
    controllerStages.setConfiguration(PhoenixApp.getInstance().getConfiguration());

    JFXDecorator decorator = new JFXDecorator(settingStage, root, false, false, false);
    decorator.setCustomMaximize(false);
    decorator.setGraphic(new SVGGlyph(""));

    var scene = new Scene(decorator, 600, 534);
    final ObservableList<String> stylesheets = scene.getStylesheets();
    stylesheets.addAll(JFoenixResources.load("/theme.css").toExternalForm());
    settingStage.setScene(scene);

    var pathToLog = PhoenixApp.getInstance().getPathToLogs();

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

    fillSettingValueFromConfiguration(PhoenixApp.getInstance().getConfiguration());

    var btnSaveSetting = controllerStages.getBtnSaveSetting();
    btnSaveSetting.setOnAction(event -> {
      // сохраним configuration в файл
      processSaveSettings();
      settingStage.close();
      PhoenixApp.getInstance().restartProcessBSLLS();
    });

    controllerStages.getLabelVersion().setText(PhoenixApp.getInstance().getVersionBSLLS());

    settingStage.show();

  }

  private void processSaveSettings() {

    var configuration = PhoenixApp.getInstance().getConfiguration();

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

    PhoenixApp.getInstance().writeConfiguration(configuration);
  }

  private void fillSettingValueFromConfiguration(ConfigurationApp configuration) {

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

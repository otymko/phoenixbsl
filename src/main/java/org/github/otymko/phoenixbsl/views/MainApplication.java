package org.github.otymko.phoenixbsl.views;

import javafx.application.Application;
import javafx.application.Platform;
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
import org.github.otymko.phoenixbsl.utils.Common;

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

    FXMLLoader loader = new FXMLLoader(PhoenixApp.class.getResource("/SettingStage.fxml"));
    var controller = new StageBarController();

    settingStage = new Stage();
    Common.setControllerFactory(loader, controller);
    Parent root = loader.load();
    controller.setOwner(settingStage);
    controller.setRootElement(root);

    controllerStages = loader.getController();
    controllerStages.setConfiguration(PhoenixApp.getInstance().getConfiguration());

    var scene = new Scene(root);
    settingStage.setScene(scene);

    scene.setFill(Color.TRANSPARENT);
    settingStage.initStyle(StageStyle.TRANSPARENT);

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
      PhoenixApp.getInstance().writeConfiguration(PhoenixApp.getInstance().getConfiguration());
      settingStage.close();
    });

    controllerStages.getLabelVersion().setText(PhoenixApp.getInstance().getVersionBSLLS());

    settingStage.show();

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

  }


}

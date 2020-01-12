package org.github.otymko.phoenixbsl.views;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.eclipse.lsp4j.Diagnostic;
import org.github.otymko.phoenixbsl.core.PhoenixApp;
import org.github.otymko.phoenixbsl.events.EventListener;
import org.github.otymko.phoenixbsl.events.EventManager;

import java.util.List;

public class MainApplication extends Application implements EventListener {

  private IssuesStage issuesStage;

  public MainApplication() {
    PhoenixApp.getInstance().getEventManager().subscribe(EventManager.EVENT_UPDATE_ISSUES, this);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    initIssuesStage(primaryStage);
  }

  public void initIssuesStage(Stage owner) {
    issuesStage = new IssuesStage(owner);
    issuesStage.setIconified(true);
    issuesStage.show();
  }

  @Override
  public void updateIssues(List<Diagnostic> diagnostics) {
    Platform.runLater(() -> issuesStage.updateIssues(diagnostics));
  }

  public static void main(String[] args) {
    launch(args);
  }

}

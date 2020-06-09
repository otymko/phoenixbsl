package org.github.otymko.phoenixbsl;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.github.otymko.phoenixbsl.core.PhoenixApp;
import org.github.otymko.phoenixbsl.events.EventListener;
import org.github.otymko.phoenixbsl.threads.GlobalKeyListenerThread;

@Slf4j
public class PhoenixMain extends Application implements EventListener {


  public static void main(String[] args) {
    if (PhoenixApp.getInstance().appIsRunning()) {
      LOGGER.info("Приложение уже запущено. Одновременный запуск невозможен.");
      PhoenixApp.getInstance().abort();
      Platform.exit();
      return;
    }
    launch(args);
  }

  @Override
  public void start(Stage mainStage) {
    startComponents();
    new MainGUI(mainStage);
  }

  private void startComponents() {

    // ядро приложения
    var app = PhoenixApp.getInstance();

    // инициализация настроек
    app.initConfiguration();

    // TODO: запускаем главную форму

    // инициализируем трей
    app.initToolbar();

    // подключаем слушаеть нажатий
    GlobalKeyListenerThread globalKeyListenerThread = new GlobalKeyListenerThread();
    globalKeyListenerThread.start();

    // запуск bsl ls
    app.initProcessBSL();

  }

}

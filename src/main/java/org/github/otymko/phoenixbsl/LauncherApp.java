package org.github.otymko.phoenixbsl;

import lombok.extern.slf4j.Slf4j;
import org.github.otymko.phoenixbsl.core.PhoenixApp;
import org.github.otymko.phoenixbsl.threads.GlobalKeyListenerThread;
import org.github.otymko.phoenixbsl.threads.MainApplicationThread;

@Slf4j
public class LauncherApp {

  public static void main(String[] args) {

    if (PhoenixApp.getInstance().appIsRunning()) {
      LOGGER.info("Приложение уже запущено. Одновременный запуск невозможен.");
      PhoenixApp.getInstance().abort();
    }

    try {
      runApp();
    } catch (RuntimeException | InterruptedException ex) {
      LOGGER.error("Приложение упало", ex);
    }

  }


  private static void runApp() throws InterruptedException {

    var app = PhoenixApp.getInstance();

    // инициализация настроек
    app.initConfiguration();

    // запускаем главную форму
    MainApplicationThread mainApplicationThread = new MainApplicationThread();
    mainApplicationThread.start();

    // инициализируем трей
    app.initToolbar();

    // подключаем слушаеть нажатий
    GlobalKeyListenerThread globalKeyListenerThread = new GlobalKeyListenerThread();
    globalKeyListenerThread.start();

    // запуск bsl ls
    app.initProcessBSL();

  }
}

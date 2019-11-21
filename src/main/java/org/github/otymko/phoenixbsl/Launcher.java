package org.github.otymko.phoenixbsl;

import org.github.otymko.phoenixbsl.core.PhoenixApp;
import org.github.otymko.phoenixbsl.threads.GlobalKeyListenerThread;
import org.github.otymko.phoenixbsl.threads.MainApplicationThread;

public class Launcher {

  public static void main(String[] args) {

    if (PhoenixApp.getInstance().appIsRunning()) {
      PhoenixApp.getInstance().abort();
    }

    try {

      // инициализируем трей
      PhoenixApp.initToolbar();

      // запускаем главную форму
      MainApplicationThread mainApplicationThread = new MainApplicationThread();
      mainApplicationThread.start();

      // подключаем слушаеть нажатий
      GlobalKeyListenerThread globalKeyListenerThread = new GlobalKeyListenerThread();
      globalKeyListenerThread.start();

    } catch (RuntimeException ex) {
      //log.error("Приложение упало. Причина " + ex.getMessage());
    }

  }

}

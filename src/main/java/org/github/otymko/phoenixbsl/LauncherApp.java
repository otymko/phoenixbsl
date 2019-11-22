package org.github.otymko.phoenixbsl;

import org.github.otymko.phoenixbsl.core.PhoenixApp;
import org.github.otymko.phoenixbsl.threads.GlobalKeyListenerThread;
import org.github.otymko.phoenixbsl.threads.MainApplicationThread;

public class LauncherApp {

  public static void main(String[] args) {

    if (PhoenixApp.getInstance().appIsRunning()) {
      PhoenixApp.getInstance().abort();
    }

    try {

      PhoenixApp app = PhoenixApp.getInstance();

      // инициализируем трей
      app.initToolbar();

      // запускаем главную форму
      MainApplicationThread mainApplicationThread = new MainApplicationThread();
      mainApplicationThread.start();

      // подключаем слушаеть нажатий
      GlobalKeyListenerThread globalKeyListenerThread = new GlobalKeyListenerThread();
      globalKeyListenerThread.start();

      // запуск bsl ls
      Process processBSL = app.startProcessBSLLS();
      if (processBSL == null) {
        System.out.println("BSL не запустился");
        return;
      }
      BSLLanguageClient client = new BSLLanguageClient();
      BSLLanguageLauncher bslLanguageLauncher = new BSLLanguageLauncher(client, processBSL.getInputStream(), processBSL.getOutputStream());
      bslLanguageLauncher.startInThread();

      try {
        Thread.currentThread().sleep(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      app.bslLanguageLauncher = bslLanguageLauncher;

      // инициализация
      bslLanguageLauncher.sendInitialize(bslLanguageLauncher.createInitializeParams());

      // откроем фейковый документ
      app.textDocumentDidOpen();

    } catch (RuntimeException ex) {
      System.out.println("Приложение упало. Причина " + ex.getMessage().toString());
    }

  }

}

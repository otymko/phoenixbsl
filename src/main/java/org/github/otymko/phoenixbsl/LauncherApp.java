package org.github.otymko.phoenixbsl;

import org.github.otymko.phoenixbsl.core.PhoenixApp;
import org.github.otymko.phoenixbsl.lsp.BSLHelper;
import org.github.otymko.phoenixbsl.lsp.BSLLanguageClient;
import org.github.otymko.phoenixbsl.lsp.BSLLanguageServer;
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
      BSLLanguageServer bslLanguageServer = new BSLLanguageServer(client, processBSL.getInputStream(), processBSL.getOutputStream());
      bslLanguageServer.startInThread();

      try {
        Thread.currentThread().sleep(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      app.bslLanguageServer = bslLanguageServer;

      // инициализация
      bslLanguageServer.initialize(BSLHelper.createInitializeParams());

      // откроем фейковый документ
      BSLHelper.textDocumentDidOpen(bslLanguageServer);

    } catch (RuntimeException ex) {
      System.out.println("Приложение упало. Причина " + ex.getMessage().toString());
    }

  }

}

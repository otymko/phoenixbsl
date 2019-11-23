package org.github.otymko.phoenixbsl;

import org.github.otymko.phoenixbsl.core.PhoenixApp;
import org.github.otymko.phoenixbsl.lsp.BSLBinding;
import org.github.otymko.phoenixbsl.lsp.BSLLanguageClient;
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
      app.initProcessBSL();

      if (app.processBSLIsRunning()) {
        BSLLanguageClient bslClient = new BSLLanguageClient();
        BSLBinding bslBinding = new BSLBinding(
          bslClient,
          app.getProcessBSL().getInputStream(),
          app.getProcessBSL().getOutputStream());
        bslBinding.startInThread();

        try {
          Thread.currentThread().sleep(2000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        app.setBslBinding(bslBinding);

        // инициализация
        bslBinding.initialize();

        // откроем фейковый документ
        bslBinding.textDocumentDidOpen(app.getFakeUri(), "");

      }


    } catch (RuntimeException ex) {
      System.out.println("Приложение упало. Причина " + ex.getMessage().toString());
    }

  }

}

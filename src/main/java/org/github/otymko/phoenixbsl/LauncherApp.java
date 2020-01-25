package org.github.otymko.phoenixbsl;

import lombok.extern.slf4j.Slf4j;
import org.github.otymko.phoenixbsl.core.PhoenixApp;
import org.github.otymko.phoenixbsl.lsp.BSLBinding;
import org.github.otymko.phoenixbsl.lsp.BSLLanguageClient;
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
      LOGGER.error("Приложение упало. Причина {}", ex.getMessage());
    }

  }


  private static void runApp() throws InterruptedException {

    var app = PhoenixApp.getInstance();

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

      Thread.currentThread().sleep(2000);

      app.setBslBinding(bslBinding);

      // инициализация
      bslBinding.initialize();

      // откроем фейковый документ
      bslBinding.textDocumentDidOpen(app.getFakeUri(), "");


    }
  }
}

package org.github.otymko.phoenixbsl;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.github.otymko.phoenixbsl.core.PhoenixCore;
import org.github.otymko.phoenixbsl.events.EventListener;

@Slf4j
public class PhoenixMain extends Application implements EventListener {

  public static void main(String[] args) {
    if (PhoenixCore.getInstance().appIsRunning()) {
      LOGGER.info("Приложение уже запущено. Одновременный запуск невозможен.");
      PhoenixCore.getInstance().abort();
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
    var core = PhoenixCore.getInstance();
    core.initializeConfiguration(); // инициализируем настроек
    core.startToolbar(); // запустим трей
    core.initializeGlobalKeyListener(); // подключаем слушаеть нажатий
    core.initProcessBSL(); // запустим bsl ls
  }

}

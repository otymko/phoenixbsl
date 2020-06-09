package org.github.otymko.phoenixbsl;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.github.otymko.phoenixbsl.logic.event.EventListener;
import org.github.otymko.phoenixbsl.gui.MainGUI;

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
    var core = PhoenixCore.getInstance();
    core.initializeConfiguration(); // инициализируем настроек
    core.startToolbar(); // запустим трей
    core.initializeGlobalKeyListener(); // подключаем слушаеть нажатий
    core.initProcessBSL(); // запустим bsl ls
    new MainGUI(mainStage);
  }

}

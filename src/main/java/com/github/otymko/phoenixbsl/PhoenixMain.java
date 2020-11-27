package com.github.otymko.phoenixbsl;

import com.github.otymko.phoenixbsl.logic.event.EventListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import com.github.otymko.phoenixbsl.gui.MainGUI;

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
    Platform.setImplicitExit(false);

    var core = PhoenixCore.getInstance();
    core.initializeConfiguration(); // инициализируем настроек
    core.initToolbar(); // запустим трей
    core.initializeGlobalKeyListener(); // подключаем слушаеть нажатий
    core.initEmptyProject();
    core.initProcessBSL(); // запустим bsl ls

    new MainGUI(mainStage);
  }

}

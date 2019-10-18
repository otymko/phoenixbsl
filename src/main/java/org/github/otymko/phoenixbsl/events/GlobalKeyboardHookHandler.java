package org.github.otymko.phoenixbsl.events;

import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;
import org.github.otymko.phoenixbsl.App;
import org.github.otymko.phoenixbsl.core.PhoenixAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class GlobalKeyboardHookHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalKeyboardHookHandler.class);

  private static boolean run = true;
  private static App app;

  public GlobalKeyboardHookHandler() {

    this.app = App.getInstance();

    var keyboardHook = new GlobalKeyboardHook(true);
    log.info("Global keyboard hook successfully started. Connected keyboards:");

    for (Map.Entry<Long, String> keyboard : GlobalKeyboardHook.listKeyboards().entrySet()) {
      log.info(String.format("%d: %s\n", keyboard.getKey(), keyboard.getValue()));
    }

    keyboardHook.addKeyListener(new GlobalKeyAdapter() {

      @Override
      public void keyPressed(GlobalKeyEvent event) {
        if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_I && event.isControlPressed()) {
          log.info(event.toString());
          if (PhoenixAPI.isWindowsForm1S()) {
            if (event.isMenuPressed()) {
              notifyAboutAlt();
              return;
            }
            app.updateFocusForm();
            app.runCheckBSL();
          }
          else {
            log.info("Это не форма 1С");
          }
        }
        if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_K && event.isControlPressed()) {
          log.info(event.toString());
          if (PhoenixAPI.isWindowsForm1S()) {
            if (event.isMenuPressed()) {
              notifyAboutAlt();
              return;
            }
            app.updateFocusForm();
            app.runFormattingBSL();
          }
        }
      }

    });

    try {
      while(run) {
        Thread.sleep(200);
      }
    } catch(InterruptedException e) {
      //Do nothing
    } finally {
      keyboardHook.shutdownHook();
    }

  }

  private void notifyAboutAlt() {
    app.notifyUser(
        "Использование горячих клавиш",
        "Не используйте при нажатии горячих клавиш Alt");
  }
}

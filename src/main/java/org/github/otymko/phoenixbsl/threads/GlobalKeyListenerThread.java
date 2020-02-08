package org.github.otymko.phoenixbsl.threads;

import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;
import lombok.extern.slf4j.Slf4j;
import org.github.otymko.phoenixbsl.core.PhoenixApp;
import org.github.otymko.phoenixbsl.entities.KeyboardShortcut;
import org.github.otymko.phoenixbsl.events.EventManager;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GlobalKeyListenerThread extends Thread {

  private PhoenixApp app = PhoenixApp.getInstance();
  private Map<KeyboardShortcut, Runnable> commands = new HashMap<>();

  public GlobalKeyListenerThread() {

    commands.put(
      new KeyboardShortcut(GlobalKeyEvent.VK_I, true),
      () -> app.getEventManager().notify(EventManager.EVENT_INSPECTION));

    commands.put(
      new KeyboardShortcut(GlobalKeyEvent.VK_K, true),
      () -> app.getEventManager().notify(EventManager.EVENT_FORMATTING));

    commands.put(
      new KeyboardShortcut(GlobalKeyEvent.VK_J, true),
      () -> app.getEventManager().notify(EventManager.EVENT_FIX_ALL));

  }

  @Override
  public void run() {

    try {
      runHook();
    } catch (IllegalStateException e) {
      LOGGER.error("Ошибка", e);
      run();
    }

  }

  public void runHook() {

    GlobalKeyboardHook keyboardHook = new GlobalKeyboardHook(false);
    LOGGER.info("Global keyboard hook successfully started. Connected keyboards:");

    keyboardHook.addKeyListener(new GlobalKeyAdapter() {
      @Override
      public void keyPressed(GlobalKeyEvent event) {
        var isSupportAction = false;
        if (event.isControlPressed()) {

          var keyShortcut = new KeyboardShortcut(event.getVirtualKeyCode(), true);
          Runnable method = commands.entrySet().stream()
            .filter(pair -> pair.getKey().equals(keyShortcut))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);

          if (method != null) {
            isSupportAction = true;
            method.run();
          }

        }
        if (isSupportAction) {
          PhoenixApp.getInstance().sleepCurrentThread(50);
        }
      }
    });

  }

}

package org.github.otymko.phoenixbsl.logic;

import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;
import lombok.extern.slf4j.Slf4j;
import org.github.otymko.phoenixbsl.PhoenixCore;
import org.github.otymko.phoenixbsl.model.KeyboardShortcut;
import org.github.otymko.phoenixbsl.logic.event.EventManager;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GlobalKeyListenerThread extends Thread {

  private final EventManager eventManager;
  private Map<KeyboardShortcut, Runnable> commands = new HashMap<>();

  public GlobalKeyListenerThread() {

    eventManager = PhoenixCore.getInstance().getEventManager();

    commands.put(
      new KeyboardShortcut(GlobalKeyEvent.VK_I, true),
      () -> eventManager.notify(EventManager.EVENT_INSPECTION));

    commands.put(
      new KeyboardShortcut(GlobalKeyEvent.VK_K, true),
      () -> eventManager.notify(EventManager.EVENT_FORMATTING));

    commands.put(
      new KeyboardShortcut(GlobalKeyEvent.VK_J, true),
      () -> eventManager.notify(EventManager.EVENT_FIX_ALL));

  }

  @Override
  public void run() {

    try {
      runHook();
    } catch (IllegalStateException e) {
      LOGGER.error("Ошибка в слушателе нажатий клавиш. Будет осуществлен перезапуск", e);
      run();
    }

  }

  private void runHook() {

    GlobalKeyboardHook keyboardHook = new GlobalKeyboardHook(false);
    LOGGER.info("Глобальный слушатей нажатий клавиш запущен");

    keyboardHook.addKeyListener(new GlobalKeyAdapter() {
      @Override
      public void keyPressed(GlobalKeyEvent event) {
        var isSupportAction = false;
        if (event.isControlPressed()) {

          var keyShortcut = new KeyboardShortcut(event.getVirtualKeyCode(), true);
          var method = getMethodByKeyboardShortcut(keyShortcut);

          if (method != null) {
            isSupportAction = true;
            method.run();
          }

        }
        if (isSupportAction) {
          PhoenixCore.getInstance().sleepCurrentThread(50);
        }
      }
    });

  }

  private Runnable getMethodByKeyboardShortcut(KeyboardShortcut keyShortcut) {
    return commands.entrySet().stream()
      .filter(pair -> pair.getKey().equals(keyShortcut))
      .map(Map.Entry::getValue)
      .findFirst()
      .orElse(null);
  }

}

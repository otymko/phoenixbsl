package org.github.otymko.phoenixbsl.threads;

import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;
import org.github.otymko.phoenixbsl.core.PhoenixApp;
import org.github.otymko.phoenixbsl.events.EventManager;

import java.util.Map;

public class GlobalKeyListenerThread extends Thread {

  @Override
  public void run() {

    try {
      runHook();
    }
    catch (Exception e) {
      run();
    }

  }

  public void runHook() {

    GlobalKeyboardHook keyboardHook = new GlobalKeyboardHook(true);
    System.out.println("Global keyboard hook successfully started. Connected keyboards:");

    for (Map.Entry<Long, String> keyboard : GlobalKeyboardHook.listKeyboards().entrySet()) {
      System.out.format("%d: %s\n", keyboard.getKey(), keyboard.getValue());
    }

    keyboardHook.addKeyListener(new GlobalKeyAdapter() {

      @Override
      public void keyPressed(GlobalKeyEvent event) {
        if (event.isControlPressed()) {
          if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_I) {
            PhoenixApp.getInstance().getEventManager().notify(EventManager.EVENT_INSPECTION);
          }
          if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_K) {
            PhoenixApp.getInstance().getEventManager().notify(EventManager.EVENT_FORMATTING);
          }
        }
      }

      @Override
      public void keyReleased(GlobalKeyEvent event) {
      }
    });

  }

}

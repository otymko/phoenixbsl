package org.github.otymko.phoenixbsl;

import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

import java.util.Map;

public class GlobalKeyboardHookHandler {

  private static boolean run = true;
  private static MainApp app;

  public GlobalKeyboardHookHandler(MainApp app) {

    this.app = app;

    GlobalKeyboardHook keyboardHook = new GlobalKeyboardHook(true);
    System.out.println("Global keyboard hook successfully started. Connected keyboards:");

    for (Map.Entry<Long, String> keyboard : GlobalKeyboardHook.listKeyboards().entrySet()) {
      System.out.format("%d: %s\n", keyboard.getKey(), keyboard.getValue());
    }

    keyboardHook.addKeyListener(new GlobalKeyAdapter() {

      @Override
      public void keyPressed(GlobalKeyEvent event) {
        if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_I && event.isControlPressed()) {
          System.out.println(event);
          app.checkFocusForm();
          if (app.isFindForm()) {
            app.startCheckBSL();
          }
        }
        if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_K && event.isControlPressed()) {
          System.out.println(event);
          app.checkFocusForm();
          if (app.isFindForm()) {
            app.formatingTextByBSL();
          }
        }
      }

    });

    try {
      while(run) {
        Thread.sleep(128);
      }
    } catch(InterruptedException e) {
      //Do nothing
    } finally {
      keyboardHook.shutdownHook();
    }

  }

}

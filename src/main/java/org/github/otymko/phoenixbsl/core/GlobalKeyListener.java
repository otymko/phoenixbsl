package org.github.otymko.phoenixbsl.core;

import org.github.otymko.phoenixbsl.events.EventManager;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class GlobalKeyListener implements NativeKeyListener {

  private boolean ctrlPressed = false;

  @Override
  public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
  }

  @Override
  public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
    int keyCode = nativeKeyEvent.getKeyCode();

    if (keyCode == NativeKeyEvent.VC_CONTROL) {
      ctrlPressed = true;
    }

    if (ctrlPressed) {
      if (keyCode == NativeKeyEvent.VC_K) {
        PhoenixApp.getInstance().getEventManager().notify(EventManager.EVENT_FORMATTING);
      } else if (keyCode == NativeKeyEvent.VC_I) {
        PhoenixApp.getInstance().getEventManager().notify(EventManager.EVENT_INSPECTION);
      }
    }

  }

  @Override
  public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
    int keyCode = nativeKeyEvent.getKeyCode();
    if (keyCode == NativeKeyEvent.VC_CONTROL) {
      ctrlPressed = false;
    }
  }

}

package org.github.otymko.phoenixbsl.threads;

import org.github.otymko.phoenixbsl.core.GlobalKeyListener;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import java.util.logging.Level;
import java.util.logging.Logger;

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
    try {
      GlobalScreen.registerNativeHook();
    } catch (NativeHookException ex) {
      System.err.println("There was a problem registering the native hook.");
      System.err.println(ex.getMessage());
    }
    GlobalScreen.addNativeKeyListener(new GlobalKeyListener());
    Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
    logger.setLevel(Level.OFF);
  }

}

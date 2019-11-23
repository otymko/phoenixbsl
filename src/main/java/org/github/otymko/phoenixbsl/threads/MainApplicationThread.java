package org.github.otymko.phoenixbsl.threads;

import org.github.otymko.phoenixbsl.views.MainApplication;

public class MainApplicationThread extends Thread {

  @Override
  public void run() {

    String[] args = {};
    MainApplication.main(args);

  }

}

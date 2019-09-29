package org.github.otymko.phoenixbsl;

import lc.kra.system.mouse.GlobalMouseHook;
import lc.kra.system.mouse.event.GlobalMouseAdapter;
import lc.kra.system.mouse.event.GlobalMouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Callable;

public class GlobalMouseHandler implements Callable<String> {

  private static final Logger log = LoggerFactory.getLogger(GlobalMouseHandler.class);
  private static boolean runHandler = true;

  private GlobalMouseHook mouseHook;

  public GlobalMouseHandler() {

    mouseHook = new GlobalMouseHook();
    log.info("Global mouse hook successfully started, press [middle] mouse button to shutdown. Connected mice:");

    for (Map.Entry<Long,String> mouse:GlobalMouseHook.listMice().entrySet()) {
      log.info("%d: %s\n", mouse.getKey(), mouse.getValue());
    }

    mouseHook.addMouseListener(new GlobalMouseAdapter() {
      @Override
      public void mousePressed(GlobalMouseEvent event)  {
        System.out.println(event);
        if ((event.getButtons() & GlobalMouseEvent.BUTTON_LEFT) != GlobalMouseEvent.BUTTON_NO
            && (event.getButtons() & GlobalMouseEvent.BUTTON_RIGHT) != GlobalMouseEvent.BUTTON_NO) {
          log.info("Both mouse buttons are currently pressed!");
        }
        if (event.getButton()==GlobalMouseEvent.BUTTON_MIDDLE) {
          runHandler = false;
        }
      }

      @Override
      public void mouseReleased(GlobalMouseEvent event)  {
        System.out.println(event);
      }

      @Override
      public void mouseMoved(GlobalMouseEvent event) {
        System.out.println(event);
      }

      @Override
      public void mouseWheel(GlobalMouseEvent event) {
        System.out.println(event);
      }
    });

    log.info("Приложение запущено");

  }

  public void start() {
    try {
      while(runHandler) {
        Thread.sleep(128);
      }
    } catch(InterruptedException e) {
      //Do nothing
    } finally {
      mouseHook.shutdownHook();
    }
  }

  @Override
  public String call() throws Exception {
    start();
    return null;
  }
}

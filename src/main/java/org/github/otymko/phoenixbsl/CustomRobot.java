package org.github.otymko.phoenixbsl;

//import java.awt.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public class CustomRobot {

  private Robot robot;

  public CustomRobot() {
    try {
      robot = new Robot();
    } catch (AWTException e) {
      robot = null;
      System.out.println("Не удалось инициализировать Robot");
    }
  }

  public void pressKeyList(List<Integer> list) {
    for (int number : list) {
      robot.keyPress(number);
      robot.delay(70);
    }
  }

  public void pressKey(int key) {
    robot.keyPress(key);
  }

  public void Ctrl(int Key) {
    robot.keyPress(KeyEvent.VK_CONTROL);
    robot.keyPress(Key);
    robot.delay(100);
    robot.keyRelease(KeyEvent.VK_CONTROL);
  }

  public void Alt(int Key) {
    robot.keyPress(KeyEvent.VK_ALT);
    robot.keyPress(Key);
    robot.delay(30);
    robot.keyRelease(KeyEvent.VK_ALT);
  }

}
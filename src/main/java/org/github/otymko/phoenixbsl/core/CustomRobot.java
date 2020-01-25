package org.github.otymko.phoenixbsl.core;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CustomRobot {

  private Robot robot;

  public CustomRobot() {
    try {
      robot = new Robot();
    } catch (AWTException e) {
      robot = null;
      LOGGER.error("Не удалось инициализировать Robot");
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

  public static List<Integer> getListKeyEventByNumber(int inValue) {
    List<Integer> list = new ArrayList<>();
    var str = String.valueOf(inValue);
    for (char symbol : str.toCharArray()) {
      int key;
      switch (String.valueOf(symbol)) {
        case ("0"):
          key = KeyEvent.VK_0;
          break;
        case ("1"):
          key = KeyEvent.VK_1;
          break;
        case ("2"):
          key = KeyEvent.VK_2;
          break;
        case ("3"):
          key = KeyEvent.VK_3;
          break;
        case ("4"):
          key = KeyEvent.VK_4;
          break;
        case ("5"):
          key = KeyEvent.VK_5;
          break;
        case ("6"):
          key = KeyEvent.VK_6;
          break;
        case ("7"):
          key = KeyEvent.VK_7;
          break;
        case ("8"):
          key = KeyEvent.VK_8;
          break;
        case ("9"):
          key = KeyEvent.VK_9;
          break;
        default:
          key = 0;
          break;
      }
      list.add(key);
    }
    return list;
  }

}
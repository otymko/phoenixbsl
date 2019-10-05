package org.github.otymko.phoenixbsl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class CustomRobot {

  private static final Logger log = LoggerFactory.getLogger(CustomRobot.class);
  private Robot robot;

  public CustomRobot() {
    try {
      robot = new Robot();
    } catch (AWTException e) {
      robot = null;
      log.error("Не удалось инициализировать Robot AWT");
    }
  }

  public void updateTextOnForm(String newText) {

    if (robot == null) {
      log.error("Robot не инициализирован. Выполнение команды updateTextOnForm невозможно.");
      return;
    }

    // выделить все
    robot.keyPress(KeyEvent.VK_CONTROL);
    robot.keyPress(KeyEvent.VK_A);
    robot.delay(100);
    robot.keyRelease(KeyEvent.VK_CONTROL);

    // поместить в буфер обмена
    Toolkit.getDefaultToolkit()
      .getSystemClipboard()
      .setContents(
        new StringSelection(newText),
        null
      );

    // вставить в текущую активную форму
    robot.keyPress(KeyEvent.VK_CONTROL);
    robot.keyPress(KeyEvent.VK_V);
    robot.delay(100);
    robot.keyRelease(KeyEvent.VK_CONTROL);
  }

  public void goToLineOnForm(int numberLine) {

    if (robot == null) {
      log.error("Robot не инициализирован. Выполнение команды updateTextOnForm невозможно.");
      return;
    }

    var listNumber = getListKeyEventByNumber(numberLine);

    // Перейти к строке
    robot.keyPress(KeyEvent.VK_CONTROL);
    robot.keyPress(KeyEvent.VK_G);
    robot.delay(100);
    robot.keyRelease(KeyEvent.VK_CONTROL);

    // Ввести номер строки
    for(int number : listNumber) {
      robot.keyPress(number);
      robot.delay(70);
    }

    // Выполнить переход
    robot.keyPress(KeyEvent.VK_ENTER);

  }

  private List<Integer> getListKeyEventByNumber(int inValue) {
    List<Integer> list = new ArrayList<>();
    var str = String.valueOf(inValue);
    for (char symbol : str.toCharArray()) {
      int key;
      switch (String.valueOf(symbol)){
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

      if (key != 0){
        list.add((Integer) key);
      }
    }

    return list;
  }

}

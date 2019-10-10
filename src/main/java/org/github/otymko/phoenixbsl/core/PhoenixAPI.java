package org.github.otymko.phoenixbsl.core;

import com.google.common.primitives.Chars;
import org.github.otymko.phoenixbsl.CustomRobot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhoenixAPI {

  private static final Logger log = LoggerFactory.getLogger(PhoenixAPI.class);

  public static CustomRobot robot = new CustomRobot();

  public static String getTextAll() {
    var result = "";
    clearClipboard();
    robot.Ctrl(KeyEvent.VK_A);
    robot.Ctrl(KeyEvent.VK_C);
    result = getFromClipboard();
    robot.Ctrl(KeyEvent.VK_Z);
    log.debug("getTextAll:" + result);
    return result;
  }

  public static String getTextSelected() {
    var result = "";
    clearClipboard();
    robot.Ctrl(KeyEvent.VK_X);
    result = getFromClipboard();
    if (result.length() > 0) {
      robot.Ctrl(KeyEvent.VK_Z);
    }
    log.debug("getTextSelected:" + result);
    return result;
  }

  public static void goToLineOnForm(int line) {
    var listNumber = getListKeyEventByNumber(line);
    // Окно перейти
    robot.Ctrl(KeyEvent.VK_G);
    // Номер строки
    robot.pressKeyList(listNumber);
    // Подтвержаем ввод
    robot.pressKey(KeyEvent.VK_ENTER);
  }

  public static void insetTextOnForm(String text, boolean isSelected) {
    if (!isSelected) {
      robot.Ctrl(KeyEvent.VK_A);
    }
    setTextInClipboard(text);
    robot.Ctrl(KeyEvent.VK_V);
  }

  public static void clearClipboard() {
    StringSelection stringSelection = new StringSelection("");
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
    log.debug("clearClipboard");
  }

  public static String getFromClipboard() {
    var result = "";
    try {
      result = (String) Toolkit.getDefaultToolkit()
          .getSystemClipboard().getData(DataFlavor.stringFlavor);
    } catch (UnsupportedFlavorException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    log.debug("getFromClipboard:" + result);
    return result;
  }

  public static void setTextInClipboard(String text) {
    Toolkit.getDefaultToolkit()
        .getSystemClipboard()
        .setContents(
            new StringSelection(text),
            null
        );
  }

  public static int getCurrentLineNumber() {

    var line = 0;
    robot.Alt(KeyEvent.VK_NUMPAD2);
    var textAll = getTextAll();
    robot.Ctrl(KeyEvent.VK_Z);
    String[] arrStr = textAll.split("\n");
    var count = 0;
    for (var element: arrStr) {
      count++;
      if (element.contains("☻")) { // 9787
        line = count - 1;
        break;
      }
    }
    return line;
  }


  // взаимодействия с формами
  public static boolean isWindowsForm1S(String classNameForm) {
    boolean result = true;
    result = classNameForm.contains("V8") || classNameForm.contains("SWT_Window");
    return result;
  }

  public static boolean isWindowsForm1S() {
    return isWindowsForm1S(PhoenixUser32.getForegroundWindowClass());
  }

  public static java.util.List<Integer> getListKeyEventByNumber(int inValue) {
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

  public void getFocusForm() {



  }

}

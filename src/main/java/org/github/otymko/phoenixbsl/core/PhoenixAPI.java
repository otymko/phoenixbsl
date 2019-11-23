package org.github.otymko.phoenixbsl.core;

import com.sun.jna.platform.win32.WinDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class PhoenixAPI {

  private static final String FUN_SYMBOL = "☻"; // 9787

  private static final Logger LOGGER = LoggerFactory.getLogger(PhoenixAPI.class.getSimpleName());
  private static final CustomRobot robot = new CustomRobot();

  private static boolean isWindowsForm1SByClassName(String classNameForm) {
    return classNameForm.contains("V8") || classNameForm.contains("SWT_Window");
  }

  public static boolean isWindowsForm1S() {
    return isWindowsForm1SByClassName(PhoenixUser32.getForegroundWindowClass());
  }

  public static String getTextSelected() {
    var result = "";
    clearClipboard();
    robot.Ctrl(KeyEvent.VK_X);
    result = getFromClipboard();
    if (result.length() > 0) {
      robot.Ctrl(KeyEvent.VK_Z);
    }
    LOGGER.debug("getTextSelected:" + result);
    return result;
  }

  public static void insetTextOnForm(String text, boolean isSelected) {
    if (!isSelected) {
      robot.Ctrl(KeyEvent.VK_A);
    }
    PhoenixAPI.setTextInClipboard(text);
    robot.Ctrl(KeyEvent.VK_V);
  }

  public static void gotoLineModule(int line, WinDef.HWND focusForm) {
    PhoenixUser32.setFocusWindows(focusForm);
    goToLineOnForm(line);
  }

  public static void goToLineOnForm(int line) {
    var listNumber = CustomRobot.getListKeyEventByNumber(line);
    // Окно перейти
    robot.Ctrl(KeyEvent.VK_G);
    // Номер строки
    robot.pressKeyList(listNumber);
    // Подтвержаем ввод
    robot.pressKey(KeyEvent.VK_ENTER);
  }

  public static int getCurrentLineNumber() {

    var line = 0;
    robot.Alt(KeyEvent.VK_NUMPAD2);
    var textAll = getTextAll();
    robot.Ctrl(KeyEvent.VK_Z);
    String[] arrStr = textAll.split("\n");
    var count = 0;
    for (var element : arrStr) {
      count++;
      if (element.contains(FUN_SYMBOL)) {
        line = count - 1;
        break;
      }
    }
    LOGGER.debug("Current line ofset: " + line);
    return line;
  }

  public static String getTextAll() {
    var result = "";
    clearClipboard();
    robot.Ctrl(KeyEvent.VK_A);
    robot.Ctrl(KeyEvent.VK_C);
    result = getFromClipboard();
    LOGGER.debug("getTextAll:" + result);
    return result;
  }

  // Взаимодействие с буфером обмена

  public static void setTextInClipboard(String text) {
    Toolkit.getDefaultToolkit()
      .getSystemClipboard()
      .setContents(
        new StringSelection(text),
        null
      );
  }

  private static void clearClipboard() {
    LOGGER.debug("clearClipboard");
    var stringSelection = new StringSelection("");
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
  }

  private static String getFromClipboard() {
    var result = "";
    try {
      result = getDataClipboard();
    } catch (IOException | UnsupportedFlavorException e) {
      LOGGER.error(e.getMessage());
    }
    return result;
  }

  private static String getDataClipboard() throws IOException, UnsupportedFlavorException {

    try {
      Thread.sleep(20);
    } catch (InterruptedException e) {
      LOGGER.error(e.getMessage());
    }

    return (String) Toolkit.getDefaultToolkit()
      .getSystemClipboard().getData(DataFlavor.stringFlavor);
  }

  public static void showMessageDialog(String message) {
    JOptionPane.showMessageDialog(new JFrame(), message);
  }
}

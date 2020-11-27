package com.github.otymko.phoenixbsl.logic;

import com.sun.jna.platform.win32.WinDef;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.lang.management.ManagementFactory;

@Slf4j
@UtilityClass
public class PhoenixAPI {
  private final String FUN_SYMBOL = "☻"; // 9787
  private final CustomRobot robot = new CustomRobot();
  private final CustomTextTransfer textTransfer = new CustomTextTransfer();

  private boolean isWindowsForm1SByClassName(String classNameForm) {
    return classNameForm.contains("V8") || classNameForm.contains("SWT_Window");
  }

  public boolean isWindowsForm1S() {
    return isWindowsForm1SByClassName(PhoenixUser32.getForegroundWindowClass());
  }

  public String getTextSelected() {
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

  public void insetTextOnForm(String text, boolean isSelected) {
    if (!isSelected) {
      robot.Ctrl(KeyEvent.VK_A);
    }
    PhoenixAPI.setTextInClipboard(text);
    robot.Ctrl(KeyEvent.VK_V);
  }

  public void gotoLineModule(int line, WinDef.HWND focusForm) {
    PhoenixUser32.setFocusWindows(focusForm);
    goToLineOnForm(line);
  }

  public void goToLineOnForm(int line) {
    var listNumber = CustomRobot.getListKeyEventByNumber(line);
    // Окно перейти
    robot.Ctrl(KeyEvent.VK_G);
    // Номер строки
    robot.pressKeyList(listNumber);
    // Подтвержаем ввод
    robot.pressKey(KeyEvent.VK_ENTER);
  }

  public int getCurrentLineNumber() {

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
    LOGGER.debug("Current line offset: " + line);
    return line;
  }

  public String getTextAll() {
    var result = "";
    clearClipboard();
    robot.Ctrl(KeyEvent.VK_A);
    robot.Ctrl(KeyEvent.VK_C);
    result = getFromClipboard();
    LOGGER.debug("getTextAll:" + result);
    return result;
  }

  // Взаимодействие с буфером обмена

  public void setTextInClipboard(String text) {
    textTransfer.setClipboardContents(text);
  }

  private void clearClipboard() {
    LOGGER.debug("clearClipboard");
    textTransfer.setClipboardContents("");
  }

  private String getFromClipboard() {
    return textTransfer.getClipboardContents();
  }

  public int getProcessId() {
    var bean = ManagementFactory.getRuntimeMXBean();
    var jvmName = bean.getName();
    long pid = Long.parseLong(jvmName.split("@")[0]);
    return (int) pid;
  }

  public void showMessageDialog(String message) {
    JOptionPane.showMessageDialog(new JFrame(), message);
  }

}

package com.github.otymko.phoenixbsl.logic;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

public class PhoenixUser32 {
  public static final User32 user32 = User32.INSTANCE;

  private PhoenixUser32() {
    // noon
  }

  public static String getForegroundWindowClass() {
    WinDef.HWND hwnd = getHWNDFocusForm();
    return getClassNameForm(hwnd);
  }

  public static String getClassNameForm(WinDef.HWND hwnd) {
    char[] windowClass = new char[512];
    user32.GetClassName(hwnd, windowClass, 512);
    return Native.toString(windowClass);
  }

  public static WinDef.HWND getHWNDFocusForm() {
    return user32.GetForegroundWindow();
  }

  public static void setFocusWindows(WinDef.HWND hwnd) {
    user32.SetFocus(hwnd);
    user32.SetForegroundWindow(hwnd);
  }

}
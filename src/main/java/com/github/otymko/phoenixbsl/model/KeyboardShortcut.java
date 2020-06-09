package com.github.otymko.phoenixbsl.model;

import java.util.ArrayList;
import java.util.List;

public class KeyboardShortcut extends Object{

  private boolean controlPressed = false;
  private List<Integer> keyList = new ArrayList<>();

  public KeyboardShortcut(List<Integer> keyList, boolean controlPressed) {
    this.controlPressed = controlPressed;
    this.keyList = keyList;
  }

  public KeyboardShortcut(int key, boolean controlPressed) {
    this.controlPressed = controlPressed;
    keyList.add(key);
  }

  public boolean equals(KeyboardShortcut obj)
  {
    return obj.controlPressed == controlPressed && keyList.equals(obj.keyList);
  }

}

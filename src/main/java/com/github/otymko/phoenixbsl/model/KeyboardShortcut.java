package com.github.otymko.phoenixbsl.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class KeyboardShortcut {
  private final boolean controlPressed;
  private final List<Integer> keyList;

  public KeyboardShortcut(List<Integer> keyList, boolean controlPressed) {
    this.controlPressed = controlPressed;
    this.keyList = keyList;
  }

  public KeyboardShortcut(int key, boolean controlPressed) {
    this.controlPressed = controlPressed;
    keyList = new ArrayList<>();
    keyList.add(key);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (this.getClass() != obj.getClass()) {
      return false;
    }

    var object = (KeyboardShortcut) obj;
    return object.controlPressed == controlPressed && keyList.equals(object.keyList);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}

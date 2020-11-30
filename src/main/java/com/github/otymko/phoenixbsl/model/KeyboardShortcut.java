package com.github.otymko.phoenixbsl.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode
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
}

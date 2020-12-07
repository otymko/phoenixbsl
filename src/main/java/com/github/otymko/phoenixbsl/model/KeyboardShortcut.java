package com.github.otymko.phoenixbsl.model;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@RequiredArgsConstructor
public class KeyboardShortcut {
  List<Integer> keyList;
  boolean controlPressed;

  public KeyboardShortcut(int key, boolean controlPressed) {
    this.controlPressed = controlPressed;
    this.keyList = List.of(key);
  }
}

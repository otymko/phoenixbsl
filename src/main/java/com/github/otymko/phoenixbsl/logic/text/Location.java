package com.github.otymko.phoenixbsl.logic.text;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class Location {
  private static final Location EMPTY = new Location(0, 0);

  int startLine;
  int endLine;

  public static Location empty() {
    return EMPTY;
  }
}

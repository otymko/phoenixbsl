package com.github.otymko.phoenixbsl.logic.text;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class SourceText {
  String content;
  int offset;
}

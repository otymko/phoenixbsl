package com.github.otymko.phoenixbsl.logic.utils;

import com.github.otymko.phoenixbsl.logic.text.Constant;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TextUtil {

  public String pasteSelectionInText(String text, String selection) {
    return text.replace(Constant.FUN_SYMBOL, selection);
  }

  public int numberOfLinesInText(String text) {
    return text.split(Constant.SEPARATOR).length;
  }

}

package com.github.otymko.phoenixbsl.logic;

import com.github.otymko.phoenixbsl.PhoenixCore;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

@Slf4j
public class CustomTextTransfer implements ClipboardOwner {

  @Override
  public void lostOwnership(Clipboard clipboard, Transferable contents) {
    // none
  }

  public void setClipboardContents(String content) {
    var stringSelection = new StringSelection(content);
    var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    PhoenixCore.getInstance().sleepCurrentThread(50);
    try {
      clipboard.setContents(stringSelection, this);
    } catch (IllegalStateException e) {
      LOGGER.error("Не удалось обновить значение в буфере обмена", e);
    }

  }

  public String getClipboardContents() {
    PhoenixCore.getInstance().sleepCurrentThread(50);
    String content = "";
    var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    var contents = clipboard.getContents(null);
    var hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
    if (hasTransferableText) {
      try {
        content = (String) contents.getTransferData(DataFlavor.stringFlavor);
      } catch (UnsupportedFlavorException | IOException ex) {
        LOGGER.error(ex.getMessage(), ex);
      }
    }
    return content;
  }

}

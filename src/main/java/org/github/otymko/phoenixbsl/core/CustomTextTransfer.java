package org.github.otymko.phoenixbsl.core;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;

@Slf4j
public class CustomTextTransfer implements ClipboardOwner {

  private StringSelection stringSelection;

  @Override
  public void lostOwnership(Clipboard clipboard, Transferable contents) {
    // TODO Auto-generated method stub
  }

  public void setClipboardContents(String content) {
    stringSelection = new StringSelection(content);
    var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(stringSelection, this);
  }

  public String getClipboardContents() {
    String content = "";
    var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    var contents = clipboard.getContents(null);
    var hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
    if (hasTransferableText) {
      try {
        content = (String) contents.getTransferData(DataFlavor.stringFlavor);
      } catch (UnsupportedFlavorException ex) {
        LOGGER.error(ex.getMessage());
      } catch (IOException ex) {
        LOGGER.error(ex.getMessage());
      }
    }
    return content;
  }

}

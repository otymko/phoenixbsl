package org.github.otymko.phoenixbsl.views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Toolbar {

  private static final Logger log = LoggerFactory.getLogger(Toolbar.class);
  private static final String PATH_TO_ICON = "/phoenix.jpg";
  private static final String APP_NAME = "PhoenixBSL для 1С";

  private static final String ITEM_NAME_SETTING = "Настройки";
  private static final String ITEM_NAME_EXIT = "Закрыть";

  public Toolbar() {

    var popup = new PopupMenu();

    var settingItem = new MenuItem(ITEM_NAME_SETTING);
    settingItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      }
    });
    popup.add(settingItem);

    var exitItem = new MenuItem(ITEM_NAME_EXIT);
    exitItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    });
    popup.add(exitItem);

    var systemTray = SystemTray.getSystemTray();
    var icon = new ImageIcon(getClass().getResource(PATH_TO_ICON));
    var image = icon.getImage();

    var trayIcon = new TrayIcon(image, APP_NAME, popup);
    trayIcon.setImageAutoSize(true);
    try {
      systemTray.add(trayIcon);
    } catch (AWTException e) {
      log.error(e.getMessage());
    }

  }

}

package com.github.otymko.phoenixbsl.gui;

import com.github.otymko.phoenixbsl.PhoenixCore;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Slf4j
public class Toolbar {
  private static final String PATH_TO_ICON = "/phoenix.png";

  public Toolbar() {
    init();
  }

  private void init() {
    var popupMenu = new PopupMenu();

    var settingItem = new MenuItem("Настройки");
    settingItem.addActionListener(event -> PhoenixCore.getInstance().showSettingStage());
    popupMenu.add(settingItem);

    var exitItem = new MenuItem("Закрыть");
    exitItem.addActionListener(event -> {
      Platform.exit();
      PhoenixCore.getInstance().stopBSLLS();
      System.exit(0);
    });
    popupMenu.add(exitItem);

    var systemTray = SystemTray.getSystemTray();
    var icon = new ImageIcon(PhoenixCore.class.getResource(PATH_TO_ICON));
    var image = icon.getImage();

    TrayIcon trayIcon = new TrayIcon(image, "Phoenix BSL", popupMenu);
    trayIcon.setImageAutoSize(true);
    trayIcon.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          PhoenixCore.getInstance().showIssuesStage();
        }
      }
    });
    try {
      systemTray.add(trayIcon);
    } catch (AWTException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

}

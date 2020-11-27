package com.github.otymko.phoenixbsl.gui;

import com.github.otymko.phoenixbsl.PhoenixCore;
import javafx.application.Platform;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Toolbar {

  private static final String PATH_TO_ICON = "/phoenix.png";

  private PopupMenu popupMenu;

  public Toolbar() {
    init();
  }

  private void init() {
    popupMenu = new PopupMenu();

    var settingItem = new MenuItem("Настройки");
    settingItem.addActionListener(e -> {
      PhoenixCore.getInstance().showSettingStage();
    });
    popupMenu.add(settingItem);

    var exitItem = new MenuItem("Закрыть");
    exitItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Platform.exit();
        PhoenixCore.getInstance().stopBSLLS();
        System.exit(0);
      }
    });
    popupMenu.add(exitItem);

    var systemTray = SystemTray.getSystemTray();
    var icon = new ImageIcon(PhoenixCore.class.getResource(PATH_TO_ICON));
    var image = icon.getImage();

    TrayIcon trayIcon = new TrayIcon(image, "Phoenix BSL", popupMenu);
    trayIcon.setImageAutoSize(true);
    trayIcon.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          PhoenixCore.getInstance().showIssuesStage();
        }
      }
    });
    try {
      systemTray.add(trayIcon);
    } catch (AWTException e) {
      System.out.println(e.getMessage());
    }
  }

}

package org.github.otymko.phoenixbsl.views;

import javafx.application.Application;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Toolbar {

  public Toolbar() {

    PopupMenu popup = new PopupMenu();

    MenuItem settingItem = new MenuItem("Настройки");
    settingItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Application.launch(SettingsFormApplication.class, "");
      }
    });
    popup.add(settingItem);

    MenuItem exitItem = new MenuItem("Закрыть");
    exitItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    });
    popup.add(exitItem);

    SystemTray systemTray = SystemTray.getSystemTray();

    ImageIcon icon = new ImageIcon("src/main/resources/phoenix.jpg");
    Image image = icon.getImage();

    TrayIcon trayIcon = new TrayIcon(image, "PhoenixBSL для 1С", popup);
    trayIcon.setImageAutoSize(true);
    try {
      systemTray.add(trayIcon);
    } catch (AWTException e) {
      e.printStackTrace();
    }

  }

}

package org.github.otymko.phoenixbsl.core;

import com.sun.jna.platform.win32.WinDef;
import org.github.otymko.phoenixbsl.events.EventListener;
import org.github.otymko.phoenixbsl.events.EventManager;
import org.github.otymko.phoenixbsl.lsp.BSLHelper;
import org.github.otymko.phoenixbsl.lsp.BSLLanguageServer;
import org.github.otymko.phoenixbsl.views.IssuesForm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class PhoenixApp implements EventListener {

  private static final String PATH_TO_ICON = "/phoenix.jpg";
  private static final PhoenixApp INSTANCE = new PhoenixApp();
  private EventManager events;
  private PopupMenu toolbar;
  private IssuesForm issuesForm;
  private WinDef.HWND focusForm;
  private Process processBSL;
  public BSLLanguageServer bslLanguageServer;

  private PhoenixApp() {

    bslLanguageServer = null;

    events = new EventManager(EventManager.EVENT_INSPECTION, EventManager.EVENT_FORMATTING);
    events.subscribe(EventManager.EVENT_INSPECTION, this);
    events.subscribe(EventManager.EVENT_FORMATTING, this);

    issuesForm = new IssuesForm();
  }

  public static PhoenixApp getInstance() {
    return INSTANCE;
  }

  public void initProcessBSL() {
    processBSL = null;
    Path path = Paths.get(".", "languageserver/bsl-language-server.jar");
    String[] arguments = new String[]{
      "java", "-jar", path.toAbsolutePath().toString()};
    try {
      processBSL = new ProcessBuilder(arguments).start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void initToolbar() {
    toolbar = new PopupMenu();

    var settingItem = new MenuItem("Настройки");
    toolbar.add(settingItem);

    var exitItem = new MenuItem("Закрыть");
    exitItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    });
    toolbar.add(exitItem);

    var systemTray = SystemTray.getSystemTray();
    var icon = new ImageIcon(PhoenixApp.class.getResource(PATH_TO_ICON));
    var image = icon.getImage();

    TrayIcon trayIcon = new TrayIcon(image, "Phoenix BSL", toolbar);
    trayIcon.setImageAutoSize(true);
    try {
      systemTray.add(trayIcon);
    } catch (AWTException e) {
      System.out.println(e.getMessage());
    }
  }

  public boolean appIsRunning() {
    var thisPid = ProcessHandle.current().pid();
    AtomicBoolean isRunning = new AtomicBoolean(false);
    ProcessHandle.allProcesses()
      .filter(
        ph -> ph.info().command().isPresent()
          && ph.info().command().get().contains("phoenixbsl")
          && ph.pid() != thisPid)
      .forEach((process) -> {
        isRunning.set(true);
      });
    return isRunning.get();
  }

  public void abort() {
    //log.error("Приложение уже запущено");
    JOptionPane.showMessageDialog(new JFrame(), "Приложение уже запущено. Повторный запуск невозможен.");
    System.exit(0);
  }

  public boolean processBSLIsRunning() {
    return processBSL != null;
  }

  public Process getProcessBSL() {
    return processBSL;
  }

  public EventManager getEventManager() {
    return events;
  }

  @Override
  public void inspection() {

    if (processBSLIsRunning() && PhoenixAPI.isWindowsForm1S()) {
      updateFocusForm();
    } else {
      return;
    }

    if (bslLanguageServer == null) {
      return;
    }

    var lineOfset = 0;
    var textForCheck = "";
    var textModuleSelected = PhoenixAPI.getTextSelected();
    if (textModuleSelected.length() > 0) {
      // получем номер строки
      textForCheck = textModuleSelected;
      lineOfset = PhoenixAPI.getCurrentLineNumber();
    } else {
      textForCheck = PhoenixAPI.getTextAll();
    }

    issuesForm.setLineOfset(lineOfset);

    BSLHelper.textDocumentDidChange(bslLanguageServer, textForCheck);
    BSLHelper.textDocumentDidSave(bslLanguageServer);

  }

  @Override
  public void formatting() {

    if (!(processBSLIsRunning() && PhoenixAPI.isWindowsForm1S())) {
      return;
    }

    var textForFormatting = "";
    var isSelected = false;
    var textModuleSelected = PhoenixAPI.getTextSelected();
    if (textModuleSelected.length() > 0) {
      textForFormatting = textModuleSelected;
      isSelected = true;
    } else {
      textForFormatting = PhoenixAPI.getTextAll();
    }

    // DidChange
    BSLHelper.textDocumentDidChange(bslLanguageServer, textForFormatting);

    // Formatting
    var listEdits = BSLHelper.textDocumentFormatting(bslLanguageServer);

    try {
      PhoenixAPI.insetTextOnForm(listEdits.get().get(0).getNewText(), isSelected);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }

  }

  public IssuesForm getIssuesForm() {
    return this.issuesForm;
  }

  private void updateFocusForm() {
    focusForm = PhoenixUser32.getHWNDFocusForm();
  }

  public WinDef.HWND getFocusForm() {
    return focusForm;
  }

}
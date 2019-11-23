package org.github.otymko.phoenixbsl.core;

import com.sun.jna.platform.win32.WinDef;
import org.github.otymko.phoenixbsl.events.EventListener;
import org.github.otymko.phoenixbsl.events.EventManager;
import org.github.otymko.phoenixbsl.lsp.BSLHelper;
import org.github.otymko.phoenixbsl.lsp.BSLLanguageServer;
import org.github.otymko.phoenixbsl.views.IssuesForm;
import org.github.otymko.phoenixbsl.views.Toolbar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class PhoenixApp implements EventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(PhoenixApp.class.getSimpleName());
  private static final PhoenixApp INSTANCE = new PhoenixApp();

  private EventManager events;
  private IssuesForm issuesForm;
  private WinDef.HWND focusForm;
  private Process processBSL;
  private BSLLanguageServer languageServer = null;

  private PhoenixApp() {

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
    var path = Paths.get(".", "languageserver/bsl-language-server.jar");
    var arguments = new String[]{
      "java", "-jar", path.toAbsolutePath().toString()};
    try {
      processBSL = new ProcessBuilder(arguments).start();
    } catch (IOException e) {
      LOGGER.error(e.getMessage().toString());
    }
  }

  public void initToolbar() {
    var toolbar = new Toolbar();
  }

  public boolean appIsRunning() {
    var thisPid = ProcessHandle.current().pid();
    var isRunning = new AtomicBoolean(false);
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
    LOGGER.error("Приложение уже запущено");
    PhoenixAPI.showMessageDialog("Приложение уже запущено. Повторный запуск невозможен.");
    System.exit(0);
  }

  public boolean processBSLIsRunning() {
    return processBSL != null;
  }

  public Process getProcessBSL() {
    return processBSL;
  }

  public void setLanguageServer(BSLLanguageServer languageServer) {
    this.languageServer = languageServer;
  }

  public EventManager getEventManager() {
    return events;
  }

  @Override
  public void inspection() {

    LOGGER.debug("Event: inspection");

    if (processBSLIsRunning() && PhoenixAPI.isWindowsForm1S()) {
      updateFocusForm();
    } else {
      return;
    }

    if (languageServer == null) {
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

    BSLHelper.textDocumentDidChange(languageServer, textForCheck);
    BSLHelper.textDocumentDidSave(languageServer);

  }

  @Override
  public void formatting() {

    LOGGER.debug("Event: formatting");

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
    BSLHelper.textDocumentDidChange(languageServer, textForFormatting);

    // Formatting
    var result = BSLHelper.textDocumentFormatting(languageServer);

    try {
      PhoenixAPI.insetTextOnForm(result.get().get(0).getNewText(), isSelected);
    } catch (InterruptedException e) {
      LOGGER.error(e.getMessage().toString());
    } catch (ExecutionException e) {
      LOGGER.error(e.getMessage().toString());
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
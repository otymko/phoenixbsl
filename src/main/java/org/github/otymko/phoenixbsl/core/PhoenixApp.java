package org.github.otymko.phoenixbsl.core;

import com.sun.jna.platform.win32.WinDef;
import org.github.otymko.phoenixbsl.events.EventListener;
import org.github.otymko.phoenixbsl.events.EventManager;
import org.github.otymko.phoenixbsl.lsp.BSLBinding;
import org.github.otymko.phoenixbsl.views.Toolbar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class PhoenixApp implements EventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(PhoenixApp.class.getSimpleName());
  private static final PhoenixApp INSTANCE = new PhoenixApp();

  public static final URI fakeUri = new File("C:/BSL/fake.bsl").toPath().toAbsolutePath().toUri();

  private EventManager events;
  private WinDef.HWND focusForm;
  private Process processBSL;
  private BSLBinding bslBinding = null;


  public int currentOffset = 0;

  private PhoenixApp() {

    events = new EventManager(EventManager.EVENT_INSPECTION, EventManager.EVENT_FORMATTING, EventManager.EVENT_UPDATE_ISSUES);
    events.subscribe(EventManager.EVENT_INSPECTION, this);
    events.subscribe(EventManager.EVENT_FORMATTING, this);

  }

  public static PhoenixApp getInstance() {
    return INSTANCE;
  }

  public void initProcessBSL() {

    processBSL = null;
    String[] arguments;
    var pathApp = Path.of(".", "app/bsl-language-server/bsl-language-server.exe");
    if (pathApp.toFile().exists()) {
      LOGGER.info("BLS LS app image is exist");
      arguments = new String[]{pathApp.toAbsolutePath().toString()};
    } else {
      LOGGER.error("Not find bsl ls");
      return;
    }
    try {
      processBSL = new ProcessBuilder(arguments).start();
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
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

  public void setBslBinding(BSLBinding bslBinding) {
    this.bslBinding = bslBinding;
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

    if (bslBinding == null) {
      return;
    }

    currentOffset = 0;
    var textForCheck = "";
    var textModuleSelected = PhoenixAPI.getTextSelected();
    if (textModuleSelected.length() > 0) {
      // получем номер строки
      textForCheck = textModuleSelected;
      currentOffset = PhoenixAPI.getCurrentLineNumber();
    } else {
      textForCheck = PhoenixAPI.getTextAll();
    }

    bslBinding.textDocumentDidChange(fakeUri, textForCheck);
    bslBinding.textDocumentDidSave(fakeUri);

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
    bslBinding.textDocumentDidChange(fakeUri, textForFormatting);

    // Formatting
    var result = bslBinding.textDocumentFormatting(fakeUri);

    try {
      PhoenixAPI.insetTextOnForm(result.get().get(0).getNewText(), isSelected);
    } catch (InterruptedException e) {
      LOGGER.error(e.getMessage());
    } catch (ExecutionException e) {
      LOGGER.error(e.getMessage());
    }

  }

  public void stopBSL() {
    if (bslBinding == null) {
      return;
    }
    bslBinding.shutdown();
    bslBinding.exit();
  }

  private void updateFocusForm() {
    focusForm = PhoenixUser32.getHWNDFocusForm();
  }

  public WinDef.HWND getFocusForm() {
    return focusForm;
  }

  public URI getFakeUri() {
    return fakeUri;
  }
}
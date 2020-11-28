package com.github.otymko.phoenixbsl.logic.designer;

import com.github.otymko.phoenixbsl.PhoenixCore;
import com.github.otymko.phoenixbsl.logic.PhoenixAPI;
import com.github.otymko.phoenixbsl.logic.PhoenixUser32;
import com.github.otymko.phoenixbsl.logic.event.EventListener;
import com.github.otymko.phoenixbsl.logic.event.EventManager;
import com.sun.jna.platform.win32.WinDef;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.lsp4j.Diagnostic;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DesignerTextEditor implements EventListener {
  public static final List<String> DIAGNOSTIC_FOR_QF = createDiagnosticListForQuickFix();
  private final PhoenixCore core;
  @Getter
  @Setter
  private Path pathToFile;
  @Getter
  @Setter
  private String content = "";
  @Getter
  private WinDef.HWND focusForm;
  @Getter
  private final List<Diagnostic> diagnostics = new ArrayList<>();
  @Getter
  @Setter
  private int currentOffset = 0;

  public DesignerTextEditor(PhoenixCore core) {
    this.core = core;

    var eventManager = core.getEventManager();
    eventManager.subscribe(EventManager.EVENT_INSPECTION, this);
    eventManager.subscribe(EventManager.EVENT_FORMATTING, this);
    eventManager.subscribe(EventManager.EVENT_FIX_ALL, this);
  }

  @Override
  public void inspection() {
    LOGGER.debug("Событие: анализ кода");
    if (PhoenixAPI.isWindowsForm1S()) {
      updateFocusForm();
    } else {
      return;
    }
    if (core.getLsService().isAlive()) {
      core.getLsService().validate();
    }
  }

  @Override
  public void formatting() {
    LOGGER.debug("Событие: форматирование");
    if (!PhoenixAPI.isWindowsForm1S()) {
      return;
    }
    if (core.getLsService().isAlive()) {
      core.getLsService().formatting();
    }
  }

  @Override
  public void fixAll() {
    LOGGER.debug("Событие: обработка квикфиксов");
    if (!PhoenixAPI.isWindowsForm1S()) {
      return;
    }
    if (core.getLsService().isAlive()) {
      core.getLsService().fixAll();
    }
  }

  @SneakyThrows
  public void saveContent() {
    var fooStream = new FileOutputStream(pathToFile.toFile(), false);
    fooStream.write(content.getBytes(StandardCharsets.UTF_8));
    fooStream.close();
  }

  public void updateFocusForm() {
    focusForm = PhoenixUser32.getHWNDFocusForm();
  }

  private static List<String> createDiagnosticListForQuickFix() {
    var list = new ArrayList<String>();
    list.add("CanonicalSpellingKeywords");
    list.add("SpaceAtStartComment");
    list.add("SemicolonPresence");
    return list;
  }

}

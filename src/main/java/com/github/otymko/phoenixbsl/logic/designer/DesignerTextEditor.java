package com.github.otymko.phoenixbsl.logic.designer;

import com.github.otymko.phoenixbsl.PhoenixCore;
import com.github.otymko.phoenixbsl.logic.PhoenixAPI;
import com.github.otymko.phoenixbsl.logic.PhoenixUser32;
import com.github.otymko.phoenixbsl.logic.event.EventListener;
import com.github.otymko.phoenixbsl.logic.event.EventManager;
import com.sun.jna.platform.win32.WinDef;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.lsp4j.Diagnostic;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class DesignerTextEditor implements EventListener {
  public static final List<String> DIAGNOSTIC_FOR_QF = createDiagnosticListForQuickFix();
  public static final List<String> FILTER_ACTION_QUICKFIX = createListFilterActionQuickFix();
  public static final String SEPARATOR = "\n";
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
  private final List<Diagnostic> diagnostics = Collections.synchronizedList(new ArrayList<>());
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
    if (!PhoenixAPI.isWindowsForm1S()) {
      return;
    }
    updateFocusForm();

    var path = core.getProject().getFakePath();
    var textForCheck = getTextFormDesigner();
    core.updateContent(path, textForCheck);

    if (core.getLsService().isAlive()) {
      core.getLsService().validate(path, content);
    }

    if (core.getProject().isUseSonarLint() && core.getLintService().isAlive()) {
      core.getLintService().validate(path, content);
    }

  }

  @Override
  public void formatting() {
    LOGGER.debug("Событие: форматирование");
    if (!PhoenixAPI.isWindowsForm1S()) {
      return;
    }
    updateFocusForm();

    var path = core.getProject().getFakePath();
    var formattingText = getFormattingText();
    core.updateContent(path, formattingText.getContext());

    if (core.getLsService().isAlive()) {
      core.getLsService().formatting(path, formattingText);
      if (formattingText.getContext() != null) {
        PhoenixAPI.insetTextOnForm(formattingText.getContext(), formattingText.isSelected());
      }
    }
  }

  @Override
  public void fixAll() {
    LOGGER.debug("Событие: обработка квикфиксов");
    if (!PhoenixAPI.isWindowsForm1S()) {
      return;
    }
    updateFocusForm();

    var path = core.getProject().getFakePath();
    var text = PhoenixAPI.getTextAll();
    core.updateContent(path, text);

    if (core.getLsService().isAlive()) {
      core.getLsService().fixAll(path, text);
    }
  }

  public void saveContent() {
    try (var fooStream = new FileOutputStream(pathToFile.toFile(), false)) {
      fooStream.write(content.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

  public void updateContentDesigner(String text, boolean isSelected) {
    PhoenixAPI.insetTextOnForm(text, isSelected);
  }

  public void updateFocusForm() {
    focusForm = PhoenixUser32.getHWNDFocusForm();
  }

  private String getTextFormDesigner() {
    setCurrentOffset(0);
    var textForCheck = "";
    var textModuleSelected = PhoenixAPI.getTextSelected();
    if (textModuleSelected.length() > 0) {
      // получем номер строки
      textForCheck = textModuleSelected;
      setCurrentOffset(PhoenixAPI.getCurrentLineNumber());
    } else {
      textForCheck = PhoenixAPI.getTextAll();
    }
    return textForCheck;
  }

  private FormattingText getFormattingText() {
    var textForFormatting = "";
    var isSelected = false;
    var textModuleSelected = PhoenixAPI.getTextSelected();
    if (textModuleSelected.length() > 0) {
      textForFormatting = textModuleSelected;
      isSelected = true;
    } else {
      textForFormatting = PhoenixAPI.getTextAll();
    }
    return new FormattingText(textForFormatting, isSelected);
  }

  private static List<String> createDiagnosticListForQuickFix() {
    var list = new ArrayList<String>();
    list.add("CanonicalSpellingKeywords");
    list.add("SpaceAtStartComment");
    list.add("SemicolonPresence");
    return list;
  }

  private static List<String> createListFilterActionQuickFix() {
    List<String> onlyKind = new ArrayList<>();
    onlyKind.add("quickfix");
    return onlyKind;
  }

}

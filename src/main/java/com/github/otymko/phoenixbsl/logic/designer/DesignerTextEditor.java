package com.github.otymko.phoenixbsl.logic.designer;

import com.github.otymko.phoenixbsl.PhoenixCore;
import com.github.otymko.phoenixbsl.logic.PhoenixUser32;
import com.sun.jna.platform.win32.WinDef;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.lsp4j.Diagnostic;

import java.util.ArrayList;
import java.util.List;

public class DesignerTextEditor {
  public static final List<String> DIAGNOSTIC_FOR_QF = createDiagnosticListForQuickFix();
  private final PhoenixCore core;

  @Getter
  private WinDef.HWND focusForm;
  @Getter
  private List<Diagnostic> diagnostics = new ArrayList<>();

  @Getter
  @Setter
  private int currentOffset = 0;

  public DesignerTextEditor(PhoenixCore core) {
    this.core = core;
  }

  private static List<String> createDiagnosticListForQuickFix() {
    var list = new ArrayList<String>();
    list.add("CanonicalSpellingKeywords");
    list.add("SpaceAtStartComment");
    list.add("SemicolonPresence");
    return list;
  }

  public void updateFocusForm() {
    focusForm = PhoenixUser32.getHWNDFocusForm();
  }
}

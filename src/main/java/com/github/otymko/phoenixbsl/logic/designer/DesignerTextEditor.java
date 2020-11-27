package com.github.otymko.phoenixbsl.logic.designer;

import com.github.otymko.phoenixbsl.logic.PhoenixUser32;
import com.sun.jna.platform.win32.WinDef;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.eclipse.lsp4j.Diagnostic;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DesignerTextEditor {
  public static final List<String> DIAGNOSTIC_FOR_QF = createDiagnosticListForQuickFix();
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

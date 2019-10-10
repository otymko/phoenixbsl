package org.github.otymko.phoenixbsl;

import com.sun.jna.platform.win32.WinDef;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.github._1c_syntax.bsl.languageserver.configuration.LanguageServerConfiguration;
import org.github._1c_syntax.bsl.languageserver.context.ServerContext;
import org.github._1c_syntax.bsl.languageserver.providers.DiagnosticProvider;
import org.github._1c_syntax.bsl.languageserver.providers.FormatProvider;
import org.github.otymko.phoenixbsl.core.PhoenixAPI;
import org.github.otymko.phoenixbsl.core.PhoenixUser32;
import org.github.otymko.phoenixbsl.events.GlobalKeyboardHookHandler;
import org.github.otymko.phoenixbsl.views.IssuesForm;
import org.github.otymko.phoenixbsl.views.Toolbar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class App {

  private static final App INSTANCE = new App();
  private static final Logger log = LoggerFactory.getLogger(App.class);

  private final String FAKE_PATH_FILE = "module.bsl";
  private final String REGEX_FORM_TITLE = "Конфигуратор|Designer";
  private final int UIA_CONTROL_DOCUMENT = 50030;
  private final int UIA_PROPERTY_VALUE = 30045;

  private final File fakeFile = new File(FAKE_PATH_FILE);
  private Pattern pattern = Pattern.compile(REGEX_FORM_TITLE, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
  private DiagnosticProvider diagnosticProvider;
  private IssuesForm issuesForm;
  private WinDef.HWND focusForm;

  public App() {
  }

  public static App getInstance() {
    return INSTANCE;
  }

  public void run() {

    // инициализация для BSL LS
    diagnosticProvider = new DiagnosticProvider(LanguageServerConfiguration.create());

    // меню в системный трей
    var toolbar = new Toolbar();

    // единая форма на все
    issuesForm = new IssuesForm();

    // хук на нажатия кнопок
    var hookHandler = new GlobalKeyboardHookHandler();

    log.info("Приложение запущено");

  }

  public void abort() {
    log.error("Приложение уже запущено");
    JOptionPane.showMessageDialog(new JFrame(), "Приложение уже запущено. Повторный запуск невозможен. ");
    System.exit(0);
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

  public void runCheckBSL() {

    var lineOfset = 0;
    var textForCheck = "";
    var textModuleSelected = PhoenixAPI.getTextSelected();
    if (textModuleSelected.length() > 0) {
      // получем номер строки
      textForCheck = textModuleSelected;
      lineOfset = PhoenixAPI.getCurrentLineNumber();
    }
    else {
      textForCheck = PhoenixAPI.getTextAll();
    }
    var bslServerContext = new ServerContext();
    var documentContext = bslServerContext.addDocument(fakeFile.toURI().toString(), textForCheck);
    var list = diagnosticProvider.computeDiagnostics(documentContext);

    issuesForm.setLineOfset(lineOfset);
    issuesForm.updateIssues(list);
    issuesForm.onVisible();

  }

  public void runFormattingBSL() {

    var textForFormatting = "";
    var isSelected = false;
    var textModuleSelected = PhoenixAPI.getTextSelected();
    if (textModuleSelected.length() > 0) {
      textForFormatting = textModuleSelected;
      isSelected = true;
    }
    else {
      textForFormatting = PhoenixAPI.getTextAll();;
    }

    var bslServerContext = new ServerContext();
    var params = new DocumentFormattingParams();
    params.setTextDocument(getTextDocumentIdentifier(fakeFile));
    params.setOptions(new FormattingOptions(4, false));

    var documentContext = bslServerContext.addDocument(fakeFile.toURI().toString(), textForFormatting);
    var newText = FormatProvider.getFormatting(params, documentContext);

    PhoenixAPI.insetTextOnForm(newText.get(0).getNewText(), isSelected);
  }

  public void gotoLineModule(int line) {
    log.info("Line: " + line);
    PhoenixUser32.setFocusWindows(focusForm);
    PhoenixAPI.goToLineOnForm(line);
  }

  public void updateFocusForm() {
    focusForm = PhoenixUser32.getHWNDFocusForm();
  }

  private TextDocumentIdentifier getTextDocumentIdentifier(File file) {
    var uri = file.toURI().toString();
    return new TextDocumentIdentifier(uri);
  }

}

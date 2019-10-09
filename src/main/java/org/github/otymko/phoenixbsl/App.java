package org.github.otymko.phoenixbsl;

import mmarquee.automation.AutomationException;
import mmarquee.automation.Element;
import mmarquee.automation.UIAutomation;
import mmarquee.automation.controls.Window;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.github._1c_syntax.bsl.languageserver.configuration.LanguageServerConfiguration;
import org.github._1c_syntax.bsl.languageserver.context.ServerContext;
import org.github._1c_syntax.bsl.languageserver.providers.DiagnosticProvider;
import org.github._1c_syntax.bsl.languageserver.providers.FormatProvider;
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

  private UIAutomation automation;
  private DiagnosticProvider diagnosticProvider;

  private int thisIdProcess = 0;
  private Window thisForm;
  private Element focusElement;
  private String tmpTextModule;

  private String tmpTextSelectedModule;


  private IssuesForm issuesForm;

  public App() {
  }

  public static App getInstance() {
    return INSTANCE;
  }

  public void run() {

    // инициализация UIAutomation
    automation = UIAutomation.getInstance();

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
    AtomicBoolean isRunning = new AtomicBoolean(false);
    ProcessHandle.allProcesses()
        .filter(
            ph -> ph.info().command().isPresent() && ph.info().command().get().contains("phoenixbsl"))
        .forEach((process) -> {
          isRunning.set(true);
        });
    return isRunning.get();
  }

  public void startCheckBSL() {

    var moduleText = getModuleText();
    if (moduleText == null ) {
      return;
    }

    var textForCheck = moduleText;
    if (tmpTextSelectedModule.length() > 0) {
      textForCheck = tmpTextSelectedModule;
    }

    var bslServerContext = new ServerContext();
    var documentContext = bslServerContext.addDocument(fakeFile.toURI().toString(), textForCheck);
    var list = diagnosticProvider.computeDiagnostics(documentContext);

    issuesForm.updateIssues(list);
    issuesForm.onVisible();
  }

  public void checkFocusForm() {

    focusElement = null;
    tmpTextSelectedModule = "";
    try {
      focusElement = automation.getFocusedElement();
    } catch (AutomationException e) {
      log.error(e.getStackTrace().toString());
      return;
    }

    var idProcess = 0;

    if (focusElement == null) {
      clearFocusCurrentForm();
    } else {
      try {
        idProcess = focusElement.getProcessId().intValue();
        if (focusElement.getControlType() == UIA_CONTROL_DOCUMENT) {
          tmpTextModule = focusElement.getPropertyValue(UIA_PROPERTY_VALUE).toString();
        }
        else {
          tmpTextModule = null;
        }
      } catch (AutomationException e) {
        log.error(e.getStackTrace().toString());
      }

      if (tmpTextModule != null && tmpTextModule.length() > 0) {
        var robot = new CustomRobot();
        tmpTextSelectedModule = robot.getSelectedText();
        String[] arrStr = tmpTextSelectedModule.split("\n");
        if (arrStr.length < 2) {
          tmpTextSelectedModule = "";
        }
      }

      try {
        var finalIdProcess = idProcess;
        automation.getDesktopWindows().forEach(window -> {
          try {
            if (window.getProcessId().toString().equals(String.valueOf(finalIdProcess))) {
              thisForm = window;
            }
          } catch (AutomationException e) {
            log.error(e.getStackTrace().toString());
          }
        });
      } catch (AutomationException e) {
        log.error(e.getStackTrace().toString());
      }

      try {
        var txt = thisForm.getName();
        var matcher = pattern.matcher(txt);
        if (matcher.find()) {
          thisIdProcess = idProcess;
        } else {
          clearFocusCurrentForm();
        }
      } catch (AutomationException e) {
        clearFocusCurrentForm();
        log.error(e.getStackTrace().toString());
      }
    }

    log.info("This form: " + focusElement);
    log.info("This id process " + thisIdProcess);
  }

  public void formattingTextByBSL() {

    var moduleText = getModuleText();
    if (moduleText == null ) {
      return;
    }

    var textForCheck = moduleText;
    var onlySelected = false;
    if (tmpTextSelectedModule.length() > 0) {
      textForCheck = tmpTextSelectedModule;
      onlySelected = true;
    }

    var bslServerContext = new ServerContext();
    var params = new DocumentFormattingParams();
    params.setTextDocument(getTextDocumentIdentifier(fakeFile));
    params.setOptions(new FormattingOptions(4, false));

    var documentContext = bslServerContext.addDocument(fakeFile.toURI().toString(), textForCheck);
    var textEdits = FormatProvider.getFormatting(params, documentContext);

    var newModuleText = textEdits.get(0).getNewText();

    var customRobot = new CustomRobot();
    customRobot.updateTextOnForm(newModuleText, onlySelected);

  }

  private TextDocumentIdentifier getTextDocumentIdentifier(File file) {
    var uri = file.toURI().toString();
    return new TextDocumentIdentifier(uri);
  }

  private String getModuleText() {
    String moduleText = null;
    try {
      moduleText = getTextDocument();
    } catch (AutomationException e) {
      log.error(e.getStackTrace().toString());
    }
    return moduleText;
  }

  private String getTextDocument() throws AutomationException {

    var module = "";
    if (tmpTextModule != null) {
      module = tmpTextModule;
    } else {
      module = thisForm.getDocument(0).getElement().getPropertyValue(30045).toString();
    }
    return module;
  }

  private void clearFocusCurrentForm() {
    thisForm = null;
    thisIdProcess = 0;
  }

  public void focusDocumentLine(int line) {

    log.info("Line: " + line);
    focusElement.setFocus();
    var customRobot = new CustomRobot();
    customRobot.goToLineOnForm(line);

  }

  public boolean isFindForm() {
    return thisIdProcess != 0;
  }

}

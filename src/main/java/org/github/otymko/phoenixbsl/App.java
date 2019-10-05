package org.github.otymko.phoenixbsl;

import mmarquee.automation.AutomationException;
import mmarquee.automation.Element;
import mmarquee.automation.UIAutomation;
import mmarquee.automation.controls.Window;
import org.eclipse.lsp4j.*;
import org.github._1c_syntax.bsl.languageserver.configuration.LanguageServerConfiguration;
import org.github._1c_syntax.bsl.languageserver.context.DocumentContext;
import org.github._1c_syntax.bsl.languageserver.context.ServerContext;
import org.github._1c_syntax.bsl.languageserver.providers.DiagnosticProvider;
import org.github._1c_syntax.bsl.languageserver.providers.FormatProvider;
import org.github.otymko.phoenixbsl.events.GlobalKeyboardHookHandler;
import org.github.otymko.phoenixbsl.views.IssuesForm;
import org.github.otymko.phoenixbsl.views.Toolbar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
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

  public App() {
  }

  public static App getInstance() {
    return INSTANCE;
  }

  public void run() {

    automation = UIAutomation.getInstance();
    diagnosticProvider = new DiagnosticProvider(LanguageServerConfiguration.create());
    Toolbar toolbar = new Toolbar();
    GlobalKeyboardHookHandler hookHandler = new GlobalKeyboardHookHandler(this);
    log.info("Приложение запущено");

  }

  public void startCheckBSL() {
    String moduleText = getModuleText();
    if (moduleText == null ) {
      return;
    }

    ServerContext bslServerContext = new ServerContext();
    DocumentContext documentContext = bslServerContext.addDocument(fakeFile.toURI().toString(), moduleText);
    List<Diagnostic> list = diagnosticProvider.computeDiagnostics(documentContext);

    IssuesForm form = new IssuesForm(this, list);
  }

  public void checkFocusForm() {

    focusElement = null;
    try {
      focusElement = automation.getFocusedElement();
    } catch (AutomationException e) {
      log.error(e.getStackTrace().toString());
    }

    if (focusElement == null) {
      clearFocusCurrentForm();
    } else {
      int idProcess = 0;
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

      try {
        int finalIdProcess = idProcess;
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
        String txt = thisForm.getName();
        Matcher matcher = pattern.matcher(txt);
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

    String moduleText = getModuleText();
    if (moduleText == null ) {
      return;
    }

    ServerContext bslServerContext = new ServerContext();
    DocumentFormattingParams params = new DocumentFormattingParams();
    params.setTextDocument(getTextDocumentIdentifier(fakeFile));
    params.setOptions(new FormattingOptions(4, true));

    DocumentContext documentContext = bslServerContext.addDocument(fakeFile.toURI().toString(), moduleText);
    List<TextEdit> textEdits = FormatProvider.getFormatting(params, documentContext);

    String newModuleText = textEdits.get(0).getNewText();

    CustomRobot customRobot = new CustomRobot();
    customRobot.updateTextOnForm(newModuleText);

  }

  private TextDocumentIdentifier getTextDocumentIdentifier(File file) {
    String uri = file.toURI().toString();
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

    String module = "";
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
    CustomRobot customRobot = new CustomRobot();
    customRobot.goToLineOnForm(line);

  }

  public boolean isFindForm() {
    return thisIdProcess != 0;
  }

}

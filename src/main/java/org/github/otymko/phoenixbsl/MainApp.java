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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainApp {

  private UIAutomation automation;
  private DiagnosticProvider diagnosticProvider;
  private String path = "D:\\DATA\\Develop\\temp\\test.txt";
  private static final Logger log = LoggerFactory.getLogger(MainApp.class);
  private int thisIdProcess;
  private Window thisForm;
  private Pattern pattern = Pattern.compile("Конфигуратор", Pattern.MULTILINE);

  public void start() {

    automation = UIAutomation.getInstance();

    LanguageServerConfiguration configuration = LanguageServerConfiguration.create();
    diagnosticProvider = new DiagnosticProvider(LanguageServerConfiguration.create());

    GlobalKeyboardHookHandler hookHandler = new GlobalKeyboardHookHandler(this);
    log.info("App is run");

  }

  private void runTaskEventMouse(GlobalMouseHandler handler) {
    FutureTask<String> task = new FutureTask<>(handler);
    Thread localThread = new Thread(task);
    localThread.start();
  }

  public void checkFocusForm() {

    Element focusElement = null;
    try {
      focusElement = automation.getFocusedElement();
    } catch (AutomationException e) {
      e.printStackTrace();
    }

    if (focusElement == null) {
      clearFocusCurrentForm();
    } else {
      int idProcess = 0;
      try {
        idProcess = focusElement.getProcessId().intValue();
      } catch (AutomationException e) {
        e.printStackTrace();
      }

      try {
        int finalIdProcess = idProcess;
        automation.getDesktopWindows().forEach(window -> {

          try {
            if (window.getProcessId().toString().equals(String.valueOf(finalIdProcess))) {
              thisForm = window;
            }
          } catch (AutomationException e) {
            e.printStackTrace();
          }

        });
      } catch (AutomationException e) {
        e.printStackTrace();
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
        e.printStackTrace();
      }
    }

    log.info("This form: " + focusElement);
    log.info("This id process " + thisIdProcess);
  }

  public boolean isFindForm() {
    return thisIdProcess != 0;
  }

  public void startCheckBSL() {

    String moduleText = null;
    try {
      moduleText = getTextDocument();
    } catch (AutomationException e) {
      e.printStackTrace();
    }

    if (moduleText == null ) {
      return;
    }

    ServerContext bslServerContext = new ServerContext();
    File file = new File(path);
    DocumentContext documentContext = bslServerContext.addDocument(file.toURI().toString(), moduleText);
    List<Diagnostic> list = diagnosticProvider.computeDiagnostics(documentContext);

    IssuesForm form = new IssuesForm(this, list);
    form.toFront();
    form.setVisible(true);
    form.setAlwaysOnTop(true);

  }

  public void formatingTextByBSL() {

    String moduleText = null;
    try {
      moduleText = getTextDocument();
    } catch (AutomationException e) {
      e.printStackTrace();
    }

    if (moduleText == null ) {
      return;
    }

    File file = new File(path);

    ServerContext bslServerContext = new ServerContext();

    DocumentFormattingParams params = new DocumentFormattingParams();
    params.setTextDocument(getTextDocumentIdentifier(file));
    params.setOptions(new FormattingOptions(4, true));

    DocumentContext documentContext = bslServerContext.addDocument(file.toURI().toString(), moduleText);
    List<TextEdit> textEdits = FormatProvider.getFormatting(params, documentContext);

    String newText = textEdits.get(0).getNewText();

    Robot robot = null;
    try {

      robot = new Robot();
      robot.keyPress(KeyEvent.VK_CONTROL);
      robot.keyPress(KeyEvent.VK_A);
      robot.delay(100);
      robot.keyRelease(KeyEvent.VK_CONTROL);

      Toolkit.getDefaultToolkit()
          .getSystemClipboard()
          .setContents(
              new StringSelection(newText),
              null
          );

      robot.keyPress(KeyEvent.VK_CONTROL);
      robot.keyPress(KeyEvent.VK_V);
      robot.delay(100);
      robot.keyRelease(KeyEvent.VK_CONTROL);

    } catch (AWTException e) {
      e.printStackTrace();
    }





  }

  private TextDocumentIdentifier getTextDocumentIdentifier(File file) {
    String uri = file.toURI().toString();
    return new TextDocumentIdentifier(uri);
  }

  private String getTextDocument() throws AutomationException {
    return thisForm.getDocument(0).getElement().getPropertyValue(30045).toString();
  }

  private void clearFocusCurrentForm() {
    thisForm = null;
    thisIdProcess = 0;
  }

  public void focusDocumentLine(int line) {

    try {
      thisForm.getDocument(0).getElement().setFocus();

    } catch (AutomationException e) {
      e.printStackTrace();
    }

    List<Integer> listNumber = getListKeyEventByNumber(line);
    log.info("Line: " + line);

    Robot robot = null;
    try {

      robot = new Robot();
      robot.keyPress(KeyEvent.VK_CONTROL);
      robot.keyPress(KeyEvent.VK_G);
      robot.delay(100);
      robot.keyRelease(KeyEvent.VK_CONTROL);
      for(int el : listNumber) {
        robot.keyPress(el);
        robot.delay(70);
      }

      robot.keyPress(KeyEvent.VK_ENTER);

    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  private List<Integer> getListKeyEventByNumber(int inValue) {
    List<Integer> list = new ArrayList<>();

    String str = String.valueOf(inValue);
    for (char symbol : str.toCharArray()) {
      int key;
      switch (String.valueOf(symbol)){
        case ("0"):
          key = KeyEvent.VK_0;
          break;
        case ("1"):
          key = KeyEvent.VK_1;
          break;
        case ("2"):
          key = KeyEvent.VK_2;
          break;
        case ("3"):
          key = KeyEvent.VK_3;
          break;
        case ("4"):
          key = KeyEvent.VK_4;
          break;
        case ("5"):
          key = KeyEvent.VK_5;
          break;
        case ("6"):
          key = KeyEvent.VK_6;
          break;
        case ("7"):
          key = KeyEvent.VK_7;
          break;
        case ("8"):
          key = KeyEvent.VK_8;
          break;
        case ("9"):
          key = KeyEvent.VK_9;
          break;
        default:
          key = 0;
          break;
      }

      if (key != 0){
        list.add((Integer) key);
      }
    }

    return list;
  }


}

package org.github.otymko.phoenixbsl.core;

import com.sun.jna.platform.win32.WinDef;
import org.eclipse.lsp4j.*;
import org.github.otymko.phoenixbsl.events.EventListener;
import org.github.otymko.phoenixbsl.events.EventManager;
import org.github.otymko.phoenixbsl.lsp.BSLHelper;
import org.github.otymko.phoenixbsl.lsp.BSLLanguageServer;
import org.github.otymko.phoenixbsl.views.IssuesForm;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class PhoenixApp implements EventListener {

  private static final String PATH_TO_ICON = "/phoenix.jpg";

  private static final PhoenixApp INSTANCE = new PhoenixApp();
  private static CustomRobot robot = new CustomRobot();
  private EventManager events;

  public static final String FAKE_PATH_FILE = "F:/BSL/fake.bsl";
  private final File fakeFile = new File(FAKE_PATH_FILE);

  private IssuesForm issuesForm;
  private WinDef.HWND focusForm;


  public BSLLanguageServer bslLanguageServer = null;

  public PhoenixApp() {
    events = new EventManager(EventManager.EVENT_INSPECTION, EventManager.EVENT_FORMATTING);
    events.subscribe(EventManager.EVENT_INSPECTION, this);
    events.subscribe(EventManager.EVENT_FORMATTING, this);

    issuesForm = new IssuesForm();
  }

  public static PhoenixApp getInstance() {
    return INSTANCE;
  }

  public EventManager getEventManager() {
    return events;
  }

  private static boolean isWindowsForm1SByClassName(String classNameForm) {
    return classNameForm.contains("V8") || classNameForm.contains("SWT_Window");
  }

  public static boolean isWindowsForm1S() {
    return isWindowsForm1SByClassName(PhoenixUser32.getForegroundWindowClass());
  }

  @Override
  public void formatting() {

    System.out.println("Need formatting!");

    var textForFormatting = "";
    var isSelected = false;
    var textModuleSelected = getTextSelected();
    if (textModuleSelected.length() > 0) {
      textForFormatting = textModuleSelected;
      isSelected = true;
    } else {
      textForFormatting = getTextAll();
      ;
    }

    // DidChange
    BSLHelper.textDocumentDidChange(bslLanguageServer, textForFormatting);

    // Formatting
    var listEdits = textDocumentFormatting();

    try {
      insetTextOnForm(listEdits.get().get(0).getNewText(), isSelected);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }

  }

  private CompletableFuture<List<? extends TextEdit>> textDocumentFormatting() {
    var paramsFormatting = new DocumentFormattingParams();
    var identifier = new TextDocumentIdentifier();
    identifier.setUri(fakeFile.toPath().toAbsolutePath().toUri().toString());
    paramsFormatting.setTextDocument(identifier);
    var options = new FormattingOptions(4, false);
    paramsFormatting.setOptions(options);
    return bslLanguageServer.getTextDocumentService().formatting(paramsFormatting);
  }

  public static void insetTextOnForm(String text, boolean isSelected) {
    if (!isSelected) {
      robot.Ctrl(KeyEvent.VK_A);
    }
    setTextInClipboard(text);
    robot.Ctrl(KeyEvent.VK_V);
  }

  private static void setTextInClipboard(String text) {
    Toolkit.getDefaultToolkit()
      .getSystemClipboard()
      .setContents(
        new StringSelection(text),
        null
      );
  }

  @Override
  public void inspection() {

    if (isWindowsForm1S()) {
      updateFocusForm();
    } else {
      return;
    }

    if (bslLanguageServer == null) {
      return;
    }

    System.out.println("Need inspection!");

    var lineOfset = 0;
    var textForCheck = "";
    var textModuleSelected = getTextSelected();
    if (textModuleSelected.length() > 0) {
      // получем номер строки
      textForCheck = textModuleSelected;
      lineOfset = getCurrentLineNumber();
    } else {
      textForCheck = getTextAll();
    }

    issuesForm.setLineOfset(lineOfset);

    BSLHelper.textDocumentDidChange(bslLanguageServer, textForCheck);
    BSLHelper.textDocumentDidSave(bslLanguageServer);

  }

  public void initToolbar() {
    var popup = new PopupMenu();

    var settingItem = new MenuItem("Настройки");
    popup.add(settingItem);

    var exitItem = new MenuItem("Закрыть");
    exitItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    });
    popup.add(exitItem);

    var systemTray = SystemTray.getSystemTray();
    var icon = new ImageIcon(PhoenixApp.class.getResource(PATH_TO_ICON));
    var image = icon.getImage();

    TrayIcon trayIcon = new TrayIcon(image, "Phoenix BSL", popup);
    trayIcon.setImageAutoSize(true);
    try {
      systemTray.add(trayIcon);
    } catch (AWTException e) {
      System.out.println(e.getMessage());
    }
  }

  public static String getTextSelected() {
    var result = "";
    clearClipboard();
    robot.Ctrl(KeyEvent.VK_X);
    result = getFromClipboard();
    if (result.length() > 0) {
      robot.Ctrl(KeyEvent.VK_Z);
    }
    //log.debug("getTextSelected:" + result);
    return result;
  }

  private static void clearClipboard() {
    StringSelection stringSelection = new StringSelection("");
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
    //log.debug("clearClipboard");
  }

  public static int getCurrentLineNumber() {

    var line = 0;
    robot.Alt(KeyEvent.VK_NUMPAD2);
    var textAll = getTextAll();
    robot.Ctrl(KeyEvent.VK_Z);
    String[] arrStr = textAll.split("\n");
    var count = 0;
    for (var element : arrStr) {
      count++;
      if (element.contains("☻")) { // 9787
        line = count - 1;
        break;
      }
    }
    //log.info("Current line ofset: " + line);
    return line;
  }

  public static String getTextAll() {
    var result = "";
    clearClipboard();
    robot.Ctrl(KeyEvent.VK_A);
    robot.Ctrl(KeyEvent.VK_C);
    result = getFromClipboard();
    //log.debug("getTextAll:" + result);
    return result;
  }

  private static String getFromClipboard() {
    var result = "";
    try {
      result = getDataClipboard();
    } catch (UnsupportedFlavorException e) {
      //log.error(Arrays.toString(e.getStackTrace()));
    } catch (IOException e) {
      //log.error(Arrays.toString(e.getStackTrace()));
    } catch (IllegalAccessException e) {
      //log.error(Arrays.toString(e.getStackTrace()));
    }
    //log.debug("getFromClipboard:", result);
    return result;
  }

  private static String getDataClipboard() throws IllegalAccessException, IOException, UnsupportedFlavorException {

    try {
      Thread.sleep(20);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    String result = (String) Toolkit.getDefaultToolkit()
      .getSystemClipboard().getData(DataFlavor.stringFlavor);

    return result;
  }

  public void gotoLineModule(int line) {
    //log.info("Line: " + line);
    PhoenixUser32.setFocusWindows(focusForm);
    goToLineOnForm(line);
  }

  public void updateFocusForm() {
    focusForm = PhoenixUser32.getHWNDFocusForm();
  }

  public static void goToLineOnForm(int line) {
    var listNumber = getListKeyEventByNumber(line);
    // Окно перейти
    robot.Ctrl(KeyEvent.VK_G);
    // Номер строки
    robot.pressKeyList(listNumber);
    // Подтвержаем ввод
    robot.pressKey(KeyEvent.VK_ENTER);
  }

  private static java.util.List<Integer> getListKeyEventByNumber(int inValue) {
    List<Integer> list = new ArrayList<>();
    var str = String.valueOf(inValue);
    for (char symbol : str.toCharArray()) {
      int key;
      switch (String.valueOf(symbol)) {
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
      list.add(key);
    }
    return list;
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

  public void abort() {
    //log.error("Приложение уже запущено");
    JOptionPane.showMessageDialog(new JFrame(), "Приложение уже запущено. Повторный запуск невозможен.");
    System.exit(0);
  }

  public IssuesForm getIssuesForm() {
    return this.issuesForm;
  }

  public Process startProcessBSLLS() {
    Process processBSL = null;
    Path path = Paths.get(".", "languageserver/bsl-language-server.jar");
    String[] arguments = new String[]{
      "java", "-jar", path.toAbsolutePath().toString()};
    try {
      processBSL = new ProcessBuilder(arguments).start();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return processBSL;
  }

}
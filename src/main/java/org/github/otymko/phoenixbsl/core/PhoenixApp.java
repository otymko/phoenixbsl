package org.github.otymko.phoenixbsl.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.platform.win32.WinDef;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.github.otymko.phoenixbsl.events.EventListener;
import org.github.otymko.phoenixbsl.events.EventManager;
import org.github.otymko.phoenixbsl.lsp.BSLBinding;
import org.github.otymko.phoenixbsl.lsp.BSLConfiguration;
import org.github.otymko.phoenixbsl.lsp.BSLLanguageClient;
import org.github.otymko.phoenixbsl.utils.ProcessHelper;
import org.github.otymko.phoenixbsl.views.Toolbar;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

@Slf4j
@Data
public class PhoenixApp implements EventListener {

  private static final PhoenixApp INSTANCE = new PhoenixApp();

  private static final Path pathToFolderLog = createPathToLog();
  private static final Path pathToConfiguration = createPathToConfiguration();
  private static final Path pathToBSLConfiguration =
    Path.of(System.getProperty("user.home"), "phoenixbsl", ".bsl-language-server.json");
  public static final URI fakeUri = Path.of("fake.bsl").toUri();
  public static final List<String> diagnosticListForQuickFix = createDiagnosticListForQuickFix();

  private EventManager events;
  private WinDef.HWND focusForm;
  private Process processBSL;
  private BSLBinding bslBinding = null;

  private ConfigurationApp configuration;

  private List<Diagnostic> diagnosticList = new ArrayList<>();

  public int currentOffset = 0;

  private PhoenixApp() {

    events = new EventManager(
      EventManager.EVENT_INSPECTION,
      EventManager.EVENT_FORMATTING,
      EventManager.EVENT_FIX_ALL,
      EventManager.EVENT_UPDATE_ISSUES,
      EventManager.SHOW_ISSUE_STAGE,
      EventManager.SHOW_SETTING_STAGE);
    events.subscribe(EventManager.EVENT_INSPECTION, this);
    events.subscribe(EventManager.EVENT_FORMATTING, this);
    events.subscribe(EventManager.EVENT_FIX_ALL, this);

    configuration = new ConfigurationApp();


  }

  public static PhoenixApp getInstance() {
    return INSTANCE;
  }

  public void initProcessBSL() {
    createProcessBSLLS();
    if (processBSL != null) {
      connectToBSLLSProcess();
    }
  }


  // EventListener
  //

  @Override
  public void inspection() {

    LOGGER.debug("Событие: анализ кода");

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

    LOGGER.debug("Событие: форматирование");

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

  @Override
  public void fixAll() {

    LOGGER.debug("Событие: обработка квикфиксов");

    if (!(processBSLIsRunning() && PhoenixAPI.isWindowsForm1S())) {
      return;
    }

    var separator = "\n";
    var textForQF = PhoenixAPI.getTextAll();

    // найдем все диагностики подсказки
    var listQF = diagnosticList.stream()
      .filter(this::isAcceptDiagnosticForQuickFix)
      //.filter(diagnostic -> diagnostic.getCode().equalsIgnoreCase("CanonicalSpellingKeywords"))
      .collect(Collectors.toList());

    List<Either<Command, CodeAction>> codeActions = new ArrayList<>();
    try {
      codeActions = bslBinding.textDocumentCodeAction(fakeUri, listQF);
    } catch (ExecutionException | InterruptedException e) {
      LOGGER.error(e.getMessage());

    }
    LOGGER.debug("Квикфиксов найдено: " + codeActions);
    String[] strings = textForQF.split(separator);

    codeActions.forEach(diagnostic -> {
      CodeAction codeAction = diagnostic.getRight();
      if (codeAction.getTitle().startsWith("Fix all:")) {
        return;
      }
      codeAction.getEdit().getChanges().forEach((s, textEdits) -> {
        textEdits.forEach(textEdit -> {
          var range = textEdit.getRange();
          var currentLine = range.getStart().getLine();
          var newText = textEdit.getNewText();
          var currentString = strings[currentLine];
          var newString =
            currentString.substring(0, range.getStart().getCharacter())
              + newText
              + currentString.substring(range.getEnd().getCharacter());
          strings[currentLine] = newString;
        });
      });
    });

    if (!codeActions.isEmpty()) {
      var text = String.join(separator, strings);
      PhoenixAPI.insetTextOnForm(text, false);
    }

  }


  public void createProcessBSLLS() {

    processBSL = null;

    var pathToBSLLS = Path.of(configuration.getPathToBSLLS()).toAbsolutePath();
    if (!pathToBSLLS.toFile().exists()) {
      LOGGER.error("Не найден BSL LS");
      return;
    }

    var arguments = ProcessHelper.getArgumentsRunProcessBSLLS(configuration);

    if (pathToBSLLS.toFile().exists()) {
      arguments.add("--configuration");
      arguments.add(pathToBSLLS.toString());
    }

    LOGGER.debug("Строка запуска BSL LS {}", String.join(" ", arguments));

    try {
      processBSL = new ProcessBuilder()
        .command(arguments.toArray(new String[0]))
        .start();
      sleepCurrentThread(500);
      if (!processBSL.isAlive()) {
        processBSL = null;
        LOGGER.error("Не удалалось запустить процесс с BSL LS. Процесс был аварийно завершен.");
      }
    } catch (IOException e) {
      LOGGER.error("Не удалалось запустить процесс с BSL LS", e);
    }

  }

  public void connectToBSLLSProcess() {

    BSLLanguageClient bslClient = new BSLLanguageClient();
    BSLBinding bslBinding = new BSLBinding(
      bslClient,
      getProcessBSL().getInputStream(),
      getProcessBSL().getOutputStream());
    bslBinding.startInThread();

    sleepCurrentThread(2000);

    setBslBinding(bslBinding);

    // инициализация
    bslBinding.initialize();

    // откроем фейковый документ
    bslBinding.textDocumentDidOpen(getFakeUri(), "");

  }

  public void sleepCurrentThread(long value) {
    try {
      Thread.sleep(value);
    } catch (Exception e) {
      LOGGER.warn("Не удалось сделать паузу в текущем поток", e);
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

  public void showIssuesStage() {
    events.notify(EventManager.SHOW_ISSUE_STAGE);
  }

  public String getVersionApp() {
    // взято из com/github/_1c_syntax/bsl/languageserver/cli/VersionCommand.java
    final var mfStream = Thread.currentThread()
      .getContextClassLoader()
      .getResourceAsStream("META-INF/MANIFEST.MF");

    var manifest = new Manifest();
    try {
      manifest.read(mfStream);
    } catch (IOException e) {
      LOGGER.error("Не удалось прочитать манифест проекта", e);
    }

    var version = "dev";
    if (manifest.getMainAttributes().get(Attributes.Name.MAIN_CLASS) == null) {
      return version;
    }
    version = manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
    return version;

  }

  public void showSettingStage() {

    events.notify(EventManager.SHOW_SETTING_STAGE);

  }

  public Path getPathToLogs() {
    return pathToFolderLog.toAbsolutePath();
  }

  public void initConfiguration() {
    // файл конфигурации должен лежать по пути: app/configuration.json
    var fileConfiguration = pathToConfiguration.toFile();
    if (!fileConfiguration.exists()) {
      // создать новый по умолчанию
      configuration = new ConfigurationApp();
      writeConfiguration(configuration, fileConfiguration);
    } else {
      // прочитать в текущие настройки
      configuration = ConfigurationApp.create(fileConfiguration);
    }

  }

  public void writeConfiguration(ConfigurationApp configurationApp, File fileConfiguration) {
    // запишем ее в файл
    ObjectMapper mapper = new ObjectMapper();
    try {
      mapper.writeValue(fileConfiguration, configurationApp);
    } catch (IOException e) {
      LOGGER.error("Не удалось записать конфигурацию в файл.", e);
    }
  }

  public void writeConfiguration(ConfigurationApp configurationApp) {
    writeConfiguration(configurationApp, pathToConfiguration.toFile());
  }

  @SneakyThrows
  public String getVersionBSLLS() {
    var result = "<Неопределено>";
    var arguments = ProcessHelper.getArgumentsRunProcessBSLLS(configuration);
    arguments.add("--version");
    var processBSL = new ProcessBuilder().command(arguments.toArray(new String[0])).start();
    var out = ProcessHelper.getStdoutProcess(processBSL);
    if (out.startsWith("version")) {
      result = out.replaceAll("version: ", "");
    }
    return result;
  }

  public void initBSLConfiguration() {
    createBSLConfigurationFile();
  }

  public void createBSLConfigurationFile() {
    var bslConfiguration = new BSLConfiguration();
    bslConfiguration.setDiagnosticLanguage("ru");
    bslConfiguration.setShowCognitiveComplexityCodeLens(false);
    bslConfiguration.setShowCyclomaticComplexityCodeLens(false);
    bslConfiguration.setComputeDiagnosticsTrigger("onSave");
    bslConfiguration.setComputeDiagnosticsSkipSupport("withSupportLocked");
    bslConfiguration.setConfigurationRoot("src");

    pathToBSLConfiguration.getParent().toFile().mkdirs();

    ObjectMapper mapper = new ObjectMapper();
    try {
      mapper.writeValue(pathToBSLConfiguration.toFile(), bslConfiguration);
    } catch (IOException e) {
      LOGGER.error("Не удалось записать файл конфигурации BSL LS", e);
    }

  }

  private static Path createPathToConfiguration() {
    var path = Path.of(System.getProperty("user.home"), "phoenixbsl", "Configuration.json").toAbsolutePath();
    path.getParent().toFile().mkdirs();
    return path;
  }

  private static Path createPathToLog() {
    var path = Path.of(System.getProperty("user.home"), "phoenixbsl", "logs").toAbsolutePath();
    path.toFile().mkdirs();
    return path;
  }

  private static List<String> createDiagnosticListForQuickFix() {
    var list = new ArrayList<String>();
    list.add("CanonicalSpellingKeywords");
    list.add("SpaceAtStartComment");
    list.add("SemicolonPresence");
    return list;
  }

  private boolean isAcceptDiagnosticForQuickFix(Diagnostic diagnostic) {
    return diagnosticListForQuickFix.contains(diagnostic.getCode());
  }


}

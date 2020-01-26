package org.github.otymko.phoenixbsl.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.platform.win32.WinDef;
import lombok.extern.slf4j.Slf4j;
import org.github.otymko.phoenixbsl.events.EventListener;
import org.github.otymko.phoenixbsl.events.EventManager;
import org.github.otymko.phoenixbsl.lsp.BSLBinding;
import org.github.otymko.phoenixbsl.lsp.BSLLanguageClient;
import org.github.otymko.phoenixbsl.views.Toolbar;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

@Slf4j
public class PhoenixApp implements EventListener {

  private static final PhoenixApp INSTANCE = new PhoenixApp();

  private static final Path pathToFolderLog = Path.of("app", "logs");
  // TODO: лучше файл настроек хранить не в каталоге с app, а в пользовательском каталоге
  // при переустановке app тогда настройки сохраняться, с другой стороны - может сменится структура настроек
  private static final Path pathToConfiguration = Path.of("app", "Configuration.json").toAbsolutePath();

  public static final URI fakeUri = new File("C:/BSL/fake.bsl").toPath().toAbsolutePath().toUri();

  private EventManager events;
  private WinDef.HWND focusForm;
  private Process processBSL;
  private BSLBinding bslBinding = null;

  private ConfigurationApp configuration;


  public int currentOffset = 0;

  private PhoenixApp() {

    events = new EventManager(
      EventManager.EVENT_INSPECTION,
      EventManager.EVENT_FORMATTING,
      EventManager.EVENT_UPDATE_ISSUES,
      EventManager.SHOW_ISSUE_STAGE,
      EventManager.SHOW_SETTING_STAGE);
    events.subscribe(EventManager.EVENT_INSPECTION, this);
    events.subscribe(EventManager.EVENT_FORMATTING, this);

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

  public void createProcessBSLLS() {
    processBSL = null;
    Collection<String> arguments = new ArrayList<>();
    ConfigurationApp configurationApp = PhoenixApp.getInstance().configuration;
    if (configurationApp.isUsePathToJarBSLLS()) {
      arguments.add(configurationApp.getPathToJava());
      arguments.add("-jar");
    }
    var pathToBSLLS = Path.of(configurationApp.getPathToBSLLS()).toAbsolutePath();
    if (!pathToBSLLS.toFile().exists()) {
      LOGGER.error("Не найден BSL LS");
      return;
    }

    arguments.add(pathToBSLLS.toString());

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
      Thread.currentThread().sleep(value);
    } catch (InterruptedException e) {
      LOGGER.error("Не удалось сделать паузу в текущем поток", e);
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
    if (manifest.getMainAttributes().get(Attributes.Name.MAIN_CLASS) == null){
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

  public ConfigurationApp getConfiguration() {
    return configuration;
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


}
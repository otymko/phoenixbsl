package com.github.otymko.phoenixbsl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.otymko.phoenixbsl.gui.Toolbar;
import com.github.otymko.phoenixbsl.logic.GlobalKeyListenerThread;
import com.github.otymko.phoenixbsl.logic.PhoenixAPI;
import com.github.otymko.phoenixbsl.logic.PhoenixUser32;
import com.github.otymko.phoenixbsl.logic.event.EventListener;
import com.github.otymko.phoenixbsl.logic.event.EventManager;
import com.github.otymko.phoenixbsl.logic.service.LSService;
import com.github.otymko.phoenixbsl.model.Configuration;
import com.github.otymko.phoenixbsl.model.ProjectSetting;
import com.sun.jna.platform.win32.WinDef;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.lsp4j.Diagnostic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

@Slf4j
@Data
public class PhoenixCore implements EventListener {
  private static final String APPLICATION_NAME = "phoenixbsl";
  private static final PhoenixCore INSTANCE = new PhoenixCore();

  private LSService lsService;
  private final List<String> diagnosticListForQuickFix = createDiagnosticListForQuickFix();
  private List<Diagnostic> diagnosticList = new ArrayList<>();
  public int currentOffset = 0;

  private final Path basePathApp = Path.of(System.getProperty("user.home"), APPLICATION_NAME);
  private final Path pathToFolderLog = createPathToLog();
  private final Path pathToConfiguration = createPathToConfiguration();
  private final Path pathToBSLConfigurationDefault =
    Path.of(basePathApp.toString(), ".bsl-language-server.json");

  private EventManager events;
  private WinDef.HWND focusForm;

  private Configuration configuration;

  private ProjectSetting projectSetting;

  private PhoenixCore() {
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

    configuration = Configuration.create();
  }

  public static PhoenixCore getInstance() {
    return INSTANCE;
  }

  public void initProcessBSL() {
    lsService = new LSService(this);
    lsService.start();
  }

  public void restartProcessBSLLS() {
    lsService.restart();
  }

  public void initEmptyProject() {
    final String projectName = "project";
    projectSetting = new ProjectSetting();
    projectSetting.setProjectKey(projectName);

    // создаем каталог project
    var pathToProjects = Path.of(basePathApp.toString(), "projects");
    pathToProjects.toFile().mkdir();

    var emptyProject = Path.of(pathToProjects.toString(), projectName);
    emptyProject.toFile().mkdir();
    projectSetting.setBasePath(emptyProject);

    var fakePath = Path.of(emptyProject.toString(), "Module.bsl");
    projectSetting.setFakePath(fakePath);
  }

  // EventListener
  //

  @Override
  public void inspection() {
    LOGGER.debug("Событие: анализ кода");

    if (PhoenixAPI.isWindowsForm1S()) {
      updateFocusForm();
    } else {
      return;
    }

    if (lsService.isAlive()) {
      lsService.validate();
    }
  }

  @Override
  public void formatting() {
    LOGGER.debug("Событие: форматирование");

    if (!PhoenixAPI.isWindowsForm1S()) {
      return;
    }

    if (lsService.isAlive()) {
      lsService.formatting();
    }
  }

  @Override
  public void fixAll() {
    LOGGER.debug("Событие: обработка квикфиксов");

    if (!PhoenixAPI.isWindowsForm1S()) {
      return;
    }

    if (lsService.isAlive()) {
      lsService.fixAll();
    }
  }

  public void sleepCurrentThread(long value) {
    try {
      Thread.sleep(value);
    } catch (Exception e) {
      LOGGER.warn("Не удалось сделать паузу в текущем поток", e);
    }
  }

  public void initToolbar() {
    new Toolbar();
  }

  public boolean appIsRunning() {
    var thisPid = ProcessHandle.current().pid();
    var isRunning = new AtomicBoolean(false);
    ProcessHandle.allProcesses()
      .filter(
        ph -> ph.info().command().isPresent()
          && ph.info().command().get().contains(APPLICATION_NAME)
          && ph.pid() != thisPid)
      .findAny()
      .ifPresent(processHandle -> isRunning.set(true));
    return isRunning.get();
  }

  public void abort() {
    PhoenixAPI.showMessageDialog("Приложение уже запущено. Повторный запуск невозможен.");
    System.exit(0);
  }

  public EventManager getEventManager() {
    return events;
  }

  public void stopBSL() {
    lsService.stop();
  }

  private void updateFocusForm() {
    focusForm = PhoenixUser32.getHWNDFocusForm();
  }

  public WinDef.HWND getFocusForm() {
    return focusForm;
  }

  public URI getFakeUri() {
    return projectSetting.getFakePath().toUri();
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

  public void initializeConfiguration() {
    // файл конфигурации должен лежать по пути: app/configuration.json
    var fileConfiguration = pathToConfiguration.toFile();
    if (!fileConfiguration.exists()) {
      // создать новый по умолчанию
      configuration = Configuration.create();
      writeConfiguration(configuration, fileConfiguration);
    } else {
      // прочитать в текущие настройки
      configuration = Configuration.create(fileConfiguration);
    }

  }

  public void writeConfiguration(Configuration configuration, File fileConfiguration) {
    // запишем ее в файл
    ObjectMapper mapper = new ObjectMapper();
    try {
      mapper.writeValue(fileConfiguration, configuration);
    } catch (IOException e) {
      LOGGER.error("Не удалось записать конфигурацию в файл.", e);
    }
  }

  public void writeConfiguration(Configuration configuration) {
    writeConfiguration(configuration, pathToConfiguration.toFile());
  }

  public String getVersionBSLLS() {
    return lsService.getVersion();
  }

  private static Path createPathToConfiguration() {
    var path = Path.of(System.getProperty("user.home"), APPLICATION_NAME, "Configuration.json")
      .toAbsolutePath();
    path.getParent().toFile().mkdirs();
    return path;
  }

  private Path createPathToLog() {
    var path = Path.of(basePathApp.toString(), "logs").toAbsolutePath();
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

  public void initializeGlobalKeyListener() {
    new GlobalKeyListenerThread().start();
  }

  @SneakyThrows
  public void updateContentFile(Path path, String content) {
    var fooStream = new FileOutputStream(path.toFile(), false);
    fooStream.write(content.getBytes(StandardCharsets.UTF_8));
    fooStream.close();
  }

}

package com.github.otymko.phoenixbsl;

import com.github.otymko.phoenixbsl.gui.Toolbar;
import com.github.otymko.phoenixbsl.logic.GlobalKeyListenerThread;
import com.github.otymko.phoenixbsl.logic.PhoenixAPI;
import com.github.otymko.phoenixbsl.logic.PhoenixContext;
import com.github.otymko.phoenixbsl.logic.designer.DesignerTextEditor;
import com.github.otymko.phoenixbsl.logic.event.EventListener;
import com.github.otymko.phoenixbsl.logic.event.EventManager;
import com.github.otymko.phoenixbsl.logic.service.LSService;
import com.github.otymko.phoenixbsl.model.Configuration;
import com.github.otymko.phoenixbsl.model.ProjectSetting;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

@Slf4j
@Data
public class PhoenixCore implements EventListener {
  public static final String APPLICATION_NAME = "phoenixbsl";
  private static final PhoenixCore INSTANCE = new PhoenixCore();
  private PhoenixContext context;
  private LSService lsService;
  private DesignerTextEditor textEditor;
  private Configuration configuration;
  private ProjectSetting project;
  private final EventManager eventManager;

  private PhoenixCore() {
    eventManager = new EventManager(
      EventManager.EVENT_INSPECTION,
      EventManager.EVENT_FORMATTING,
      EventManager.EVENT_FIX_ALL,
      EventManager.EVENT_UPDATE_ISSUES,
      EventManager.SHOW_ISSUE_STAGE,
      EventManager.SHOW_SETTING_STAGE);

    configuration = Configuration.create();
  }

  public static PhoenixCore getInstance() {
    return INSTANCE;
  }

  public void initialize() {
    initContext();
    initConfiguration(); // инициализируем настроек
    initProjects();
    initToolbar(); // запустим трей
    initTextEditor();
    initGlobalKeyListener(); // подключаем слушаеть нажатий
    initProcessBSL(); // запустим bsl ls
  }

  @Override
  public void showSettingStage() {
    eventManager.notify(EventManager.SHOW_SETTING_STAGE);
  }

  @Override
  public void showIssuesStage() {
    eventManager.notify(EventManager.SHOW_ISSUE_STAGE);
  }

  public void sleepCurrentThread(long value) {
    try {
      Thread.sleep(value);
    } catch (Exception e) {
      LOGGER.warn("Не удалось сделать паузу в текущем поток", e);
    }
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

  public boolean appIsRunning() {
    var currentProcess = ProcessHandle.current();
    var thisPid = currentProcess.pid();

    // исключаем, если это запуск jar
    var optionalCommand = currentProcess.info().command();
    if (optionalCommand.isPresent() && optionalCommand.get().equals("java.exe")) {
      return false;
    }

    var thisUser = currentProcess.info().user().orElse("");
    var isRunning = new AtomicBoolean(false);
    ProcessHandle.allProcesses()
      .filter(
        ph -> ph.info().command().isPresent()
          && ph.info().command().get().contains(PhoenixCore.APPLICATION_NAME)
          && !ph.info().user().orElse("").equals(thisUser)
          && ph.pid() != thisPid)
      .findAny()
      .ifPresent(processHandle -> isRunning.set(true));
    return isRunning.get();
  }

  public void abort() {
    PhoenixAPI.showMessageDialog("Приложение уже запущено. Повторный запуск невозможен.");
    System.exit(0);
  }

  public String getVersionBSLLS() {
    return lsService.getVersion();
  }

  public void restartBSLLS() {
    lsService.restart();
  }

  public void stopBSLLS() {
    lsService.stop();
  }

  public URI getFakeUri() {
    return project.getFakePath().toUri();
  }

  public Path getPathToLogs() {
    return context.getPathToFolderLog().toAbsolutePath();
  }

  public void updateContent(Path path, String content) {
    textEditor.setPathToFile(path);
    textEditor.setContent(content);
    textEditor.saveContent();
  }

  public void updateProject(ProjectSetting project) {
    this.project = project;
    initProject();
  }

  private void initContext() {
    context = new PhoenixContext(this);
  }

  private void initConfiguration() {
    getContext().initConfiguration();
  }

  private void initProcessBSL() {
    lsService = new LSService(this);
    lsService.start();
  }

  private void initProjects() {
    // создаем каталог project
    var pathToProjects = Path.of(context.getBasePathApp().toString(), "projects");
    if (!pathToProjects.toFile().exists()) {
      pathToProjects.toFile().mkdir();
    }

    if (!configuration.getProjects().isEmpty()) {
      var projectName = context.getProjectName();
      configuration.getProjects().stream()
        .filter(item -> item.getName().equalsIgnoreCase(projectName))
        .findAny()
        .ifPresent(this::setProject);
    }

    if (project == null) {
      initEmptyProject(pathToProjects);
      configuration.getProjects().add(project);
    }

    initProject();
  }

  private void initEmptyProject(Path pathToProjects) {
    final String projectKey = "project";
    project = new ProjectSetting();
    project.setName(PhoenixContext.DEFAULT_PROJECT_NAME);
    project.setProjectKey(projectKey);

    var emptyProject = Path.of(pathToProjects.toString(), projectKey);
    project.setBasePath(emptyProject);
  }

  private void initProject() {
    if (!project.getBasePath().toFile().exists()) {
      project.getBasePath().toFile().mkdirs();
    }
  }

  private void initTextEditor() {
    textEditor = new DesignerTextEditor(this);
  }

  private void initToolbar() {
    new Toolbar();
  }

  private void initGlobalKeyListener() {
    new GlobalKeyListenerThread().start();
  }

}

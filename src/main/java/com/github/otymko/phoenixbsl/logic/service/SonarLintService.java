package com.github.otymko.phoenixbsl.logic.service;

import com.github.otymko.phoenixbsl.PhoenixCore;
import com.github.otymko.phoenixbsl.logic.PhoenixAPI;
import com.github.otymko.phoenixbsl.logic.event.EventManager;
import com.github.otymko.phoenixbsl.logic.sonarlint.CustomLogOutput;
import com.github.otymko.phoenixbsl.logic.sonarlint.DefaultClientInputFile;
import com.github.otymko.phoenixbsl.logic.sonarlint.SonarLintHelper;
import com.github.otymko.phoenixbsl.logic.sonarlint.StoreIssueListener;
import com.github.otymko.phoenixbsl.model.ProjectSetting;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.sonarsource.sonarlint.core.ConnectedSonarLintEngineImpl;
import org.sonarsource.sonarlint.core.client.api.common.Language;
import org.sonarsource.sonarlint.core.client.api.common.LogOutput;
import org.sonarsource.sonarlint.core.client.api.connected.ConnectedAnalysisConfiguration;
import org.sonarsource.sonarlint.core.client.api.connected.ConnectedGlobalConfiguration;
import org.sonarsource.sonarlint.core.client.api.connected.ServerConfiguration;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
public class SonarLintService implements Service {
  public static final String SOURCE = "sonarlint";
  private static final String SERVER_ID = SOURCE;

  @Setter
  private ProjectSetting project;

  private final ConnectedGlobalConfiguration configuration;
  private ConnectedSonarLintEngineImpl connection;

  private final LogOutput logOutput = new CustomLogOutput();

  public SonarLintService() {
    configuration = ConnectedGlobalConfiguration.builder()
      .setLogOutput(logOutput)
      .addEnabledLanguage(Language.BSL)
      .setServerId(SERVER_ID).build();
  }

  @Override
  public void start() {
    var serverConfiguration = ServerConfiguration.builder()
      .token(project.getToken())
      .url(project.getServerUrl())
      .userAgent(PhoenixCore.APPLICATION_NAME)
      .build();

    connection = new ConnectedSonarLintEngineImpl(configuration);
    try {
      connection.update(serverConfiguration, null);
      connection.updateProject(serverConfiguration, project.getProjectKey(), null);
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      PhoenixAPI.showMessageDialog("Не удалось подключить SonarLint, по причине: " + e.getMessage());
      connection = null;
      return;
    }
    connection.start();
  }

  @Override
  public void stop() {
    if (connection != null) {
      connection.stop(false);
      connection = null;
    }
  }

  @Override
  public void restart() {
    stop();
    start();
  }

  public boolean isAlive() {
    return connection != null;
  }

  public void validate(Path path, String content) {
    var inputFile = getInputFile(path);

    var analysisConfiguration = ConnectedAnalysisConfiguration.builder()
      .setBaseDir(PhoenixCore.getInstance().getProject().getBasePath())
      .addInputFile(inputFile)
      .setProjectKey(project.getProjectKey())
      .build();

    var issueListener = new StoreIssueListener(new ArrayList<>());
    connection.analyze(analysisConfiguration, issueListener, logOutput, null);

    var diagnostics = issueListener.getIssues()
      .stream()
      .filter(issue -> issue.getTextRange() != null)
      .map(SonarLintHelper::newDiagnosticByIssue)
      .collect(Collectors.toList());

    var core = PhoenixCore.getInstance();

    var diagnosticList = core.getTextEditor().getDiagnostics();
    PhoenixAPI.clearListBySource(diagnosticList, SOURCE);
    diagnosticList.addAll(diagnostics);

    core.getEventManager().notify(
      EventManager.EVENT_UPDATE_ISSUES,
      diagnosticList
    );
  }

  private static DefaultClientInputFile getInputFile(Path path) {
    return new DefaultClientInputFile(path.toString(), path.toString(), false,
      Charset.defaultCharset(), path.toUri());
  }


}

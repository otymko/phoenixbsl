package com.github.otymko.phoenixbsl.logic.service;

import com.github.otymko.phoenixbsl.PhoenixCore;
import com.github.otymko.phoenixbsl.logic.PhoenixAPI;
import com.github.otymko.phoenixbsl.logic.event.EventManager;
import com.github.otymko.phoenixbsl.logic.sonarlint.CustomLogOutput;
import com.github.otymko.phoenixbsl.logic.sonarlint.DefaultClientInputFile;
import com.github.otymko.phoenixbsl.logic.sonarlint.StoreIssueListener;
import com.github.otymko.phoenixbsl.model.ProjectSetting;
import lombok.Setter;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.sonarsource.sonarlint.core.ConnectedSonarLintEngineImpl;
import org.sonarsource.sonarlint.core.client.api.common.Language;
import org.sonarsource.sonarlint.core.client.api.common.LogOutput;
import org.sonarsource.sonarlint.core.client.api.connected.ConnectedAnalysisConfiguration;
import org.sonarsource.sonarlint.core.client.api.connected.ConnectedGlobalConfiguration;
import org.sonarsource.sonarlint.core.client.api.connected.ServerConfiguration;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SonarLintService implements Service {
  private static final String SERVER_ID = "BF41A1F2-AXXb5ffO74B20IfqK24x";
  private static final String SOURCE = "sonarlint";
  private static final Map<String, DiagnosticSeverity> STRING_TO_SEVERITY_MAP = createStringToSeverityMap();

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
    ServerConfiguration serverConfiguration = ServerConfiguration.builder()
      .token(project.getToken())
      .url(project.getServerUrl())
      .userAgent(PhoenixCore.APPLICATION_NAME)
      .build();

    connection = new ConnectedSonarLintEngineImpl(configuration);
    connection.update(serverConfiguration, null);
    connection.updateProject(serverConfiguration, project.getProjectKey(), null);
    connection.start();
  }

  @Override
  public void stop() {
    connection.stop(false);
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

    StoreIssueListener issueListener = new StoreIssueListener(new ArrayList<>());
    connection.analyze(analysisConfiguration, issueListener, logOutput, null);

    var diagnostics = issueListener.getIssues()
      .stream()
      .filter(issue -> issue.getTextRange() != null)
      .map(issue -> {
        var line = issue.getStartLine() == null ? 1 : issue.getStartLine();
        var position = new Position(line - 1, 0);
        var diagnostic = new Diagnostic();
        diagnostic.setSource(SOURCE);
        diagnostic.setSeverity(STRING_TO_SEVERITY_MAP.getOrDefault(issue.getType(), DiagnosticSeverity.Information));
        diagnostic.setMessage("[SonarLint]: " + issue.getMessage());
        diagnostic.setCode(issue.getRuleKey());
        diagnostic.setRange(new Range(position, position));
        return diagnostic;
      })
      .collect(Collectors.toList());

    var core = PhoenixCore.getInstance();

    var diagnosticList = core.getTextEditor().getDiagnostics();
    PhoenixAPI.clearListBySource(diagnosticList, SOURCE);
    diagnosticList.addAll(diagnostics);

    PhoenixCore.getInstance().getEventManager().notify(
      EventManager.EVENT_UPDATE_ISSUES,
      diagnosticList
    );
  }

  private static DefaultClientInputFile getInputFile(Path path) {
    return new DefaultClientInputFile(path.toString(), path.toString(), false,
      Charset.defaultCharset(), path.toUri());
  }

  private static Map<String, DiagnosticSeverity> createStringToSeverityMap() {
    Map<String, DiagnosticSeverity> map = new HashMap<>();
    map.put("BUG", DiagnosticSeverity.Error);
    map.put("CODE_SMELL", DiagnosticSeverity.Information);
    map.put("VULNERABILITY", DiagnosticSeverity.Error);
    map.put("SECURITY_HOTSPOT", DiagnosticSeverity.Error);
    return map;
  }
}

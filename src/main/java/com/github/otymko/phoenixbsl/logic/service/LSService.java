package com.github.otymko.phoenixbsl.logic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.otymko.phoenixbsl.PhoenixCore;
import com.github.otymko.phoenixbsl.logic.PhoenixAPI;
import com.github.otymko.phoenixbsl.logic.designer.DesignerTextEditor;
import com.github.otymko.phoenixbsl.logic.lsp.BSLBinding;
import com.github.otymko.phoenixbsl.logic.lsp.BSLConfiguration;
import com.github.otymko.phoenixbsl.logic.lsp.BSLLanguageClient;
import com.github.otymko.phoenixbsl.logic.utils.ProcessHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
public class LSService implements Service {
  private final PhoenixCore core;
  private Process process;
  private BSLBinding binding;

  public LSService(PhoenixCore core) {
    this.core = core;
  }

  @Override
  public void start() {
    createProcessBSLLS();
    if (process != null) {
      connectToBSLLSProcess();
    }
  }

  @Override
  public void stop() {
    if (binding != null) {
      binding.shutdown();
      binding.exit();
    }
    if (process != null) {
      process.destroy();
    }
  }

  @Override
  public void restart() {
    stop();
    start();
  }

  public String getVersion() {
    return getLSVersion();
  }

  private void createProcessBSLLS() {
    process = null;

    var configuration = core.getConfiguration();
    var pathToBSLLS = Path.of(configuration.getPathToBSLLS()).toAbsolutePath();
    if (!pathToBSLLS.toFile().exists()) {
      LOGGER.error("Не найден BSL LS");
      return;
    }

    var arguments = ProcessHelper.getArgumentsRunProcessBSLLS(configuration);

    Path pathToBSLConfiguration;
    if (configuration.isUseCustomBSLLSConfiguration()) {
      Path path;
      try {
        path = Path.of(core.getContext().getBasePathApp().toString(), configuration.getPathToBSLLSConfiguration());
      } catch (InvalidPathException exp) {
        path = null;
      }
      if (path != null && path.toFile().exists()) {
        pathToBSLConfiguration = path;
      } else {
        pathToBSLConfiguration = Path.of(configuration.getPathToBSLLSConfiguration()).toAbsolutePath();
      }

    } else {
      initBSLConfiguration();
      pathToBSLConfiguration = core.getContext().getPathToBSLConfigurationDefault();
    }

    if (pathToBSLConfiguration.toFile().exists()) {
      arguments.add("--configuration");
      arguments.add(pathToBSLConfiguration.toString());
    }

    LOGGER.debug("Строка запуска BSL LS {}", String.join(" ", arguments));

    try {
      process = new ProcessBuilder()
        .command(arguments.toArray(new String[0]))
        .start();
      core.sleepCurrentThread(500);
      if (!process.isAlive()) {
        process = null;
        LOGGER.error("Не удалалось запустить процесс с BSL LS. Процесс был аварийно завершен.");
      }
    } catch (IOException e) {
      LOGGER.error("Не удалалось запустить процесс с BSL LS", e);
    }
  }

  private void connectToBSLLSProcess() {

    BSLLanguageClient bslClient = new BSLLanguageClient();
    binding = new BSLBinding(
      bslClient,
      process.getInputStream(),
      process.getOutputStream());
    binding.startInThread();

    core.sleepCurrentThread(2000);

    // инициализация
    binding.initialize();

    // откроем фейковый документ
    core.updateContent(core.getProjectSetting().getFakePath(), "");
    binding.textDocumentDidOpen(core.getFakeUri(), "");

  }

  public void initBSLConfiguration() {
    createBSLConfigurationFile();
  }

  public void createBSLConfigurationFile() {
    var bslConfiguration = new BSLConfiguration();
    bslConfiguration.setLanguage("ru");
    var codeLens = new BSLConfiguration.CodeLensOptions();
    codeLens.setShowCognitiveComplexity(false);
    codeLens.setShowCyclomaticComplexity(false);
    bslConfiguration.setCodeLens(codeLens);
    var diagnosticsOptions = new BSLConfiguration.DiagnosticsOptions();
    diagnosticsOptions.setComputeTrigger("onSave");
    diagnosticsOptions.setSkipSupport("never");
    bslConfiguration.setDiagnostics(diagnosticsOptions);
    bslConfiguration.setConfigurationRoot("src");

    core.getContext().getPathToBSLConfigurationDefault().getParent().toFile().mkdirs();

    ObjectMapper mapper = new ObjectMapper();
    try {
      mapper.writeValue(core.getContext().getPathToBSLConfigurationDefault().toFile(), bslConfiguration);
    } catch (IOException e) {
      LOGGER.error("Не удалось записать файл конфигурации BSL LS", e);
    }

  }

  public boolean isAlive() {
    return process != null;
  }

  public void validate() {
    LOGGER.debug("Событие: анализ кода");

    core.getTextEditor().setCurrentOffset(0);
    var textForCheck = "";
    var textModuleSelected = PhoenixAPI.getTextSelected();
    if (textModuleSelected.length() > 0) {
      // получем номер строки
      textForCheck = textModuleSelected;
      core.getTextEditor().setCurrentOffset(PhoenixAPI.getCurrentLineNumber());
    } else {
      textForCheck = PhoenixAPI.getTextAll();
    }

    core.updateContent(core.getProjectSetting().getFakePath(), textForCheck);
    binding.textDocumentDidChange(core.getFakeUri(), textForCheck);
    binding.textDocumentDidSave(core.getFakeUri());
  }

  public void formatting() {
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
    core.updateContent(core.getProjectSetting().getFakePath(), textForFormatting);
    binding.textDocumentDidChange(core.getFakeUri(), textForFormatting);

    // Formatting
    var result = binding.textDocumentFormatting(core.getFakeUri());

    String newText = null;
    try {
      newText = result.get().get(0).getNewText();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("Ошибка получения форматированного текста", e);
    }

    if (newText != null) {
      PhoenixAPI.insetTextOnForm(newText, isSelected);
    }
  }

  public void fixAll() {
    var separator = "\n";

    var textForQF = PhoenixAPI.getTextAll();

    // найдем все диагностики подсказки
    var listQF = core.getTextEditor().getDiagnostics().stream()
      .filter(this::isAcceptDiagnosticForQuickFix)
      .collect(Collectors.toList());

    List<Either<Command, CodeAction>> codeActions = new ArrayList<>();
    try {
      codeActions = binding.textDocumentCodeAction(core.getFakeUri(), listQF);
    } catch (ExecutionException | InterruptedException e) {
      LOGGER.error(e.getMessage());

    }
    LOGGER.debug("Квикфиксов найдено: " + codeActions);
    String[] strings = textForQF.split(separator);

    try {
      applyAllQuickFixes(codeActions, strings);
    } catch (ArrayIndexOutOfBoundsException e) {
      LOGGER.error("При применении fix all к тексту модуля возникли ошибки", e);
      return;
    }

    if (!codeActions.isEmpty()) {
      var text = String.join(separator, strings);
      PhoenixAPI.insetTextOnForm(text, false);
    }
  }

  private void applyAllQuickFixes(List<Either<Command, CodeAction>> codeActions, String[] strings) {
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

  }

  private boolean isAcceptDiagnosticForQuickFix(Diagnostic diagnostic) {
    return DesignerTextEditor.DIAGNOSTIC_FOR_QF.contains(diagnostic.getCode().getLeft());
  }

  @SneakyThrows
  private String getLSVersion() {
    var result = "<Неопределено>";
    var arguments = ProcessHelper.getArgumentsRunProcessBSLLS(core.getConfiguration());
    arguments.add("--version");
    Process processBSL;
    try {
      processBSL = new ProcessBuilder().command(arguments.toArray(new String[0])).start();
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
      return result;
    }

    var out = ProcessHelper.getStdoutProcess(processBSL);
    if (out == null) {
      return result;
    }
    if (out.startsWith("version")) {
      result = out.replace("version: ", "");
    }
    return result;
  }
}

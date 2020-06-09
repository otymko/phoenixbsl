package org.github.otymko.phoenixbsl.logic.lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.github.otymko.phoenixbsl.PhoenixCore;
import org.github.otymko.phoenixbsl.logic.event.EventManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BSLLanguageClient implements LanguageClient {

  public BSLLanguageClient() {}

  @Override
  public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
    return null;
  }

  @Override
  public CompletableFuture<Void> registerCapability(RegistrationParams params) {
    return null;
  }

  @Override
  public CompletableFuture<Void> unregisterCapability(UnregistrationParams params) {
    return null;
  }

  @Override
  public void telemetryEvent(Object o) {

  }

  @Override
  public void publishDiagnostics(PublishDiagnosticsParams publishDiagnosticsParams) {

    var core = PhoenixCore.getInstance();

    var diagnosticList = core.getDiagnosticList();
    diagnosticList.clear();
    diagnosticList.addAll(publishDiagnosticsParams.getDiagnostics());

    PhoenixCore.getInstance().getEventManager().notify(
      EventManager.EVENT_UPDATE_ISSUES,
      diagnosticList
    );

  }

  @Override
  public void showMessage(MessageParams messageParams) {
  }

  @Override
  public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams showMessageRequestParams) {
    return null;
  }

  @Override
  public void logMessage(MessageParams messageParams) {

  }

  @Override
  public CompletableFuture<List<WorkspaceFolder>> workspaceFolders() {
    return null;
  }

  @Override
  public CompletableFuture<List<Object>> configuration(ConfigurationParams configurationParams) {
    return null;
  }

  @Override
  public void semanticHighlighting(SemanticHighlightingParams params) {

  }

}

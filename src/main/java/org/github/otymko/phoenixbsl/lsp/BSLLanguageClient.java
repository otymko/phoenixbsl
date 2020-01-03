package org.github.otymko.phoenixbsl.lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.github.otymko.phoenixbsl.core.PhoenixApp;
import org.github.otymko.phoenixbsl.events.EventManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BSLLanguageClient implements LanguageClient {

  public BSLLanguageClient() {
//    PhoenixApp.getInstance().getEventManager().subscribe(this, EventManager.);
  }

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

    List<Diagnostic> list = publishDiagnosticsParams.getDiagnostics();
    if (list.isEmpty()) {
      return;
    }

    PhoenixApp.getInstance().getEventManager().notify(
      EventManager.EVENT_UPDATE_ISSUES,
      publishDiagnosticsParams.getDiagnostics());

//    var issuesForm = PhoenixApp.getInstance().getIssuesForm();
//    issuesForm.updateIssues(publishDiagnosticsParams.getDiagnostics());
//    issuesForm.onVisible();


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

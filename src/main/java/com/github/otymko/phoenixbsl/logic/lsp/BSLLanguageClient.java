package com.github.otymko.phoenixbsl.logic.lsp;

import com.github.otymko.phoenixbsl.PhoenixCore;
import com.github.otymko.phoenixbsl.logic.event.EventManager;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.SemanticHighlightingParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.UnregistrationParams;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BSLLanguageClient implements LanguageClient {

  public BSLLanguageClient() {
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

    var core = PhoenixCore.getInstance();

    var diagnosticList = core.getTextEditor().getDiagnostics();
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

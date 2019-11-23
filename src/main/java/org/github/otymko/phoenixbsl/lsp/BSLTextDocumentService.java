package org.github.otymko.phoenixbsl.lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BSLTextDocumentService implements TextDocumentService {

  private LanguageServer server;

  public BSLTextDocumentService(LanguageServer server) {
    this.server = server;
  }

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    server.getTextDocumentService().didOpen(params);
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    server.getTextDocumentService().didChange(params);
  }

  @Override
  public void didClose(DidCloseTextDocumentParams params) {
    server.getTextDocumentService().didClose(params);
  }

  @Override
  public void didSave(DidSaveTextDocumentParams params) {
    server.getTextDocumentService().didSave(params);
  }

  public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
    return server.getTextDocumentService().formatting(params);
  }

}

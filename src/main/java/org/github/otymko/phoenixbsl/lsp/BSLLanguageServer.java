package org.github.otymko.phoenixbsl.lsp;

import com.google.common.annotations.VisibleForTesting;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class BSLLanguageServer implements LanguageServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(BSLLanguageServer.class.getSimpleName());

  private BSLLanguageClient client;
  private LanguageServer server;
  private InputStream in;
  private OutputStream out;
  private Launcher<LanguageServer> launcher;

  private BSLTextDocumentService textDocumentService;

  public BSLLanguageServer(BSLLanguageClient client, InputStream in, OutputStream out) {
    this.client = client;
    this.in = in;
    this.out = out;
  }

  public void startInThread() {
    LOGGER.info("Подключение к серверу LSP");
    Thread thread = new Thread(this::start);
    thread.setDaemon(true);
    thread.setName("BSLLanguageLauncher");
    thread.start();
  }

  @VisibleForTesting
  private void start() {

    launcher = LSPLauncher.createClientLauncher(client, in, out);
    Future<?> future = launcher.startListening();

    server = launcher.getRemoteProxy();
    textDocumentService = new BSLTextDocumentService(server);

    while (true) {
      try {
        future.get();
        return;
      } catch (InterruptedException e) {
        LOGGER.error(e.getMessage().toString());
      } catch (ExecutionException e) {
        LOGGER.error(e.getMessage().toString());
      }
    }

  }

  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    return server.initialize(params);
  }

  public CompletableFuture<Object> shutdown() {
    return server.shutdown();
  }

  @Override
  public void exit() {
    server.exit();
  }

  @Override
  public TextDocumentService getTextDocumentService() {
    return textDocumentService;
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return null;
  }

}

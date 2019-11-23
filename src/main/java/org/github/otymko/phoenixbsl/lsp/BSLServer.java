package org.github.otymko.phoenixbsl.lsp;

import com.google.common.annotations.VisibleForTesting;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class BSLServer {

  private BSLClient client;
  private InputStream in;
  private OutputStream out;

  public Launcher<LanguageServer> launcher;
  private LanguageServer server;

  public BSLServer(BSLClient client, InputStream in, OutputStream out) {
    this.client = client;
    this.in = in;
    this.out = out;
  }

  public void startInThread() {
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

    while (true) {
      try {
        future.get();
        return;
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }

  }

  // TextDocumentService

  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    return server.initialize(params);
  }

  public CompletableFuture<Object> shutdown() {
    return server.shutdown();
  }

  public void didOpen(DidOpenTextDocumentParams params) {
    launcher.getRemoteProxy().getTextDocumentService().didOpen(params);
  }

  public void didChange(DidChangeTextDocumentParams params) {
    launcher.getRemoteProxy().getTextDocumentService().didChange(params);
  }

  public void didSave(DidSaveTextDocumentParams params) {
    launcher.getRemoteProxy().getTextDocumentService().didSave(params);
  }

  public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
    return launcher.getRemoteProxy().getTextDocumentService().formatting(params);
  }

  // TODO: перенести в другое место?
  public InitializeParams createInitializeParams() {

    var params = new InitializeParams();
    params.setProcessId(getProcessId());
    params.setTrace("on");

    ClientCapabilities serverCapabilities = new ClientCapabilities();
    params.setCapabilities(serverCapabilities);

    return params;

  }

  private int getProcessId() {
    var bean = ManagementFactory.getRuntimeMXBean();
    var jvmName = bean.getName();
    var pid = Long.valueOf(jvmName.split("@")[0]);
    return pid.intValue();
  }

}

package org.github.otymko.phoenixbsl;

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

public class BSLLanguageLauncher {

  private BSLLanguageClient client;
  private InputStream in;
  private OutputStream out;

  public Launcher<LanguageServer> launcher;

  public BSLLanguageLauncher(BSLLanguageClient client, InputStream in, OutputStream out) {
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

  public void start() {

    launcher = LSPLauncher.createClientLauncher(client, in, out);
    Future<?> future = launcher.startListening();

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

  public void sendInitialize(InitializeParams params) {
    launcher.getRemoteProxy().initialize(params);
  }

  public CompletableFuture<List<? extends TextEdit>> sendFormatting(DocumentFormattingParams params) {
    return launcher.getRemoteProxy().getTextDocumentService().formatting(params);
  }

  public void sendTextDocumentDidOpen(DidOpenTextDocumentParams params) {
    launcher.getRemoteProxy().getTextDocumentService().didOpen(params);
  }

  public void sendTextDocumentDidChange(DidChangeTextDocumentParams params) {
    launcher.getRemoteProxy().getTextDocumentService().didChange(params);
  }

  public void sendTextDocumentDidSave(DidSaveTextDocumentParams params) {
    launcher.getRemoteProxy().getTextDocumentService().didSave(params);
  }

  public void sendShutdown() {
    launcher.getRemoteProxy().shutdown();
  }

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

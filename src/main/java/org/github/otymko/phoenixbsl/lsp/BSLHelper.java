package org.github.otymko.phoenixbsl.lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BSLHelper {

  public static final String FAKE_PATH_FILE = "F:/BSL/fake.bsl";
  public static final File fakeFile = new File(FAKE_PATH_FILE);

  public static void textDocumentDidChange(LanguageServer server, String textForCheck) {
    var params = new DidChangeTextDocumentParams();
    VersionedTextDocumentIdentifier versionedTextDocumentIdentifier = new VersionedTextDocumentIdentifier();
    versionedTextDocumentIdentifier.setUri(fakeFile.toPath().toAbsolutePath().toUri().toString());
    versionedTextDocumentIdentifier.setVersion(1);
    params.setTextDocument(versionedTextDocumentIdentifier);
    var textDocument = new TextDocumentContentChangeEvent();
    textDocument.setText(textForCheck);
    List<TextDocumentContentChangeEvent> list = new ArrayList<>();
    list.add(textDocument);
    params.setContentChanges(list);
    server.getTextDocumentService().didChange(params);
  }

  public static void textDocumentDidOpen(LanguageServer server) {
    // откроем фейковый документ
    DidOpenTextDocumentParams params = new DidOpenTextDocumentParams();
    TextDocumentItem item = new TextDocumentItem();
    item.setLanguageId("bsl");
    item.setUri(fakeFile.toPath().toAbsolutePath().toUri().toString());
    item.setText("");
    params.setTextDocument(item);
    server.getTextDocumentService().didOpen(params);
  }

  public static void textDocumentDidSave(LanguageServer server) {
    var paramsSave = new DidSaveTextDocumentParams();
    TextDocumentIdentifier textDocumentIdentifier = new TextDocumentIdentifier();
    textDocumentIdentifier.setUri(fakeFile.toPath().toAbsolutePath().toUri().toString());
    paramsSave.setTextDocument(textDocumentIdentifier);
    server.getTextDocumentService().didSave(paramsSave);
  }

  public static CompletableFuture<List<? extends TextEdit>> textDocumentFormatting(LanguageServer server) {
    var paramsFormatting = new DocumentFormattingParams();
    var identifier = new TextDocumentIdentifier();
    identifier.setUri(fakeFile.toPath().toAbsolutePath().toUri().toString());
    paramsFormatting.setTextDocument(identifier);
    var options = new FormattingOptions(4, false);
    paramsFormatting.setOptions(options);
    return server.getTextDocumentService().formatting(paramsFormatting);
  }

  public static InitializeParams createInitializeParams() {
    var params = new InitializeParams();
    params.setProcessId(getProcessId());
    params.setTrace("on");
    ClientCapabilities serverCapabilities = new ClientCapabilities();
    params.setCapabilities(serverCapabilities);
    return params;
  }

  private static int getProcessId() {
    var bean = ManagementFactory.getRuntimeMXBean();
    var jvmName = bean.getName();
    var pid = Long.valueOf(jvmName.split("@")[0]);
    return pid.intValue();
  }

}

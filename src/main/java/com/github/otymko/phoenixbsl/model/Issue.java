package com.github.otymko.phoenixbsl.model;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import org.eclipse.lsp4j.DiagnosticSeverity;

public class Issue extends RecursiveTreeObject<Issue> {

  private String description = "";
  private String location = "";
  private int startLine = 0;
  private DiagnosticSeverity severity = DiagnosticSeverity.Hint;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getLocation() {
    return location;
  }

  public void setSeverity(DiagnosticSeverity severity) {
    this.severity = severity;
  }

  public DiagnosticSeverity getSeverity() {
    return severity;
  }

  @Override
  public String toString() {
    return this.description + " " + this.location;
  }

  public int getStartLine() {
    return startLine;
  }

  public void setStartLine(int startLine) {
    this.startLine = startLine;
  }
}

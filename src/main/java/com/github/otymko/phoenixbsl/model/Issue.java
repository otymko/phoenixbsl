package com.github.otymko.phoenixbsl.model;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.lsp4j.DiagnosticSeverity;

@Getter
@Setter
public class Issue extends RecursiveTreeObject<Issue> {

  private String description = "";
  private String location = "";
  private int startLine = 0;
  private DiagnosticSeverity severity = DiagnosticSeverity.Hint;

  @Override
  public String toString() {
    return this.description + " " + this.location;
  }

}

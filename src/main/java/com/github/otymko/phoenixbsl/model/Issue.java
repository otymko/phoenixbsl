package com.github.otymko.phoenixbsl.model;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.lsp4j.DiagnosticSeverity;

@Data
@EqualsAndHashCode(callSuper = true)
public class Issue extends RecursiveTreeObject<Issue> {
  private String source = "";
  private String description = "";
  private String location = "";
  private int startLine = 0;
  private DiagnosticSeverity severity = DiagnosticSeverity.Hint;

  @Override
  public String toString() {
    return this.description + " " + this.location;
  }

}

package org.github.otymko.phoenixbsl.lsp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BSLConfiguration {

  private String diagnosticLanguage;
  private boolean showCognitiveComplexityCodeLens;
  private boolean showCyclomaticComplexityCodeLens;
  @JsonIgnore
  private String computeDiagnosticsTrigger = "onSave";
  private String computeDiagnosticsSkipSupport;
  private String traceLog;
  private String configurationRoot;

}

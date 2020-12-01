package com.github.otymko.phoenixbsl.logic.sonarlint;

import com.github.otymko.phoenixbsl.logic.service.SonarLintService;
import lombok.experimental.UtilityClass;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class SonarLintHelper {
  private final Map<String, DiagnosticSeverity> STRING_TO_SEVERITY_MAP = createStringToSeverityMap();

  public Diagnostic newDiagnosticByIssue(Issue issue) {
    var line = issue.getStartLine() == null ? 1 : issue.getStartLine();
    var position = new Position(line - 1, 0);
    var diagnostic = new Diagnostic();
    diagnostic.setSource(SonarLintService.SOURCE);
    diagnostic.setSeverity(STRING_TO_SEVERITY_MAP.getOrDefault(issue.getType(), DiagnosticSeverity.Information));
    diagnostic.setMessage(issue.getMessage());
    diagnostic.setCode(issue.getRuleKey());
    diagnostic.setRange(new Range(position, position));
    return diagnostic;
  }

  private Map<String, DiagnosticSeverity> createStringToSeverityMap() {
    Map<String, DiagnosticSeverity> map = new HashMap<>();
    map.put("BUG", DiagnosticSeverity.Error);
    map.put("CODE_SMELL", DiagnosticSeverity.Information);
    map.put("VULNERABILITY", DiagnosticSeverity.Error);
    map.put("SECURITY_HOTSPOT", DiagnosticSeverity.Error);
    return map;
  }

}

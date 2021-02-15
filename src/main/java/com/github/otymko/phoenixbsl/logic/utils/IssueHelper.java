package com.github.otymko.phoenixbsl.logic.utils;

import com.github.otymko.phoenixbsl.logic.PhoenixAPI;
import com.github.otymko.phoenixbsl.logic.text.Location;
import com.github.otymko.phoenixbsl.model.Issue;
import lombok.experimental.UtilityClass;
import org.eclipse.lsp4j.Diagnostic;

@UtilityClass
public class IssueHelper {

  public boolean checkDiagnosticBySelection(Diagnostic diagnostic, Location selection) {
    if (selection == Location.empty()) {
      return true;
    }
    return diagnostic.getRange().getStart().getLine() >= selection.getStartLine()
      && diagnostic.getRange().getEnd().getLine() <= selection.getEndLine();
  }

  public Issue createIssue(Diagnostic diagnostic) {
    var range = diagnostic.getRange();
    var position = range.getStart();
    var startLine = position.getLine() + 1;

    var issue = new Issue();
    issue.setSource(PhoenixAPI.getValueSourceByString(diagnostic.getSource()));
    issue.setDescription(diagnostic.getMessage());
    issue.setStartLine(startLine);
    issue.setLocation(String.valueOf(startLine));
    issue.setSeverity(diagnostic.getSeverity());
    return issue;
  }

}

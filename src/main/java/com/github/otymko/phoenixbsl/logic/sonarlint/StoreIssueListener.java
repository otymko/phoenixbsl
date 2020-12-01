package com.github.otymko.phoenixbsl.logic.sonarlint;

import lombok.Getter;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.client.api.common.analysis.IssueListener;

import java.util.List;

public class StoreIssueListener implements IssueListener {
  @Getter
  private final List<Issue> issues;

  public StoreIssueListener(List<Issue> issues) {
    this.issues = issues;
  }

  @Override
  public void handle(Issue issue) {
    issues.add(issue);
  }
}

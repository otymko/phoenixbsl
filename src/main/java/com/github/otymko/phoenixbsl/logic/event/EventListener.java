package com.github.otymko.phoenixbsl.logic.event;

import org.eclipse.lsp4j.Diagnostic;

import java.util.List;

public interface EventListener {
  default void formatting() {
  }

  default void inspection() {
  }

  default void fixAll() { }

  default void updateIssues(List<Diagnostic> diagnostics) {
  }

  default void showIssuesStage() { }

  default void showSettingStage() {}

}

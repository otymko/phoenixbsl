package com.github.otymko.phoenixbsl.logic.event;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.lsp4j.Diagnostic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class EventManager {
  public static final int EVENT_INSPECTION = 1;
  public static final int EVENT_FORMATTING = 2;
  public static final int EVENT_UPDATE_ISSUES = 3;
  public static final int SHOW_ISSUE_STAGE = 4;
  public static final int SHOW_SETTING_STAGE = 5;
  public static final int EVENT_FIX_ALL = 6;
  private final Map<Integer, List<EventListener>> listeners = new HashMap<>();

  public EventManager(int... operations) {
    for (int operation : operations) {
      listeners.put(operation, new ArrayList<>());
    }
  }

  public synchronized void subscribe(int eventType, EventListener listener) {
    List<EventListener> users = listeners.get(eventType);
    users.add(listener);
  }

  public synchronized void unsubscribe(int eventType, EventListener listener) {
    List<EventListener> users = listeners.get(eventType);
    users.remove(listener);
  }

  public synchronized void notify(int eventType) {
    List<EventListener> users = listeners.get(eventType);
    for (EventListener listener : users) {
      if (eventType == EVENT_INSPECTION) {
        listener.inspection();
      } else if (eventType == EVENT_FORMATTING) {
        listener.formatting();
      } else if (eventType == SHOW_ISSUE_STAGE) {
        listener.showIssuesStage();
      } else if (eventType == SHOW_SETTING_STAGE) {
        listener.showSettingStage();
      } else if (eventType == EVENT_FIX_ALL) {
        listener.fixAll();
      }
    }
  }

  public synchronized void notify(int eventType, List<Diagnostic> diagnostics) {
    List<EventListener> users = listeners.get(eventType);
    if (users == null) {
      return;
    }
    for (EventListener listener : users) {
      if (eventType == EVENT_UPDATE_ISSUES) {
        listener.updateIssues(diagnostics);
      }
    }
  }

}

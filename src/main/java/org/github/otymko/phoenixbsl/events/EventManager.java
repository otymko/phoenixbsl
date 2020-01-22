package org.github.otymko.phoenixbsl.events;

import org.eclipse.lsp4j.Diagnostic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventManager {

  public static final int EVENT_INSPECTION = 1;
  public static final int EVENT_FORMATTING = 2;
  public static final int EVENT_UPDATE_ISSUES = 3;
  public static final int SHOW_ISSUE_STAGE = 4;

  private static final Logger LOGGER = LoggerFactory.getLogger(EventManager.class.getSimpleName());

  Map<Integer, List<EventListener>> listeners = new HashMap<>();

  public EventManager(int... operations) {
    for (int operation : operations) {
      this.listeners.put(operation, new ArrayList<>());
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
    List< EventListener> users = listeners.get(eventType);
    for (EventListener listener : users) {
      if (eventType == EVENT_INSPECTION) {
        listener.inspection();
      } else if (eventType == EVENT_FORMATTING) {
        listener.formatting();
      } else if (eventType == SHOW_ISSUE_STAGE){
        listener.showIssuesStage();
      }
    }
  }

  public synchronized void notify(int eventType, List<Diagnostic> diagnostics) {
    LOGGER.info("Событие {}", eventType);
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

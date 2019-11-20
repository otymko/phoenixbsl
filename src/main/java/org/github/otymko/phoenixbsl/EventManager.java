package org.github.otymko.phoenixbsl;

import java.util.*;

public class EventManager {

  public static final int EVENT_INSPECTION = 1;
  public static final int EVENT_FORMATTING = 2;

  Map<Integer, List<EventListener>> listeners = new HashMap<>();

  public EventManager(int ... operations) {
    for (int operation : operations) {
      this.listeners.put(operation, new ArrayList<>());
    }
  }

  public void subscribe(int eventType, EventListener listener) {
    List<EventListener> users = listeners.get(eventType);
    users.add(listener);
  }

  public void unsubscribe(int eventType, EventListener listener) {
    List<EventListener> users = listeners.get(eventType);
    users.remove(listener);
  }

  public void notify(int eventType) {
    List<EventListener> users = listeners.get(eventType);
    for (EventListener listener : users) {
      if (eventType == EVENT_INSPECTION) {
        listener.inspection();
      } else if (eventType == EVENT_FORMATTING) {
        listener.formatting();
      }
    }
  }

}

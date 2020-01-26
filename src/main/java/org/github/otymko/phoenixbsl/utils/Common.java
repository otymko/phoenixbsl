package org.github.otymko.phoenixbsl.utils;

import javafx.fxml.FXMLLoader;
import org.github.otymko.phoenixbsl.views.StageBarController;

public class Common {

  public static void setControllerFactory(FXMLLoader loader, StageBarController controller) {
    loader.setControllerFactory(type -> {
      try {
        if (type == StageBarController.class) {
          return controller;
        }
        // default implementation:
        return type.getDeclaredConstructor().newInstance();
      } catch (Exception exc) {
        // this is pretty much fatal...
        throw new RuntimeException(exc);
      }
    });
  }

}

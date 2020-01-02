package org.github.otymko.phoenixbsl.views;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CustomIssue extends RecursiveTreeObject<CustomIssue> {

  StringProperty description;
  StringProperty position;
  StringProperty type;

  public CustomIssue(String description, String position, String type) {
    this.description = new SimpleStringProperty(description);
    this.position = new SimpleStringProperty(position);
    this.type = new SimpleStringProperty(type);
  }

}

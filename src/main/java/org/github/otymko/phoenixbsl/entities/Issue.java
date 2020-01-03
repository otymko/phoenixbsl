package org.github.otymko.phoenixbsl.entities;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

public class Issue extends RecursiveTreeObject<Issue> {

  private String description = "";
  private String location = "";
  private int startLine = 0;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getLocation() {
    return location;
  }

  @Override
  public String toString() {
    return this.description + " " + this.location;
  }

  public int getStartLine() {
    return startLine;
  }

  public void setStartLine(int startLine) {
    this.startLine = startLine;
  }
}

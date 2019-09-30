package org.github.otymko.phoenixbsl.entities;

public class Issue {

  private String discription = "";
  private String location = "";
  private int startLine = 0;

  public String getDiscription() {
    return discription;
  }

  public void setDiscription(String discription) {
    this.discription = discription;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getLocation() {
    return location;
  }

  @Override
  public String toString() {
    return this.discription + " " + this.location;
  }

  public int getStartLine() {
    return startLine;
  }

  public void setStartLine(int startLine) {
    this.startLine = startLine;
  }
}

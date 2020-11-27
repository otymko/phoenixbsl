package com.github.otymko.phoenixbsl.gui.controller;

import com.github.otymko.phoenixbsl.model.Issue;
import com.jfoenix.controls.JFXTreeTableView;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lombok.Getter;

@Getter
public class IssueStageController {

  @FXML
  private JFXTreeTableView<Issue> issuesTree;

  @FXML
  private TextField search;

  @FXML
  private Label labelError;

  @FXML
  private Label labelWarning;

  @FXML
  private Label labelInfo;

}



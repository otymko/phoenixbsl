package org.github.otymko.phoenixbsl.views;

import com.jfoenix.controls.JFXTreeTableView;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lombok.Getter;
import org.github.otymko.phoenixbsl.entities.Issue;

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



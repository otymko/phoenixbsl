package org.github.otymko.phoenixbsl.views;

import com.jfoenix.controls.JFXTreeTableView;
import javafx.fxml.FXML;
import lombok.Getter;
import org.github.otymko.phoenixbsl.entities.Issue;

@Getter
public class IssueStageController {

  @FXML
  private JFXTreeTableView<Issue> issuesTree;

}



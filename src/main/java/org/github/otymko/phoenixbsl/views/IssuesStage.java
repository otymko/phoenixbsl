package org.github.otymko.phoenixbsl.views;

import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.github.otymko.phoenixbsl.core.PhoenixAPI;
import org.github.otymko.phoenixbsl.core.PhoenixApp;
import org.github.otymko.phoenixbsl.entities.Issue;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class IssuesStage extends Stage {

  private Map<DiagnosticSeverity, String> severityToStringMap = createSeverityToStringMap();

  private TreeTableView<Issue> tree;
  private RecursiveTreeItem<Issue> recursiveTreeItem;

  public int lineOffset = 0;

  private int countError = 0;
  private int countWarning = 0;
  private int countInfo = 0;

  private Label labelError;
  private Label labelWarning;
  private Label labelInfo;


  public IssuesStage() {


    Parent root = null;
    try {
      root = FXMLLoader.load(PhoenixApp.class.getResource("/IssuesStage.fxml"));
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    Scene scene = new Scene(root);
    setScene(scene);

    getIcons().add(new Image(PhoenixApp.class.getResourceAsStream("/phoenix.jpg")));
    setTitle("Phoenix BSL");

    tree = (TreeTableView<Issue>) scene.lookup("#issuesTree");
    tree.setPlaceholder(new Label("Замечаний нет"));

    TreeTableColumn<Issue, String> descriptionColumn = new JFXTreeTableColumn<>("Описание");
    descriptionColumn.setPrefWidth(450);
    descriptionColumn.setCellFactory(param -> {
      TreeTableCell<Issue, String> cell = new TreeTableCell<>();
      Text text = new Text();
      cell.setGraphic(text);
      cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
      text.textProperty().bind(cell.itemProperty());
      text.wrappingWidthProperty().bind(descriptionColumn.widthProperty());
      return cell;
    });
    descriptionColumn.setCellValueFactory(
      param -> new SimpleStringProperty(param.getValue().getValue().getDescription()));

    TreeTableColumn<Issue, String> positionColumn = new JFXTreeTableColumn<>("стр.");
    positionColumn.setPrefWidth(60);
    positionColumn.setMinWidth(60);
    positionColumn.setMaxWidth(60);
    positionColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getLocation()));
    positionColumn.setReorderable(false);
    positionColumn.setResizable(false);

    TreeTableColumn<Issue, String> typeColumn = new JFXTreeTableColumn<>("Тип");
    typeColumn.setPrefWidth(120);
    typeColumn.setMinWidth(120);
    typeColumn.setMaxWidth(120);
    typeColumn.setCellValueFactory(param -> new SimpleStringProperty(severityToStringMap.get(param.getValue().getValue().getSeverity())));
    typeColumn.setReorderable(false);
    typeColumn.setResizable(false);

    ObservableList<Issue> issues = FXCollections.observableArrayList();

    recursiveTreeItem = new RecursiveTreeItem<>(issues, RecursiveTreeObject::getChildren);
    tree.setRoot(recursiveTreeItem);
    tree.setShowRoot(false);
    tree.setEditable(true);
    tree.getColumns().add(descriptionColumn);
    tree.getColumns().add(positionColumn);
    tree.getColumns().add(typeColumn);

    tree.setOnMouseClicked(event -> {
      if (event.getClickCount() != 2) {
        return;
      }

      var issue = tree.getSelectionModel().getSelectedItem().getValue();
      PhoenixAPI.gotoLineModule(issue.getStartLine(), PhoenixApp.getInstance().getFocusForm());

    });

    labelError = (Label) scene.lookup("#labelError");
    labelWarning = (Label) scene.lookup("#labelWarning");
    labelInfo = (Label) scene.lookup("#labelInfo");

    TextField filter = (TextField) scene.lookup("#search");
    filter.textProperty().addListener((o, oldVal, newVal) -> filterIssuesTree(newVal));

    updateIndicators();

  }

  private void filterIssuesTree(String filter) {
    if (filter.isEmpty()) {
      recursiveTreeItem.setPredicate(userProp -> true);
    } else {
      tree.setRoot(recursiveTreeItem);
      recursiveTreeItem.setPredicate(userProp -> {
        final Issue issue = userProp.getValue();
        final String filterLowerCase = filter.toLowerCase();
        return issue.getDescription().toLowerCase().contains(filterLowerCase)
          || severityToStringMap.get(issue.getSeverity()).toLowerCase().contains(filterLowerCase)
          || issue.getLocation().toLowerCase().contains(filterLowerCase);
      });
    }
  }

  public IssuesStage(Stage ownerStage) {
    this();
    initOwner(ownerStage);
  }

  public void updateIssues(List<Diagnostic> diagnostics) {

    countError = 0;
    countWarning = 0;
    countInfo = 0;

    ObservableList<Issue> issues = FXCollections.observableArrayList();
    diagnostics.stream().forEach(diagnostic -> {
      var range = diagnostic.getRange();
      var position = range.getStart();
      var startLine = position.getLine() + 1 + lineOffset;

      Issue issue = new Issue();
      issue.setDescription(diagnostic.getMessage());
      issue.setStartLine(startLine);
      issue.setLocation(String.valueOf(startLine));
      issue.setSeverity(diagnostic.getSeverity());
      issues.add(issue);

      if (diagnostic.getSeverity() == DiagnosticSeverity.Error) {
        countError++;
      } else if (diagnostic.getSeverity() == DiagnosticSeverity.Warning) {
        countWarning++;
      } else {
        countInfo++;
      }
    });

    updateIndicators();

    recursiveTreeItem = new RecursiveTreeItem<>(issues, RecursiveTreeObject::getChildren);
    tree.setRoot(recursiveTreeItem);
    tree.setShowRoot(false);

    this.toFront();
    this.setIconified(false);

  }

  private void updateIndicators() {

    labelError.setText("Ошибки: " + countError);
    labelWarning.setText("Предупреждения: " + countWarning);
    labelInfo.setText("Инфо: " + countInfo);

  }

  private Map<DiagnosticSeverity, String> createSeverityToStringMap() {
    Map<DiagnosticSeverity, String> map = new EnumMap<>(DiagnosticSeverity.class);
    map.put(DiagnosticSeverity.Error, "Ошибка");
    map.put(DiagnosticSeverity.Information, "Информация");
    map.put(DiagnosticSeverity.Hint, "Подсказка");
    map.put(DiagnosticSeverity.Warning, "Предупреждение");
    return map;
  }

}

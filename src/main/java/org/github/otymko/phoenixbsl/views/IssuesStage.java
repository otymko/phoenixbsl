package org.github.otymko.phoenixbsl.views;

import com.jfoenix.assets.JFoenixResources;
import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.jfoenix.svg.SVGGlyph;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.github.otymko.phoenixbsl.core.PhoenixAPI;
import org.github.otymko.phoenixbsl.core.PhoenixApp;
import org.github.otymko.phoenixbsl.entities.Issue;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
public class IssuesStage extends Stage {

  private static Map<DiagnosticSeverity, String> severityToStringMap = createSeverityToStringMap();
  private static Map<String, DiagnosticSeverity> stringToSeverityMap = createStringToSeverityMap();

  private ObservableList<Issue> issues = FXCollections.observableArrayList();

  private JFXTreeTableView<Issue> tree;
  private RecursiveTreeItem<Issue> recursiveTreeItem;

  private TextField search;

  public int lineOffset = 0;

  private int countError = 0;
  private int countWarning = 0;
  private int countInfo = 0;

  private Label labelError;
  private Label labelWarning;
  private Label labelInfo;

  private TreeTableColumn<Issue, String> typeColumn;


  @SneakyThrows
  public IssuesStage() {
    FXMLLoader loader = new FXMLLoader(PhoenixApp.class.getResource("/IssuesStage.fxml"));

    Parent root = loader.load();
    IssueStageController localController = loader.getController();

    JFXDecorator decorator = new JFXDecorator(this, root, false, true, true);
    decorator.setCustomMaximize(true);
    decorator.setGraphic(new SVGGlyph(""));

    Scene scene = new Scene(decorator, 600, 800);
    final ObservableList<String> stylesheets = scene.getStylesheets();
    stylesheets.addAll(JFoenixResources.load("/theme.css").toExternalForm());
    setScene(scene);

    getIcons().add(new Image(PhoenixApp.class.getResourceAsStream("/phoenix.png")));
    this.setTitle("Phoenix BSL v. " + PhoenixApp.getInstance().getVersionApp());

    tree = localController.getIssuesTree();
    initTreeTable();

    labelError = localController.getLabelError();
    labelWarning = localController.getLabelWarning();
    labelInfo = localController.getLabelInfo();

    search = localController.getSearch();
    search.textProperty().addListener((o, oldVal, newVal) -> filterIssuesTree(newVal));

    updateIndicators();
  }

  private void initTreeTable() {
    tree.setPlaceholder(new Label("Замечаний нет"));

    JFXTreeTableColumn<Issue, String> descriptionColumn = new JFXTreeTableColumn<>("Описание");
    descriptionColumn.setPrefWidth(450);
    descriptionColumn.setCellFactory(param -> {
      TreeTableCell<Issue, String> cell = new TreeTableCell<>();
      Text text = new Text();
      text.setStyle("-fx-text-fill: -fx-text-inner-color;");
      cell.setGraphic(text);
      cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
      text.textProperty().bind(cell.itemProperty());
      text.wrappingWidthProperty().bind(descriptionColumn.widthProperty());
      cell.setStyle("-fx-text-fill: -fx-text-inner-color;");
      return cell;
    });
    descriptionColumn.setContextMenu(null);
    descriptionColumn.setResizable(true);
    // FIXME: сделать более понятнее и универсальнее
    descriptionColumn.setCellValueFactory(param -> {

      if (!(param.getValue().getValue() instanceof Issue)) {
        return new SimpleStringProperty();
      }
      var issue = param.getValue().getValue();
      var value = "";
      if (issue != null) {
        value = issue.getDescription();
      }
      return new SimpleStringProperty(value);
    });

    JFXTreeTableColumn<Issue, Integer> positionColumn = new JFXTreeTableColumn<>("кол-во\n/\nстр.");
    positionColumn.setPrefWidth(90);
    positionColumn.setMinWidth(90);
    positionColumn.setMaxWidth(90);
    positionColumn.setContextMenu(null);
    positionColumn.setCellValueFactory(param -> {
      if (!(param.getValue().getValue() instanceof Issue)) {
        RecursiveTreeObject item = param.getValue().getValue();
        if (item == null) {
          return new SimpleIntegerProperty().asObject();
        } else {
          var groupValue = item.getGroupedValue();
          if (groupValue == null) {
            return new SimpleIntegerProperty().asObject();
          }
          DiagnosticSeverity severity = stringToSeverityMap.get(groupValue);
          var list = recursiveTreeItem.getChildren().stream()
            .filter(issueTreeItem -> issueTreeItem.getValue().getSeverity() == severity)
            .collect(Collectors.toList());
          return new SimpleIntegerProperty(list.size()).asObject();
        }
      }
      var issue = param.getValue().getValue();
      var value = 0;
      if (issue != null) {
        value = issue.getStartLine();
      }
      return new SimpleIntegerProperty(value).asObject();
    });
    positionColumn.setResizable(true);

    typeColumn = new JFXTreeTableColumn<>("Тип");
    typeColumn.setPrefWidth(130);
    typeColumn.setMinWidth(130);
    typeColumn.setMaxWidth(130);
    typeColumn.setResizable(true);
    typeColumn.setContextMenu(null);
    typeColumn.setCellValueFactory(param -> {
      if (!(param.getValue().getValue() instanceof Issue)) {

        RecursiveTreeObject item = param.getValue().getValue();
        if (item == null) {
          return new SimpleStringProperty();
        } else {
          Object prop = item.getGroupedValue();
          if (prop == null) {
            return new SimpleStringProperty();
          } else {
            return new SimpleStringProperty(item.getGroupedValue().toString());
          }
        }
      }
      var issue = param.getValue().getValue();
      String value = "";
      if (issue != null) {
        value = severityToStringMap.get(issue.getSeverity());
      }
      return new SimpleStringProperty(value);
    });

    ObservableList<Issue> issues = FXCollections.observableArrayList();

    recursiveTreeItem = new RecursiveTreeItem<>(issues, RecursiveTreeObject::getChildren);
    recursiveTreeItem.setExpanded(true);
    tree.setRoot(recursiveTreeItem);
    tree.setShowRoot(false);
    tree.setMaxWidth(9999);
    tree.getColumns().add(typeColumn);
    tree.getColumns().add(descriptionColumn);
    tree.getColumns().add(positionColumn);
    tree.setOnMouseClicked(event -> {
      if (event.getClickCount() != 2) {
        return;
      }
      var item = tree.getSelectionModel().getSelectedItem();
      if (item != null) {
        var issue = item.getValue();
        PhoenixAPI.gotoLineModule(issue.getStartLine(), PhoenixApp.getInstance().getFocusForm());
      }
    });

  }

  private void filterIssuesTree(String filter) {

    if (filter.isEmpty()) {
      recursiveTreeItem.setPredicate(userProp -> true);
    } else {
      recursiveTreeItem.setPredicate(userProp -> {
        final Issue issue = userProp.getValue();
        final String filterLowerCase = filter.toLowerCase();
        return issue.getDescription().toLowerCase().contains(filterLowerCase)
          || severityToStringMap.get(issue.getSeverity()).toLowerCase().contains(filterLowerCase)
          || issue.getLocation().toLowerCase().contains(filterLowerCase);
      });
    }
    tree.setRoot(recursiveTreeItem);
    tree.refresh();

  }

  public IssuesStage(Stage ownerStage) {
    this();
    initOwner(ownerStage);
  }

  public void updateIssues(List<Diagnostic> diagnostics) {
    countError = 0;
    countWarning = 0;
    countInfo = 0;

    issues.clear();
    diagnostics.forEach(diagnostic -> {
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

    FXCollections.sort(issues, Comparator.comparingInt(Issue::getStartLine));
    updateIndicators();

    recursiveTreeItem = new RecursiveTreeItem<>(issues, RecursiveTreeObject::getChildren);
    recursiveTreeItem.setExpanded(true);
    tree.setRoot(recursiveTreeItem);
    filterIssuesTree(search.getText());
    tree.setShowRoot(false);
    tree.unGroup(typeColumn);
    if (PhoenixApp.getInstance().getConfiguration().isUseGroupIssuesBySeverity()) {
      tree.group(typeColumn);
    }
    tree.refresh();

    this.toFront();
    this.setIconified(false);
  }

  private void updateIndicators() {
    labelError.setText("Ошибки: " + countError);
    labelWarning.setText("Предупреждения: " + countWarning);
    labelInfo.setText("Инфо: " + countInfo);
  }

  private static Map<DiagnosticSeverity, String> createSeverityToStringMap() {
    Map<DiagnosticSeverity, String> map = new EnumMap<>(DiagnosticSeverity.class);
    map.put(DiagnosticSeverity.Error, "Ошибка");
    map.put(DiagnosticSeverity.Information, "Информация");
    map.put(DiagnosticSeverity.Hint, "Подсказка");
    map.put(DiagnosticSeverity.Warning, "Предупреждение");
    return map;
  }

  // FIXME: переделать?
  private static Map<String, DiagnosticSeverity> createStringToSeverityMap() {
    Map<DiagnosticSeverity, String> map = severityToStringMap;
    Map<String, DiagnosticSeverity> thisMap = new HashMap<>();
    thisMap.put(map.get(DiagnosticSeverity.Error), DiagnosticSeverity.Error);
    thisMap.put(map.get(DiagnosticSeverity.Information), DiagnosticSeverity.Information);
    thisMap.put(map.get(DiagnosticSeverity.Hint), DiagnosticSeverity.Hint);
    thisMap.put(map.get(DiagnosticSeverity.Warning), DiagnosticSeverity.Warning);
    return thisMap;
  }

}

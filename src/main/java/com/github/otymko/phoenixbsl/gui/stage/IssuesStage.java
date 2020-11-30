package com.github.otymko.phoenixbsl.gui.stage;

import com.github.otymko.phoenixbsl.PhoenixCore;
import com.github.otymko.phoenixbsl.gui.controller.IssueStageController;
import com.github.otymko.phoenixbsl.logic.PhoenixAPI;
import com.github.otymko.phoenixbsl.logic.PhoenixContext;
import com.github.otymko.phoenixbsl.model.Issue;
import com.github.otymko.phoenixbsl.model.ProjectSetting;
import com.jfoenix.assets.JFoenixResources;
import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.jfoenix.svg.SVGGlyph;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
public class IssuesStage extends Stage {
  private static final String COLUMN_DESCRIPTION = "DESCRIPTION";
  private static final String COLUMN_POSITION = "POSITION";
  private static final String COLUMN_TYPE = "TYPE";
  private static final Map<DiagnosticSeverity, String> severityToStringMap = createSeverityToStringMap();
  private static final Map<String, DiagnosticSeverity> stringToSeverityMap = createStringToSeverityMap();

  private final ObservableList<Issue> issues = FXCollections.observableArrayList();

  private final JFXTreeTableView<Issue> tree;
  private RecursiveTreeItem<Issue> recursiveTreeItem;

  private final ComboBox<ProjectSetting> project;
  private final TextField search;

  public int lineOffset = 0;

  private int countError = 0;
  private int countWarning = 0;
  private int countInfo = 0;

  private final Label labelError;
  private final Label labelWarning;
  private final Label labelInfo;

  private TreeTableColumn<Issue, String> typeColumn;


  @SneakyThrows
  public IssuesStage() {
    FXMLLoader loader = new FXMLLoader(PhoenixCore.class.getResource("/IssuesStage.fxml"));

    Parent root = loader.load();
    IssueStageController localController = loader.getController();

    JFXDecorator decorator = new JFXDecorator(this, root, false, true, true);
    decorator.setCustomMaximize(true);
    decorator.setGraphic(new SVGGlyph(""));

    Scene scene = new Scene(decorator, 600, 800);
    final ObservableList<String> stylesheets = scene.getStylesheets();
    stylesheets.addAll(JFoenixResources.load("/theme.css").toExternalForm());
    setScene(scene);

    getIcons().add(new Image(PhoenixCore.class.getResourceAsStream("/phoenix.png")));
    this.setTitle("Phoenix BSL v. " + PhoenixCore.getInstance().getVersionApp());

    tree = localController.getIssuesTree();
    initTreeTable();

    labelError = localController.getLabelError();
    labelWarning = localController.getLabelWarning();
    labelInfo = localController.getLabelInfo();

    project = localController.getProject();
    project.getItems().setAll(PhoenixCore.getInstance().getConfiguration().getProjects());
    project.setPromptText("Выберите проект");
    project.getSelectionModel().select(PhoenixCore.getInstance().getProject());
    project.setOnAction(event -> {
      PhoenixCore.getInstance().updateProject(project.getSelectionModel().getSelectedItem());
    });

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
      cell.setGraphic(text);
      cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
      text.textProperty().bind(cell.itemProperty());
      text.wrappingWidthProperty().bind(descriptionColumn.widthProperty());
      cell.setStyle("-fx-text-fill: -fx-text-inner-color;");
      return cell;
    });
    descriptionColumn.setContextMenu(null);
    descriptionColumn.setResizable(true);
    descriptionColumn.setCellValueFactory(param -> new SimpleStringProperty(getValueCellDescription(param).toString()));

    JFXTreeTableColumn<Issue, Integer> positionColumn = new JFXTreeTableColumn<>("кол-во\n/\nстр.");
    positionColumn.setId("positionColumn");
    positionColumn.setPrefWidth(90);
    positionColumn.setMinWidth(90);
    positionColumn.setMaxWidth(90);
    positionColumn.setContextMenu(null);
    positionColumn.setCellValueFactory(param -> new SimpleIntegerProperty((Integer) getValueCellPosition(param)).asObject());
    positionColumn.setResizable(true);

    typeColumn = new JFXTreeTableColumn<>("Тип");
    typeColumn.setPrefWidth(130);
    typeColumn.setMinWidth(130);
    typeColumn.setMaxWidth(130);
    typeColumn.setResizable(true);
    typeColumn.setContextMenu(null);
    typeColumn.setCellValueFactory(param -> new SimpleStringProperty(getValueCellType(param).toString()));

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
        PhoenixAPI.gotoLineModule(issue.getStartLine(), PhoenixCore.getInstance().getTextEditor().getFocusForm());
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
    if (PhoenixCore.getInstance().getConfiguration().isUseGroupIssuesBySeverity()) {
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

  private static Map<String, DiagnosticSeverity> createStringToSeverityMap() {
    Map<DiagnosticSeverity, String> map = severityToStringMap;
    Map<String, DiagnosticSeverity> thisMap = new HashMap<>();
    thisMap.put(map.get(DiagnosticSeverity.Error), DiagnosticSeverity.Error);
    thisMap.put(map.get(DiagnosticSeverity.Information), DiagnosticSeverity.Information);
    thisMap.put(map.get(DiagnosticSeverity.Hint), DiagnosticSeverity.Hint);
    thisMap.put(map.get(DiagnosticSeverity.Warning), DiagnosticSeverity.Warning);
    return thisMap;
  }

  private Object getValueCellDescription(TreeTableColumn.CellDataFeatures<Issue, String> param) {
    return getValueCell(param.getValue(), COLUMN_DESCRIPTION);
  }

  private String getValueCell(TreeItem item, String column) {

    String result = "";
    var value = item.getValue();
    if (value instanceof Issue) {
      var issue = (Issue) value;
      switch (column) {
        case COLUMN_DESCRIPTION:
          result = issue.getDescription();
          break;
        case COLUMN_POSITION:
          result = String.valueOf(issue.getStartLine());
          break;
        case COLUMN_TYPE:
          result = severityToStringMap.get(issue.getSeverity());
          break;
        default:
          LOGGER.warn("Колонка не поддерживается: " + column);
          break;
      }
    } else {
      var treeObject = item.getValue();
      if (treeObject != null) {
        var groupValue = ((RecursiveTreeObject) treeObject).getGroupedValue();
        if (groupValue != null) {
          if (column.equals(COLUMN_TYPE)) {
            result = groupValue.toString();
          } else if (column.equals(COLUMN_POSITION)) {
            var severity = stringToSeverityMap.get(groupValue);
            var list = recursiveTreeItem.getChildren().stream()
              .filter(issueTreeItem -> issueTreeItem.getValue().getSeverity() == severity)
              .collect(Collectors.toList());
            result = String.valueOf(list.size());
          }
        }
      }
    }
    return result;
  }

  private Object getValueCellPosition(TreeTableColumn.CellDataFeatures<Issue, Integer> param) {
    var result = getValueCell(param.getValue(), COLUMN_POSITION);
    if (result.isBlank()) {
      return 0;
    } else {
      return Integer.parseInt(result);
    }
  }

  private Object getValueCellType(TreeTableColumn.CellDataFeatures<Issue, String> param) {
    return getValueCell(param.getValue(), COLUMN_TYPE);
  }

}

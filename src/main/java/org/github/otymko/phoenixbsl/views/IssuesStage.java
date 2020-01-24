package org.github.otymko.phoenixbsl.views;

import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.github.otymko.phoenixbsl.core.PhoenixAPI;
import org.github.otymko.phoenixbsl.core.PhoenixApp;
import org.github.otymko.phoenixbsl.entities.Issue;

import java.io.IOException;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class IssuesStage extends Stage {

  private Map<DiagnosticSeverity, String> severityToStringMap = createSeverityToStringMap();

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

  private double xOffset = 0;
  private double yOffset = 0;


  public IssuesStage() {

    Parent root = null;
    try {
      root = FXMLLoader.load(PhoenixApp.class.getResource("/IssuesStage.fxml"));
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    root.setOnMousePressed(new EventHandlerMouseEvent2());
    root.setOnMouseDragged(new EventHandlerMouseEvent());

    initStyle(StageStyle.UNDECORATED);

    Scene scene = new Scene(root);
    setScene(scene);

    scene.setFill(Color.TRANSPARENT);
    initStyle(StageStyle.TRANSPARENT);

    scene.lookup("#toolbar").setOnMouseDragged(new EventHandlerMouseEvent());
    scene.lookup("#toolbar").setOnMousePressed(new EventHandlerMouseEvent2());

    getIcons().add(new Image(PhoenixApp.class.getResourceAsStream("/phoenix.png")));
    Label title = (Label) scene.lookup("#titleApp");
    title.setText("Phoenix BSL v. " + PhoenixApp.getInstance().getVersionApp());

    Button btnClose = (Button) scene.lookup("#btnClose");
    btnClose.setOnAction(event -> {
      close();
    });

    Button btnRestore = (Button) scene.lookup("#btnRestore");
    btnRestore.setOnAction(event -> {
      setMaximized(!isMaximized());
    });

    Button btnMinimize = (Button) scene.lookup("#btnMinimize");
    btnMinimize.setOnAction(event -> {
      setIconified(true);
    });

    tree = (JFXTreeTableView) (TreeTableView<Issue>) scene.lookup("#issuesTree");
    tree.setPlaceholder(new Label("Замечаний нет"));

    TreeTableColumn<Issue, String> descriptionColumn = new TreeTableColumn<>("Описание");
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
    descriptionColumn.setCellValueFactory(
      param -> new SimpleStringProperty(param.getValue().getValue().getDescription()));

    TreeTableColumn<Issue, Integer> positionColumn = new TreeTableColumn<>("стр.");
    positionColumn.setPrefWidth(60);
    positionColumn.setMinWidth(60);
    positionColumn.setMaxWidth(60);
    positionColumn.setCellValueFactory(param -> new SimpleIntegerProperty(param.getValue().getValue().getStartLine()).asObject());
    positionColumn.setReorderable(false);
    positionColumn.setResizable(false);

    TreeTableColumn<Issue, String> typeColumn = new TreeTableColumn<>("Тип");
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

    search = (TextField) scene.lookup("#search");
    search.textProperty().addListener((o, oldVal, newVal) -> filterIssuesTree(newVal));

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

    FXCollections.sort(issues, Comparator.comparingInt(Issue::getStartLine));

    updateIndicators();

    recursiveTreeItem = new RecursiveTreeItem<>(issues, RecursiveTreeObject::getChildren);
    tree.setRoot(recursiveTreeItem);
    tree.setShowRoot(false);

    filterIssuesTree(search.getText());

    tree.refresh();

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


  class EventHandlerMouseEvent implements EventHandler<MouseEvent> {

    @Override
    public void handle(MouseEvent event) {
      setX(event.getScreenX() - xOffset);
      setY(event.getScreenY() - yOffset);
    }

  }

  class EventHandlerMouseEvent2 implements EventHandler<MouseEvent> {

    @Override
    public void handle(MouseEvent event) {
      xOffset = event.getSceneX();
      yOffset = event.getSceneY();
    }

  }

}

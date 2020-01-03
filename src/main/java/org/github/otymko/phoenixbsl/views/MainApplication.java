package org.github.otymko.phoenixbsl.views;

import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.eclipse.lsp4j.Diagnostic;
import org.github.otymko.phoenixbsl.entities.Issue;
import org.github.otymko.phoenixbsl.events.EventListener;

import java.util.List;

public class MainApplication extends Application implements EventListener {

  public Stage issuesStage;

  private JFXTreeTableView<Issue> tree = new JFXTreeTableView<>();
  private TreeItem<Issue> recursiveTreeItem;

  private int countError = 0;
  private int countWarning = 0;
  private int countInfo = 0;

  @Override
  public void start(Stage primaryStage) throws Exception {

    primaryStage.setTitle("123");
//    PhoenixApp.getInstance().getEventManager().subscribe(EventManager.EVENT_UPDATE_ISSUES, this);
   // initIssuesStage();

  }

//  public void updateIssuesStage() {
//    issuesStage.show();
//  }

  public void initIssuesStage() {

    issuesStage = new Stage();
    issuesStage.setTitle("Phoenix");

    JFXTreeTableColumn<Issue, String> descriptionColumn = new JFXTreeTableColumn<>("Description");
    descriptionColumn.setPrefWidth(300);
    descriptionColumn.setCellValueFactory(
      param -> new SimpleStringProperty(param.getValue().getValue().getDescription()));
    descriptionColumn.setReorderable(false);

    JFXTreeTableColumn<Issue, String> positionColumn = new JFXTreeTableColumn<>("стр.");
    positionColumn.setPrefWidth(50);
    positionColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getLocation()));
    positionColumn.setReorderable(false);

    JFXTreeTableColumn<Issue, String> typeColumn = new JFXTreeTableColumn<>("Тип");
    typeColumn.setPrefWidth(90);
    typeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getLocation()));
    typeColumn.setReorderable(false);


    ObservableList<Issue> issues = FXCollections.observableArrayList();


    recursiveTreeItem = new RecursiveTreeItem<>(issues, RecursiveTreeObject::getChildren);
    tree.setRoot(recursiveTreeItem);
    tree.setShowRoot(false);
    tree.setEditable(true);
    tree.getColumns().add(descriptionColumn);
    tree.getColumns().add(positionColumn);
    tree.getColumns().add(typeColumn);

    tree.setPrefSize(450, 520);

    GridPane main = new GridPane();
    main.setPadding(new Insets(10, 10, 10, 10));
    main.setHgap(20);
    main.setVgap(20);

    Label searchLabel = new Label("Поиск:");


    JFXTextField filterField = new JFXTextField();
    filterField.setPadding(new Insets(0, 0, 0, 10));
    filterField.setPrefWidth(420);
    filterField.textProperty().addListener((o, oldVal, newVal) -> {
      tree.setPredicate(userProp -> {
        final Issue issue = userProp.getValue();
        return issue.getDescription().toLowerCase().contains(newVal.toLowerCase())
//          || user.position.get().contains(newVal)
          || issue.getLocation().toLowerCase().contains(newVal.toLowerCase());
      });
    });

    GridPane searchPanel = new GridPane();
    searchPanel.add(searchLabel, 0, 0);
    searchPanel.add(filterField, 1, 0);

    main.add(searchPanel, 0, 0);
    main.add(tree, 0, 1);

    // сводка внизу
    GridPane infoPanel = new GridPane();
    infoPanel.setAlignment(Pos.CENTER);

    var labelError = new Label();
    labelError.setText("Ошибки: " + countError);
    infoPanel.add(labelError, 0, 0);
    labelError.setPadding(new Insets(0, 50, 10, 10));

    var labelWarning = new Label();
    labelWarning.setText("Предупреждения: " + countWarning);
    infoPanel.add(labelWarning, 1, 0);
    labelWarning.setPadding(new Insets(0, 50, 10, 10));

    var labelInfo = new Label();
    labelInfo.setText("Инфо: " + countInfo);
    infoPanel.add(labelInfo, 2, 0);
    labelInfo.setPadding(new Insets(0, 10, 10, 10));

    main.add(infoPanel, 0, 2);

    Scene scene = new Scene(main, 480, 600);
    issuesStage.setScene(scene);
    issuesStage.setResizable(false);
    //primaryStage.show();
  }


  @Override
  public void updateIssues(List<Diagnostic> diagnostics) {

//    var listModel = new ArrayList<Issue>();
    ObservableList<Issue> issues = FXCollections.observableArrayList();
    var lineOfset = 0;
    for (Diagnostic diagnostic : diagnostics) {

      var range = diagnostic.getRange();
      var position = range.getStart();
      var startLine = position.getLine() + 1 + lineOfset;
      var message = String.format("[%s]: %s", startLine, diagnostic.getMessage());

      Issue issue = new Issue();
      issue.setDescription(getHTMLText(message));
      issue.setStartLine(startLine);

      issues.add(issue); //addElement(issue);

//      if (diagnostic.getSeverity() == DiagnosticSeverity.Error) {
//        countError++;
//      } else if (diagnostic.getSeverity() == DiagnosticSeverity.Warning) {
//        countWarning++;
//      } else {
//        countInfo++;
//      }

    }

    recursiveTreeItem = new RecursiveTreeItem<Issue>(issues, RecursiveTreeObject::getChildren);
    tree.setRoot(recursiveTreeItem);
    tree.setShowRoot(false);


    if (!issuesStage.isShowing()) {
      issuesStage.show();
    }

  }

  private String getHTMLText(String inValue) {

    int length = 70;
    String result = "<html>" + breakLines(inValue, length) + "</html>";
    return result;
  }

  public String breakLines(String input, int maxWidth) {

    String[] arr = {};

    StringBuilder sb = new StringBuilder();
    int charCount = 0;
    for (String word : input.split("\\s")) {
      if (charCount > 0) {
        if (charCount + word.length() + 1 > maxWidth) {
          charCount = 0;
          sb.append("<br>");
        } else {
          charCount++;
          sb.append(' ');
        }
      }
      charCount += word.length();
      sb.append(word);
    }
    String res = sb.toString();
    return res;
  }

  public static void main(String[] args) {
    launch(args);
  }

}

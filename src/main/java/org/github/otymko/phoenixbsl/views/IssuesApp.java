package org.github.otymko.phoenixbsl.views;

import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class IssuesApp extends Application {

  private JFXTreeTableView<CustomIssue> tree = new JFXTreeTableView<>();

  private int countError = 0;
  private int countWarning = 0;
  private int countInfo = 0;

  @Override
  public void start(Stage primaryStage) throws Exception {

    primaryStage.setTitle("Phoenix");

    JFXTreeTableColumn<CustomIssue, String> descriptionColumn = new JFXTreeTableColumn<>("Description");
    descriptionColumn.setPrefWidth(300);
    descriptionColumn.setCellValueFactory(param -> param.getValue().getValue().description);
    descriptionColumn.setReorderable(false);

    JFXTreeTableColumn<CustomIssue, String> positionColumn = new JFXTreeTableColumn<>("стр.");
    positionColumn.setPrefWidth(50);
    positionColumn.setCellValueFactory(param -> param.getValue().getValue().position);
    positionColumn.setReorderable(false);

    JFXTreeTableColumn<CustomIssue, String> typeColumn = new JFXTreeTableColumn<>("Тип");
    typeColumn.setPrefWidth(90);
    typeColumn.setCellValueFactory(param -> param.getValue().getValue().type);
    typeColumn.setReorderable(false);


    ObservableList<CustomIssue> issues = FXCollections.observableArrayList();

    final TreeItem<CustomIssue> root = new RecursiveTreeItem<>(issues, RecursiveTreeObject::getChildren);
    tree.setRoot(root);
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
        final CustomIssue issue = userProp.getValue();
        return issue.description.get().toLowerCase().contains(newVal.toLowerCase())
//          || user.position.get().contains(newVal)
          || issue.type.get().toLowerCase().contains(newVal.toLowerCase());
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
    primaryStage.setScene(scene);
    primaryStage.setResizable(false);
    primaryStage.show();

  }

  public void updateIssue() {

  }

  public static void main(String[] args) {

    launch();

  }

}

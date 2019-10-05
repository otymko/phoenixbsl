package org.github.otymko.phoenixbsl.views;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.github.otymko.phoenixbsl.App;
import org.github.otymko.phoenixbsl.entities.Issue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class IssuesForm extends JFrame {

  private App app;
  private DefaultListModel<Issue> listModel = new DefaultListModel<>();
  private JList<Issue> issuesList;

//  private JTextField search = new JTextField();

  private JLabel labelError = new JLabel();
  private JLabel labelWarning = new JLabel();
  private JLabel labelInfo = new JLabel();

  private int countError = 0;
  private int countWarning = 0;
  private int countInfo = 0;

  private static final String DEFAULT_TITLE = "Замечания";
  private static Color color = new java.awt.Color(68,68,68);

  public IssuesForm() {
    super(DEFAULT_TITLE);
    this.app = App.getInstance();

    toFront();
    setSize(500, 600);
//    setUndecorated(true);
    initComponents();
    updateSummary();

  }

  private void initComponents() {

    var container = getContentPane();
    var boxLayout = new BoxLayout(container, BoxLayout.Y_AXIS);
    container.setLayout(boxLayout);

//    var containerSearch = new Container();
//    containerSearch.setSize(500, 100);
//    containerSearch.setLayout(new BoxLayout(containerSearch, BoxLayout.X_AXIS));
//    containerSearch.add(search);
//    container.add(containerSearch);


    issuesList = new JList<>(listModel);
    issuesList.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        var list = (JList)evt.getSource();
        if (evt.getClickCount() == 2) {
          var issue = (Issue) ((JList) evt.getSource()).getSelectedValue();
          if (issue == null){
            return;
          }
          app.focusDocumentLine(issue.getStartLine());
        }
      }
    });
    issuesList.setSize(500, 500);
    var pane = new JScrollPane(issuesList);
    pane.setSize(500, 500);
    container.add(pane);

    var containerLabel = new Container();
    containerLabel.setSize(500, 100);
    containerLabel.setLayout(new BoxLayout(containerLabel, BoxLayout.X_AXIS));
    containerLabel.add(labelError);
    containerLabel.add(labelWarning);
    containerLabel.add(labelInfo);
    container.add(containerLabel);

  }

  public void updateSummary() {
    labelError.setText(String.format("Ошибки: %s ", countError));
    labelWarning.setText(String.format("Предупреждений: %s ", countWarning));
    labelInfo.setText(String.format("Информации: %s ", countInfo));
  }

  public void onVisible() {
    setVisible(true);
    setAlwaysOnTop(true);
  }

  public void updateIssues(List<Diagnostic> list) {
    initList(list);
    issuesList.setModel(listModel);

    updateSummary();
  }

  public void initList(List<Diagnostic> list) {

    clearFormData();

    for (Diagnostic diagnostic : list) {

      String message = getHTMLText(diagnostic.getMessage());

      Issue issue = new Issue();
      issue.setDiscription(message);
      Range range = diagnostic.getRange();
      Position position = range.getStart();
      String location = String.format("[%s, %s]", position.getLine() + 1, position.getCharacter() + 1);
      issue.setLocation(location);
      issue.setStartLine(position.getLine() + 1);
      listModel.addElement(issue);

      if (diagnostic.getSeverity() == DiagnosticSeverity.Error) {
        countError++;
      } else if (diagnostic.getSeverity() == DiagnosticSeverity.Warning) {
        countWarning++;
      } else {
        countInfo++;
      }
    }
  }

  private void clearFormData() {
    listModel.clear();
    countError = 0;
    countInfo = 0;
    countWarning = 0;
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
    for(String word : input.split("\\s")) {
      if(charCount > 0) {
        if(charCount + word.length() + 1 > maxWidth) {
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

}

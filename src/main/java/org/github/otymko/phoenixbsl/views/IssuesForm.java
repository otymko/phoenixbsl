package org.github.otymko.phoenixbsl.views;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.github.otymko.phoenixbsl.App;
import org.github.otymko.phoenixbsl.entities.Issue;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class IssuesForm extends JFrame {

  private App app;
  private DefaultListModel<Issue> listModel = new DefaultListModel<>();
  private JList<Issue> issuesList;

  public IssuesForm() {
    super("Замечания");
    this.app = App.getInstance();
    setBounds(100,100,500,600);
    issuesList = new JList<>(listModel);
    issuesList.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        JList list = (JList)evt.getSource();
        if (evt.getClickCount() == 2) {

          Issue issue = (Issue) ((JList) evt.getSource()).getSelectedValue();
          if (issue == null){
            return;
          }
          app.focusDocumentLine(issue.getStartLine());

        }
      }
    });
    add(new JScrollPane(issuesList));
    toFront();
  }

  public void onVisible() {
    setVisible(true);
    setAlwaysOnTop(true);
  }

  public void updateIssues(List<Diagnostic> list) {
    initList(list);
    issuesList.setModel(listModel);
  }

  public void initList(List<Diagnostic> list) {
    listModel.clear();
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

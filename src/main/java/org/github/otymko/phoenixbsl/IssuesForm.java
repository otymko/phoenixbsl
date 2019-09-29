package org.github.otymko.phoenixbsl;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import javax.swing.*;
import java.util.List;

public class IssuesForm extends JFrame {

  private MainApp app;
  private DefaultListModel<Issue> listModel = new DefaultListModel<>();

  public IssuesForm(MainApp app, List<Diagnostic> list) {
    super("Issues");

    this.app = app;

    this.setBounds(100,100,500,600);
    initList(list);
    JList<Issue> issuesList = new JList<>(listModel);
    issuesList.addListSelectionListener(e -> {

      Issue issue = (Issue) ((JList) e.getSource()).getSelectedValue();
      app.focusDocumentLine(issue.getStartLine());

    });
    //issuesList.setCellRenderer(new IssueRenderer());
    add(new JScrollPane(issuesList));
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

  @Override
  public void dispose() {
    super.dispose();
    System.exit(0);
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

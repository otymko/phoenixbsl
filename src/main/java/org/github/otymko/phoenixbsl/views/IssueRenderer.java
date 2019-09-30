package org.github.otymko.phoenixbsl.views;

import org.github.otymko.phoenixbsl.entities.Issue;

import javax.swing.*;
import java.awt.*;

public class IssueRenderer extends JLabel implements ListCellRenderer<Issue> {

  public IssueRenderer() {
    setOpaque(true);
  }

  @Override
  public Component getListCellRendererComponent(JList<? extends Issue> list, Issue value,
                                                int index, boolean isSelected, boolean cellHasFocus) {

    this.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
    this.setText(value.getDiscription());
    return this;
  }
}

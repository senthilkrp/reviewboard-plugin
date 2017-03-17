package com.senthil.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import java.awt.*;
import javax.swing.*;
import org.jetbrains.annotations.Nullable;


/**
 * Created by spanneer on 2/22/17.
 */
public class PublishDialog extends DialogWrapper {

  private FormBuilder formBuilder = FormBuilder.createFormBuilder();

  JBCheckBox shipItCheckBox = new JBCheckBox("Ship it");
  JTextArea commentArea = new JTextArea();

  protected PublishDialog(@Nullable Project project) {
    super(project);
    initialize();
    setTitle("Publish Review");
    init();
    pack();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    JPanel panel = formBuilder.getPanel();
    panel.setMinimumSize(new Dimension(400, 200));
    return panel;
  }

  private void initialize() {
    formBuilder = FormBuilder.createFormBuilder();
    formBuilder.addComponent(new JBLabel("Comments:"));
    commentArea.setRows(5);
    formBuilder.addComponent(commentArea);
    formBuilder.addComponent(shipItCheckBox);
  }

  public boolean hasShipIt() {
    return shipItCheckBox.isSelected();
  }

  public String getComments() {
    return commentArea.getText();
  }
}

package com.senthil.state;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Comparing;
import com.senthil.net.ReviewFactory;
import com.senthil.ui.panels.LoginPanel;
import javax.swing.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;


public class SettingsPage implements Configurable {

  public static final String SETTINGS_DISPLAY_NAME = "Offline Review";

  private LoginPanel loginPanel = new LoginPanel();
  private Configuration oldConfigurationState;
  private Project project;

  public SettingsPage(Project project) {
    this.project = project;
  }

  @Nls
  @Override
  public String getDisplayName() {
    return SETTINGS_DISPLAY_NAME;
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return null;
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    oldConfigurationState = ConfigurationPersistence.getInstance(project).getState();
    if (oldConfigurationState != null) {
      loginPanel.setUsername(oldConfigurationState.getUsername());
      loginPanel.setPassword(oldConfigurationState.getPassword());
      loginPanel.setReviewBoardUrl(oldConfigurationState.getReviewBoardUrl());
      loginPanel.setRepository(oldConfigurationState.getRepository());
      loginPanel.refresh();
    }
    loginPanel.addActionListener(e -> testConnection());
    return loginPanel.getPanel();
  }

  @Override
  public boolean isModified() {
//    if (oldConfigurationState == null) {
//      return !loginPanel.getUsername().isEmpty() || !loginPanel.getPassword()
//          .isEmpty() || loginPanel.getRepository() != null;
//    }
//    return !Comparing.equal(loginPanel.getUsername(), oldConfigurationState.getUsername()) || !Comparing.equal(
//        loginPanel.getPassword(), oldConfigurationState.getPassword()) || !Comparing.equal(
//        loginPanel.getReviewBoardUrl(), oldConfigurationState.getReviewBoardUrl());
    return true;
  }

  @Override
  public void apply() throws ConfigurationException {
    PropertiesComponent.getInstance(project).setValue("repoId", loginPanel.getRepository().getId(), -1);
    Configuration configuration = new Configuration(loginPanel.getUsername(), loginPanel.getPassword(), loginPanel.getReviewBoardUrl(), loginPanel.getRepository());
    ReviewFactory.getInstance().setCredentials(loginPanel.getUsername(), loginPanel.getPassword(), loginPanel.getReviewBoardUrl());
    ConfigurationPersistence.getInstance(project).loadState(configuration);

  }

  @Override
  public void reset() {

  }

  @Override
  public void disposeUIResources() {

  }

  private void testConnection() {
        if (StringUtils.isEmpty(loginPanel.getUsername())
                || StringUtils.isEmpty(loginPanel.getPassword())) {
            Messages.showErrorDialog(project, "Connection information provided is invalid.", "Invalid Settings");
            return;
        }

    try {
      ReviewFactory.getInstance().testConnection(loginPanel.getUsername(), loginPanel.getPassword(), loginPanel.getReviewBoardUrl());
      Messages.showErrorDialog(project, "Connection Successful",
          "Success");
    } catch (Exception e) {
      e.printStackTrace();
      Messages.showErrorDialog(project, "Invalid Credentials", "Could not Authenticate with the Rb Server.");
    }
  }
}

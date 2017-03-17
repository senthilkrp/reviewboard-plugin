package com.senthil.components;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.senthil.net.Constants;
import com.senthil.net.ReviewFactory;
import com.senthil.state.Configuration;
import com.senthil.state.ConfigurationPersistence;
import com.senthil.state.SettingsPage;
import com.senthil.utils.Utils;
import org.jetbrains.annotations.NotNull;


/**
 * Created by spanneer on 1/14/17.
 */
public class OfflineReviewBoard extends AbstractProjectComponent {

  private RbWidget widget;

  public OfflineReviewBoard(@NotNull Project project) {
    super(project);
  }

  @Override
  public void projectOpened() {

    Configuration configuration = ConfigurationPersistence.getInstance(myProject).getState();
    if (configuration != null) {
      ReviewFactory.getInstance().setCredentials(configuration.getUsername(), configuration.getPassword(), configuration.getReviewBoardUrl());
    }else {
      ShowSettingsUtil.getInstance().editConfigurable(myProject, new SettingsPage(myProject));
    }

    int repoId = Utils.getRepoId(myProject);

    if (repoId == -1) {
      StudioNotification.getInstance(myProject)
          .showNotification(Constants.REVIEW_NOTIFICATION_GROUP, "Error",
              "Cant find repository for " + Utils.getProductName(myProject), NotificationType.ERROR, null);
    }

    StatusBar statusBar = WindowManager.getInstance().getStatusBar(myProject);
    widget = new RbWidget(myProject);
    if (statusBar != null) {
      statusBar.addWidget(widget, myProject);
    }
  }

  @Override
  public void projectClosed() {
    StatusBar statusBar = WindowManager.getInstance().getStatusBar(myProject);
    if (statusBar != null) {
      statusBar.removeWidget(widget.ID());
    }
  }

  @NotNull
  @Override
  public String getComponentName() {
    return "Review Board";
  }
}

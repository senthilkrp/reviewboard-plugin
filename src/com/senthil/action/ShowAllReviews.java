package com.senthil.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.senthil.model.ReviewRequests;
import com.senthil.net.ReviewFactory;
import com.senthil.ui.ReviewPanel;
import com.senthil.utils.Utils;
import java.util.concurrent.ExecutionException;


public class ShowAllReviews extends AnAction {
  private final Integer repoId;

  public ShowAllReviews(Integer repoId) {
    super("Show All Reviews");
    this.repoId = repoId;
  }

  @Override
    public void actionPerformed(AnActionEvent e) {
      ApplicationManager.getApplication().invokeLater(() -> {
        ReviewRequests reviews;
        try {
          reviews = ReviewFactory.getInstance().reviewRequests(repoId).get();
          Utils.showToolWindow(e.getProject(), new ReviewPanel(e.getProject(), reviews), "");
        } catch (InterruptedException | ExecutionException e1) {
          e1.printStackTrace();
        }
      });
    }
}

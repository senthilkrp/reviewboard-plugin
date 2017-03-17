package com.senthil.ui;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.chains.SimpleDiffRequestChain;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.LocalFilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.senthil.components.StudioNotification;
import com.senthil.model.Diff;
import com.senthil.model.ReviewFile;
import com.senthil.model.ReviewFiles;
import com.senthil.model.Comments;
import com.senthil.model.RBChange;
import com.senthil.model.ReviewRequest;
import com.senthil.net.Constants;
import com.senthil.net.ReviewFactory;
import com.senthil.utils.OfflineUtils;
import com.senthil.ui.toolswindow.ReviewChangesTreeList;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.swing.*;
import org.jetbrains.annotations.CalledInAwt;


/**
 * Created by spanneer on 2/3/17.
 */
public class SummaryPanel extends JBPanel {

  private final Project project;
  private ReviewRequest review;
  //  private JBList<RBFileDiff.File> filesList = new JBList<>(JBList.createDefaultListModel());
  private ReviewChangesTreeList filesList;
  private RangeSlider rangeSlider = new RangeSlider();
  private JComponent toolbar;

  public SummaryPanel(Project project) {
    this.project = project;
    add(new JLabel("Summary for review"));
    filesList = new ReviewChangesTreeList(project, new ArrayList());
    setLayout(new BorderLayout());
    JBPanel toolbarContainer = new JBPanel();
    toolbarContainer.setLayout(new FlowLayout(FlowLayout.LEFT));
    toolbarContainer.add(createToolbar().getComponent());
    toolbarContainer.add(rangeSlider);
    add(toolbarContainer, BorderLayout.NORTH);
    rangeSlider.setPaintLabels(true);
    rangeSlider.setMinimum(0);
    rangeSlider.setMaximum(1);
    rangeSlider.setSnapToTicks(true);
    rangeSlider.setMajorTickSpacing(1);
    rangeSlider.setPreferredSize(new Dimension(240, rangeSlider.getPreferredSize().height));
    rangeSlider.setValue(0);
    rangeSlider.setUpperValue(1);
    rangeSlider.setFocusable(false);
    rangeSlider.setVisible(false);
    rangeSlider.addChangeListener(e -> {
      if (!rangeSlider.isEnabled() || rangeSlider.getValueIsAdjusting()) {
        return;
      }
      rangeSlider.setEnabled(false);
      if (rangeSlider.getValue() == 0) {
        ReviewFactory.getInstance().files(review.getId(), rangeSlider.getUpperValue()).thenAccept(filesList -> {
          showFiles(filesList.asList());
        });
      } else {
        CompletableFuture<ReviewFiles> minFuture =
            ReviewFactory.getInstance().files(review.getId(), rangeSlider.getValue());
        CompletableFuture<ReviewFiles> maxFuture =
            ReviewFactory.getInstance().files(review.getId(), rangeSlider.getUpperValue());

        minFuture.thenAcceptBothAsync(maxFuture, (ReviewFiles minFiles, ReviewFiles maxFiles) -> {
          List<? extends ReviewFile> originalFiles = minFiles.asList();
          List<? extends ReviewFile> patchFiles = maxFiles.asList();
          patchFiles.removeAll(originalFiles);
          Map<String, ReviewFile> map = minFiles.asMap();

          final java.util.List<Change> changes = new ArrayList<>();
          patchFiles.forEach(file -> {
            ReviewFile originalFile = map.get(file.getSourceFile());
            FilePath srcFilePath;
            String content;
            if (originalFile != null) {
              srcFilePath = new LocalFilePath(originalFile.getDestinationFile(), false);
              content = originalFile.getPatchedFile();
            } else {
              srcFilePath = new LocalFilePath(file.getSourceFile(), false);
              content = file.getOriginalFile();
            }

            FilePath destFilePath = new LocalFilePath(file.getDestinationFile(), false);

            LazyContentRevision original =
                new LazyContentRevision(ReviewFactory.getInstance().contents(content), srcFilePath,
                    originalFile.getDiffRevision());
            LazyContentRevision patched =
                new LazyContentRevision(ReviewFactory.getInstance().contents(file.getPatchedFile()), destFilePath,
                    file.getDiffRevision());
            changes.add(new RBChange(original, patched, file));
          });
          ApplicationManager.getApplication().invokeLater(() -> {
            filesList.setChangesToDisplay(changes);
            rangeSlider.setEnabled(true);
          });
        });
      }
    });
    add(new JBScrollPane(filesList), BorderLayout.CENTER);

    filesList.setDoubleClickHandler(new Runnable() {
      @Override
      @CalledInAwt
      public void run() {
        RBChange change = (RBChange) filesList.getSelectedChanges().get(0);
        SimpleDiffRequest request = getRequest(change, project);
        if (request == null) {
          return;
        }
        CommentDiffWindow window =
            new CommentDiffWindow(project, new SimpleDiffRequestChain(request), DiffDialogHints.FRAME);
        window.show();
      }
    });
  }

  public CompletableFuture<Void> setReview(ReviewRequest review) {

    this.review = review;
    rangeSlider.setVisible(false);
    rangeSlider.setEnabled(false);
    filesList.setChangesToDisplay(Collections.emptyList());
    return refresh();
  }

  private CompletableFuture<Void> refresh() {
    CompletableFuture<Void> future = new CompletableFuture<Void>();
    ReviewFactory.getInstance()
        .diffs(review.getId())
        .thenAccept(diffs -> diffs.stream().map(Diff::getRevision).max(Comparator.naturalOrder()).ifPresent(t -> {
          ApplicationManager.getApplication().invokeAndWait(() -> {
            rangeSlider.setMaximum(t);
            rangeSlider.setUpperValue(t);
            rangeSlider.setEnabled(true);

            ReviewFactory.getInstance().files(review.getId(), rangeSlider.getUpperValue()).thenAccept(filesList -> {
              showFiles(filesList.asList());
              future.complete(null);
            });
          });
        }));
    return future;
  }

  private void showFiles(List<? extends ReviewFile> fileList) {
    final java.util.List<Change> changes = new ArrayList<>();
    fileList.forEach(file -> {
      FilePath srcFilePath = new LocalFilePath(file.getSourceFile(), false);
      FilePath destFilePath = new LocalFilePath(file.getDestinationFile(), false);

      LazyContentRevision original =
          new LazyContentRevision(ReviewFactory.getInstance().contents(file.getOriginalFile()), srcFilePath,
              file.getDiffRevision());
      LazyContentRevision patched =
          new LazyContentRevision(ReviewFactory.getInstance().contents(file.getPatchedFile()), destFilePath,
              file.getDiffRevision());
      changes.add(new RBChange(original, patched, file));
    });
    ApplicationManager.getApplication().invokeAndWait(() -> {
      filesList.setChangesToDisplay(changes);
      rangeSlider.setVisible(true);
      rangeSlider.setEnabled(true);
    });
  }

  private ActionToolbar createToolbar() {
    DefaultActionGroup group = new DefaultActionGroup();
    group.add(new ReviewAction());
    group.add(new PublishAction());
    ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, true);
    actionToolbar.setLayoutPolicy(ActionToolbar.AUTO_LAYOUT_POLICY);
    return actionToolbar;
  }

  class ChangeDiffVersionAction extends AnAction implements DumbAware {

    @Override
    public void actionPerformed(AnActionEvent e) {

    }
  }

  class ReviewAction extends AnAction implements DumbAware {

    public ReviewAction() {
      super("Review", "Review changes", Icons.Review);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
      try {
        List<Change> changes = filesList.getSelectedChanges();
        List<SimpleDiffRequest> requests = new ArrayList<>();
        for (Change change : changes) {
          requests.add(getRequest(change, e.getProject()));
        }
        CommentDiffWindow window =
            new CommentDiffWindow(e.getProject(), new SimpleDiffRequestChain(requests), DiffDialogHints.FRAME);
        window.show();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  private SimpleDiffRequest getRequest(Change change, Project project) {
    try {
      FileType fileType =
          FileTypeManager.getInstance().getFileTypeByFileName(((RBChange) change).getFile().getSourceFile());
      String cont1 = change.getBeforeRevision().getContent();
      DocumentContent content1 = DiffContentFactory.getInstance().create(project, cont1 == null ? "" : cont1, fileType);
      String cont2 = change.getAfterRevision().getContent();
      DocumentContent content2 = DiffContentFactory.getInstance().create(project, cont2 == null ? "" : cont2, fileType);
      SimpleDiffRequest request =
          new SimpleDiffRequest(((RBChange) change).getFile().getDestinationFile(), content1, content2,
              change.getBeforeRevision().getRevisionNumber().asString(),
              change.getAfterRevision().getRevisionNumber().asString());
      request.putUserData(Constants.COMMENTS_KEY, ReviewFactory.getInstance()
          .comments(SummaryPanel.this.review.getId(),
              Integer.valueOf(change.getBeforeRevision().getRevisionNumber().asString()),
              ((RBChange) change).getFile().getId()));
      request.putUserData(Constants.FILE_KEY, ((RBChange) change).getFile());
      request.putUserData(Constants.REVIEW, SummaryPanel.this.review);
      return request;
    } catch (VcsException e) {
      e.printStackTrace();
      return null;
    }
  }

  class PublishAction extends AnAction implements DumbAware {

    public PublishAction() {
      super("Submit", "Submit current review", Icons.UPLOAD);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {

      try {
        PublishDialog dialog = new PublishDialog(SummaryPanel.this.project);
        if (dialog.showAndGet()) {
          File drafts = new File("unchecked" + File.separator + SummaryPanel.this.review.getId());

          File[] savedComments = OfflineUtils.listFiles(drafts, (dir, name) -> name.endsWith("_comments.json"));

          if (savedComments == null) {
            savedComments = new File[0];
          }
          List<Comments> draftReviewFiles = Arrays.stream(savedComments).map(file -> {
            try {
              return OfflineUtils.read(file.getPath(), Comments.class);
            } catch (FileNotFoundException e1) {
              return null;
            }
          }).filter(Objects::nonNull).collect(Collectors.toList());

          ReviewFactory.getInstance()
              .submitReview(SummaryPanel.this.review, draftReviewFiles, dialog.hasShipIt(), dialog.getComments())
              .whenComplete((reviewId, ex) -> {
                if (reviewId != null) {
                  StudioNotification.getInstance(project)
                      .showNotification(Constants.REVIEW_NOTIFICATION_GROUP, "Success",
                          "Review published with id " + reviewId, NotificationType.INFORMATION, null);
                  System.out.println("Published review with id " + reviewId);
                  //delete the files
                  boolean isDeleted = OfflineUtils.delete(drafts);
                  assert isDeleted;
                } else {
                  StudioNotification.getInstance(project)
                      .showNotification(Constants.REVIEW_NOTIFICATION_GROUP, "Error",
                          "Error publishing review. Check your username/password in Preferences->Other Settings.",
                          NotificationType.ERROR, null);
                  System.out.println("Error publishing review");
                  ex.printStackTrace();
                }
              });
        }
      } catch (Exception ex) {
        System.out.println("Could not publish review");
      }
    }
  }
}


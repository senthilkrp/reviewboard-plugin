package com.senthil.components;

import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.ide.actions.GotoActionBase;
import com.intellij.ide.util.gotoByName.ChooseByNameFilter;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.ide.util.gotoByName.ListChooseByNameModel;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.popup.PopupFactoryImpl;
import com.intellij.util.Consumer;
import com.intellij.util.ImageLoader;
import com.senthil.action.ShowAllReviews;
import com.senthil.model.Comments;
import com.senthil.model.FileContent;
import com.senthil.model.ReviewRequest;
import com.senthil.model.ReviewRequests;
import com.senthil.net.Constants;
import com.senthil.net.ReviewDataProvider;
import com.senthil.net.ReviewFactory;
import com.senthil.state.Configuration;
import com.senthil.state.ConfigurationPersistence;
import com.senthil.state.SettingsPage;
import com.senthil.ui.toolswindow.ReviewChangesTreeList;
import com.senthil.util.TextIcon;
import com.senthil.utils.Utils;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import org.jetbrains.annotations.CalledInAwt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.util.ui.SwingHelper.*;


/**
 * Created by spanneer on 1/14/17.
 */
class RbWidget extends EditorBasedWidget implements StatusBarWidget.IconPresentation, StatusBarWidget.MultipleTextValuesPresentation {

  private final ReviewDataProvider reviewDataProvider;
  private final WidgetState syncState;
  private final IdleState offlineState;

  private Date lastUpdateTime = null;

  private WidgetState activeWidgetState;
  private IdleState idleState;
  private WidgetState errorState;
  private WidgetState pausedState;
  private ReviewRequests reviewRequests;

  public RbWidget(@NotNull Project project) {
    super(project);
    activeWidgetState = (new WidgetState(project, AllIcons.General.Ellipsis, "Please Wait.."));
    errorState = new WidgetState(project, AllIcons.General.Error, "Click here to configure review board", p -> {
      SettingsPage settingsPage = new SettingsPage(p);
      if (ShowSettingsUtil.getInstance().editConfigurable(p, settingsPage)) {
        try {
          settingsPage.apply();
        } catch (ConfigurationException e) {
          e.printStackTrace();
        }
      }
    });
    pausedState = new WidgetState(project, AllIcons.General.InspectionsPause, "Paused");
    syncState = new WidgetState(project, AllIcons.Actions.Refresh, "Syncing with server");
    idleState = new IdleState(project, "rb_online.png", this::sync);
    offlineState = new IdleState(project, "rb_offline.png", null);
    reviewDataProvider = ReviewFactory.getInstance();
  }

  private void sync() {
    setState(syncState);
    List<CompletableFuture> futures = new ArrayList<>();
    CompletableFuture.runAsync(() -> {
      reviewRequests.forEach(reviewRequest -> {
        reviewDataProvider.diffs(reviewRequest.getId()).thenAcceptAsync(diffs -> {
          diffs.forEach(diff -> {
            reviewDataProvider.files(reviewRequest.getId(), diff.getRevision()).thenAcceptAsync(reviewFiles -> {
              reviewFiles.forEach(reviewFile -> {
                CompletableFuture<Comments> commentsFuture =
                    reviewDataProvider.comments(reviewRequest.getId(), diff.getRevision(), reviewFile.getId());
                CompletableFuture<FileContent> patchedFuture = reviewDataProvider.contents(reviewFile.getPatchedFile());
                CompletableFuture<FileContent> originalFuture =
                    reviewDataProvider.contents(reviewFile.getOriginalFile());
                futures.add(commentsFuture);
                futures.add(patchedFuture);
                futures.add(originalFuture);
              });
            });
          });
        });
      });
    });
  }

  @Override
  public void install(@NotNull StatusBar statusBar) {
    super.install(statusBar);
    idleState.install(statusBar);
    offlineState.install(statusBar);
    java.util.Timer timer = new java.util.Timer();

    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        if (activeWidgetState == syncState) {
          return;
        }
        int repoId = Utils.getRepoId(myProject);
        if (repoId == -1) {
          setState(errorState);
          return;
        }

        setState(syncState);

        if (lastUpdateTime == null) {
          lastUpdateTime = ConfigurationPersistence.getInstance(getProject()).getState() == null ? new Date(0)
              : ConfigurationPersistence.getInstance(getProject()).getState().getLastAccessTime();
        }
        reviewDataProvider.reviewRequests(repoId).thenAccept(reviewRequests -> {
          RbWidget.this.reviewRequests = reviewRequests;
          if (reviewRequests == null) {
            setState(errorState);
          } else if (reviewRequests.isStale()) {
            setState(offlineState);
            offlineState.setReviewRequests(reviewRequests.asList());
          } else {
            setState(idleState);
            idleState.setReviewRequests(reviewRequests.asList());
            List<ReviewRequest> newReviews = reviewRequests.stream()
                .filter(req -> req.isPending() && req.getLastUpdatedTime().after(lastUpdateTime))
                .collect(Collectors.toList());

            if (newReviews.size() > 0) {
              lastUpdateTime =
                  newReviews.stream().map(ReviewRequest::getLastUpdatedTime).max(Comparator.naturalOrder()).get();
              StudioNotification.getInstance(myProject)
                  .showNotification(Constants.REVIEW_NOTIFICATION_GROUP, "New Review",
                      "You have " + newReviews.size() + " new review(s)", NotificationType.INFORMATION,
                      new NotificationListener.Adapter() {
                        @Override
                        protected void hyperlinkActivated(@NotNull Notification notification,
                            @NotNull HyperlinkEvent e) {
                          System.out.println("clicked");
                        }
                      });
            }
          }
        }).exceptionally(ex -> {
          System.out.println("Error ");
          ex.printStackTrace();
          setState(errorState);
          return null;
        });
      }
    }, 2000, Constants.REFERESH_TIME);
  }

  @CalledInAwt
  public synchronized void setState(WidgetState state) {
    activeWidgetState = state;
    if (myStatusBar != null) {
      myStatusBar.updateWidget(ID());
    }
  }

  @Override
  public void dispose() {
    ConfigurationPersistence persistence = ConfigurationPersistence.getInstance(myProject);
    if (persistence == null) {
      return;
    }
    Configuration configuration = persistence.getState();
    if (configuration == null) {
      configuration = new Configuration();
    }
    configuration.setLastAccessTime(lastUpdateTime);
    persistence.loadState(configuration);
    super.dispose();
  }

  @Nullable
  @Override
  public String getTooltipText() {
    return activeWidgetState.getTooltipText();
  }

  @Nullable
  @Override
  public Consumer<MouseEvent> getClickConsumer() {
    return mouseEvent -> {
      if ((mouseEvent.getModifiers() & InputEvent.SHIFT_MASK) != 0) {
        toggleState();
      } else {
        activeWidgetState.getClickConsumer().consume(mouseEvent);
      }
    };
  }

  private void toggleState() {
    if (activeWidgetState == idleState) {
      setState(pausedState);
    } else if (activeWidgetState == pausedState) {
      setState(idleState);
    }
  }

  @Override
  public ListPopup getPopupStep() {
    return null;
  }

  @Override
  public String getSelectedValue() {
    return null;
  }

  @NotNull
  @Override
  public String getMaxValue() {
    return "";
  }

  @NotNull
  @Override
  public Icon getIcon() {
    return activeWidgetState.getIcon();
  }

  @NotNull
  @Override
  public String ID() {
    return "Review Board Widget";
  }

  @Nullable
  @Override
  public WidgetPresentation getPresentation(@NotNull PlatformType type) {
    return this;
  }
}

class ReviewAction extends AnAction implements DumbAware {

  private final ReviewRequest review;
  private final ReviewDataProvider provider;
  private ReviewChangesTreeList changesTree;

  public ReviewAction(ReviewDataProvider provider, ReviewRequest review) {
    super(review.getDescription().length() > 30 ? review.getDescription().substring(0, 30) + ELLIPSIS
        : review.getDescription());
    this.review = review;
    this.provider = provider;
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    try {
      Utils.showReview(e.getProject(), review);
    } catch (Exception e1) {
      e1.printStackTrace();
    }
  }
}

class ReviewGroup extends ActionGroup {
  private final ReviewDataProvider dataProvider;
  private final List<ReviewRequest> reviewRequests;
  private final Project project;
  private final Runnable runnable;

  ReviewGroup(Project project, ReviewDataProvider dataProvider, List<ReviewRequest> reviewRequests, Runnable runnable) {
    this.dataProvider = dataProvider;
    if(reviewRequests != null && reviewRequests.size() > 5) {
      this.reviewRequests = reviewRequests.subList(0, 5);
    }else  {
      this.reviewRequests = reviewRequests;
    }

    this.project = project;
    this.runnable = runnable;
  }

  @NotNull
  @Override
  public AnAction[] getChildren(@Nullable AnActionEvent e) {

    AnAction[] actions = new AnAction[reviewRequests.size() + 6];
    int i = 0;
    for (; i < reviewRequests.size(); i++) {
      actions[i] = new ReviewAction(dataProvider, reviewRequests.get(i));
    }
    actions[i++] = new Separator();
    actions[i++] = new SearchReviewAction();
    actions[i++] = new Separator();
    actions[i++] = new ShowAllReviews(Utils.getRepoId(project));
    actions[i++] = new Separator();
    actions[i] = new AnAction("Sync") {

      @Override
      public void actionPerformed(AnActionEvent e) {
        runnable.run();
      }
    };
    return actions;
  }

  class SearchReviewAction extends GotoActionBase implements DumbAware {

    public SearchReviewAction() {
      getTemplatePresentation().setText("Search");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
      final Project project = e.getProject();
      if (project == null) {
        return;
      }
      myInAction = getClass();

      GotoActionCallback<FileType> callback = new GotoActionCallback<FileType>() {
        @Override
        protected ChooseByNameFilter<FileType> createFilter(@NotNull ChooseByNamePopup popup) {
          return null;
        }

        @Override
        public void elementChosen(final ChooseByNamePopup popup, final Object element) {
          if (element != null && element instanceof ReviewRequest) {
            Utils.showReview(project, (ReviewRequest) element);
          }
        }
      };

      ChooseByNamePopup popup = ChooseByNamePopup.createPopup(e.getProject(),
          new ListChooseByNameModel<>(e.getProject(), "Search Review Board", "Review Not Found",
              Collections.<ReviewRequest>emptyList()), new RBFileProvider(e.getProject()), null, false, 0);
      showNavigationPopup(callback, "Search Review Board", popup);
    }

    @Override
    protected void gotoActionPerformed(AnActionEvent e) {
      e.getPresentation().setText("Search...");
      e.getPresentation().setEnabledAndVisible(true);
    }
  }
}

class IdleState extends WidgetState {

  private List<ReviewRequest> reviewRequests;
  private Image image = null;
  private Runnable runnable;

  IdleState(Project project, String iconPath, Runnable runnable) {
    super(project, null, null);
    this.runnable = runnable;
    image = ImageLoader.loadFromResource(iconPath, getClass());
  }

  public void setReviewRequests(@NotNull List<ReviewRequest> reviewRequests) {
    this.reviewRequests = reviewRequests.stream()
        .filter(reviewRequest -> "pending".equalsIgnoreCase(reviewRequest.getStatus()))
        .collect(Collectors.toList());
  }

  @NotNull
  @Override
  public Icon getIcon() {
    return new TextIcon(reviewRequests.size(), image);
  }

  @Nullable
  @Override
  public String getTooltipText() {
    return "You have " + reviewRequests.size() + " pending reviewRequests";
  }

  @Nullable
  @Override
  public Consumer<MouseEvent> getClickConsumer() {
    return mouseEvent -> {
      if (reviewRequests != null) {
        showPopup(mouseEvent);
      }
    };
  }

  private void showPopup(MouseEvent event) {
    DataContext context = DataManager.getInstance().getDataContext((Component) myStatusBar);
    PopupFactoryImpl.ActionGroupPopup popup = (PopupFactoryImpl.ActionGroupPopup) JBPopupFactory.getInstance()
        .createActionGroupPopup("Select an Item to Review",
            new ReviewGroup(this.getProject(), ReviewFactory.getInstance(), reviewRequests, runnable), context,
            JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true, null, 5);
    if (popup.isVisible()) {
      popup.cancel();
      return;
    }
    final Dimension dimension = popup.getContent().getPreferredSize();
    final Point at = new Point(0, -dimension.height);
    popup.setSize(dimension);
    popup.show(new RelativePoint(event.getComponent(), at));
    Disposer.register(this, popup); // destroy popup on unexpected project close
  }
}

class WidgetState extends EditorBasedWidget implements StatusBarWidget.IconPresentation {

  private final Icon icon;
  private final String toolTip;
  Consumer<Project> consumer;

  WidgetState(Project project, Icon icon, String toolTip, Consumer<Project> consumer) {
    super(project);
    this.icon = icon;
    this.toolTip = toolTip;
    this.consumer = consumer;
  }

  WidgetState(Project project, Icon icon, String toolTip) {
    this(project, icon, toolTip, null);
  }

  @NotNull
  @Override
  public Icon getIcon() {
    return this.icon;
  }

  @Nullable
  @Override
  public String getTooltipText() {
    return toolTip;
  }

  @NotNull
  public Consumer<MouseEvent> getClickConsumer() {
    return mouseEvent -> {
      if (consumer != null) {
        consumer.consume(myProject);
      }
    };
  }

  @NotNull
  @Override
  public String ID() {
    return "Review Board Widget";
  }

  @Nullable
  @Override
  public WidgetPresentation getPresentation(@NotNull PlatformType type) {
    return this;
  }
}
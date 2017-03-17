package com.senthil.utils;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.ContentFactory;
import com.senthil.model.Repositories;
import com.senthil.model.ReviewRequest;
import com.senthil.model.Writable;
import com.senthil.net.ReviewDataProvider;
import com.senthil.net.ReviewFactory;
import com.senthil.state.SettingsPage;
import com.senthil.ui.SummaryPanel;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import javax.swing.*;
import org.jetbrains.annotations.CalledInAwt;
import org.jetbrains.annotations.CalledInBackground;


/**
 * Created by spanneer on 2/2/17.
 */
public class Utils {

  private static final ScheduledExecutorService schedulerExecutor = Executors.newScheduledThreadPool(10);
  static Pattern pattern = Pattern.compile("^https://rb.corp.linkedin.com/api/repositories/.*");

  public static Icon getIcon(Integer number) {
//    UIUtil.createImage(1,1, BufferedImage.TYPE_INT_RGB);
//    Graphics2D g2d = img.createGraphics();
//    Font font = new Font("Arial", Font.PLAIN, 48);
//    g2d.setFont(font);
//    FontMetrics fm = g2d.getFontMetrics();
//    int width = fm.stringWidth(text);
//    int height = fm.getHeight();
//    g2d.dispose();
    return null;
  }

  public static int getRepoId(Project project) {
    int repoId = -1;
    PropertiesComponent comp = PropertiesComponent.getInstance(project);
    if (comp != null) {
      repoId = comp.getInt("repoId", -1);
    }

    if (repoId != -1) {
      return repoId;
    } else {
      ShowSettingsUtil.getInstance().editConfigurable(project, new SettingsPage(project));
      return repoId;
    }
  }

  public static String getProductName(Project project) {
    if(!project.getName().contains("_trunk")) {
      return project.getName();
    }
    return  project.getName().substring(0, project.getName().lastIndexOf("_trunk"));

//    Collection<Repository> repositories = VcsRepositoryManager.getInstance(project).getRepositories();
//    if (repositories.size() != 1) {
//      return null;
//    }
//
//    if (!(repositories.toArray()[0] instanceof GitRepository)) {
//      return null;
//    }
//
//    GitRepository gitrepo = (GitRepository) repositories.toArray()[0];
//    Collection<GitRemote> remotes = gitrepo.getInfo().getRemotes();
//    assert remotes.size() > 0;
//    String firstUrl = ((GitRemote) remotes.toArray()[0]).getFirstUrl();
//    String path = firstUrl.split(":")[1];
//    return path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf(".git"));
  }

  public static void showReview(Project project, ReviewRequest review) {
    SummaryPanel panel = new SummaryPanel(project);
    panel.setReview(review);
    showToolWindow(project, panel, review.getDescription());
  }

  public static void showToolWindow(Project project, JComponent component, String title) {
    ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Linkedin Review");
    toolWindow.getContentManager().removeAllContents(true);
    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    toolWindow.getContentManager().addContent(contentFactory.createContent(component, title, false));
    toolWindow.setAutoHide(false);
    toolWindow.show(null);
    toolWindow.activate(null);
  }

  public static <T extends Writable> CompletableFuture<T> supplyAsync(CompletableFuture<T> primaryFuture,
      CompletableFuture<T> secondaryFuture, Consumer<T> consumer) {
    return supplyAsync(primaryFuture, secondaryFuture, 10, TimeUnit.SECONDS, consumer);
  }

  public static <T extends Writable> CompletableFuture<T> supplyAsync(CompletableFuture<T> primaryFuture,
      CompletableFuture<T> secondaryFuture) {
    return supplyAsync(primaryFuture, secondaryFuture, 10, TimeUnit.SECONDS, null);
  }

  public static <T extends Writable> CompletableFuture<T> supplyAsync(CompletableFuture<T> primaryFuture,
      CompletableFuture<T> secondaryFuture, int timeoutValue, TimeUnit timeUnit) {
    return supplyAsync(primaryFuture, secondaryFuture, timeoutValue, timeUnit, null);
  }

  @CalledInBackground
  @CalledInAwt
  public static <T extends Writable> CompletableFuture<T> supplyAsync(CompletableFuture<T> primaryFuture,
      CompletableFuture<T> secondaryFuture, int timeoutValue, TimeUnit timeUnit, Consumer<? super T> consumer) {

    final CompletableFuture<T> cf = new CompletableFuture<T>();

    primaryFuture.thenAccept(t -> {
      try {

        if (t != null) {
          if (consumer != null) {
            consumer.accept(t);
          } else {
            CompletableFuture.runAsync(t::write);
          }
        } else {
          t = secondaryFuture.get();
        }
        cf.complete(t);
      } catch (Exception e) {
        cf.completeExceptionally(e);
      }
    });

    primaryFuture.exceptionally(e -> {
      if (e instanceof UnknownHostException) {
        try {
          cf.complete(secondaryFuture.get());
        } catch (Exception ex) {
          cf.completeExceptionally(ex);
        }
      }
      return null;
    });
    schedulerExecutor.schedule(() -> {
      if (!primaryFuture.isDone()) {
        try {
          cf.complete(secondaryFuture.get());
        } catch (InterruptedException | ExecutionException e) {
          cf.completeExceptionally(e);
        }
      }
    }, timeoutValue, timeUnit);
    return cf;
  }
}

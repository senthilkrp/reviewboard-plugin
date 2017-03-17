package com.senthil.ui;

import com.intellij.diff.DiffContext;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.tools.simple.SimpleDiffViewer;
import com.intellij.diff.tools.util.base.DiffViewerListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.EditorMouseAdapter;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseEventArea;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupAdapter;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.ui.JBColor;
import com.intellij.util.containers.HashMap;
import com.senthil.net.Constants;
import com.senthil.model.Comment;
import com.senthil.model.Comments;
import com.senthil.utils.OfflineUtils;
import com.senthil.ui.diff.CommentGutterIconRenderer;
import com.senthil.ui.panels.CommentPanel;
import com.senthil.ui.panels.CommentsListViewPanel;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;


public class ReviewDiffWindow extends SimpleDiffViewer {
  private final DiffRequest diffRequest;
  private Map<Integer, Comment> newCommentsMap = new HashMap<>();
  private Map<Integer, List<Comment>> existingCommentsMap = new HashMap<>();
  private Map<Integer, RangeHighlighter> highlightersMap = new HashMap<>();
  private List<Comment> newComments = new ArrayList<>();
  private ListCellRenderer<Comment> listCellRenderer;

  public ReviewDiffWindow(@NotNull DiffContext context, @NotNull DiffRequest request) {
    super(context, request);
    this.diffRequest = request;
    this.listCellRenderer = new ListCellRenderer<Comment>() {
      @Override
      public Component getListCellRendererComponent(JList list, final Comment value, final int index,
          boolean isSelected, boolean cellHasFocus) {
        return new CommentPanel(value.getUser().getName(), value.getText(), value.getLastUpdatedTime()).getPanel();
      }
    };
    getEditor2().addEditorMouseListener(new EditorMouseAdapter() {
      @Override
      public void mouseClicked(EditorMouseEvent e) {
        if (e.getArea() != null && (e.getArea().equals(EditorMouseEventArea.LINE_NUMBERS_AREA) || e.getArea()
            .equals(EditorMouseEventArea.LINE_MARKERS_AREA))) {
          final Point locationOnScreen = e.getMouseEvent().getLocationOnScreen();
          final int lineNumber = EditorUtil.yPositionToLogicalLine(getEditor2(), e.getMouseEvent()) + 1;
          Comment newComment = newCommentsMap.get(lineNumber);
          List<Comment> comments = new ArrayList<>();
          List<Comment> existingComments = existingCommentsMap.get(lineNumber);
          if (existingComments != null) {
            comments.addAll(existingComments);
          }
          comments.add(newComment);
          showCommentsView(locationOnScreen, lineNumber, getEditor2(), comments);
        }
      }
    });

    CompletableFuture<Comments> commentsFuture = request.getUserData(Constants.COMMENTS_KEY);
    if (commentsFuture != null) {
      commentsFuture.thenAcceptAsync(commentsList -> {
        System.out.println(commentsList);
        commentsList.forEach(comment -> {
          addComment(comment.getFirstLine(), comment);
        });
      });
    }
  }

  private void addCommentIcon(int lineNumber) {
    MarkupModel markup = getEditor2().getMarkupModel();
    TextAttributes attributes = new TextAttributes();
    attributes.setBackgroundColor(JBColor.yellow);
    ApplicationManager.getApplication().invokeAndWait(() -> {
      RangeHighlighter rangeHighlighter =
          markup.addLineHighlighter(lineNumber - 1, HighlighterLayer.SELECTION, attributes);
      highlightersMap.put(lineNumber, rangeHighlighter);
      rangeHighlighter.setGutterIconRenderer(new CommentGutterIconRenderer());
    });
  }

  private void deleteCommentIcon(int lineNumber) {
    if (existingCommentsMap.containsKey(lineNumber)) {
      return;
    }
    MarkupModel markup = getEditor2().getMarkupModel();
    RangeHighlighter highlighter = highlightersMap.get(lineNumber);
    if (highlighter != null) {
      markup.removeHighlighter(highlightersMap.remove(lineNumber));
    }
  }

  private void showCommentsView(Point locationOnScreen, final int lineNumber, final Editor editor,
      List<Comment> comments) {
    comments = comments == null ? Collections.emptyList() : comments;
    final CommentsListViewPanel commentsListViewPanel = new CommentsListViewPanel(comments, listCellRenderer);
    commentsListViewPanel.setVisible(true);

    commentsListViewPanel.setListener(new CommentsListViewPanel.CommentListener<Comment>() {
      @Override
      public void onAdd(String value, boolean issueOpened) {
        addComment(lineNumber, new Comment(lineNumber, value));
      }

      @Override
      public void onDelete(Comment value) {
        newCommentsMap.remove(lineNumber);
      }
    });

    final JBPopup popup = JBPopupFactory.getInstance()
        .createComponentPopupBuilder(commentsListViewPanel, commentsListViewPanel)
        .setTitle("Comment")
        .setMovable(true)
        .setRequestFocus(true)
        .setAdText("Click anywhere outside the dialog to save comment. Press escape to discard")
        .setResizable(true)
        .setFocusable(true)
        .setMayBeParent(false)
        .setModalContext(false)
        .setCancelOnOtherWindowOpen(false)
        .createPopup();
    popup.showInScreenCoordinates(getEditor1().getComponent(), locationOnScreen);
    popup.addListener(new JBPopupAdapter() {
      @Override
      public void onClosed(LightweightWindowEvent event) {
        addComment(lineNumber, commentsListViewPanel.getNewComment());
        commentsListViewPanel.setListener(null);
      }
    });
  }

  private void addComment(int lineNumber, @NotNull Comment comment) {
    if (comment.isNew() && (comment.getText() == null || comment.getText().trim().isEmpty())) {
      deleteComment(lineNumber);
      return;
    }

    if (!newCommentsMap.containsKey(lineNumber) && !existingCommentsMap.containsKey(lineNumber)) {
      addCommentIcon(lineNumber);
    }

    if (comment.isNew()) {
      comment.setFirstLine(lineNumber);
      newCommentsMap.put(lineNumber, comment);
    } else {
      if (existingCommentsMap.containsKey(lineNumber)) {
        existingCommentsMap.get(lineNumber).add(comment);
      } else {
        List<Comment> comments = new ArrayList<>();
        comments.add(comment);
        existingCommentsMap.put(lineNumber, comments);
      }
    }
  }

  private void deleteComment(int lineNumber) {
    newCommentsMap.remove(lineNumber);
    deleteCommentIcon(lineNumber);
  }

  @Override
  protected void onDispose() {
    super.onDispose();

//  Save the new comments, so that we can upload then later.
    Set<Comment> newComments = new HashSet<>(newCommentsMap.values());

    Integer fileId = diffRequest.getUserData(Constants.FILE_KEY).getId();

    Comments comments = new Comments(newComments);
    comments.setFileId(fileId);

    OfflineUtils.write(comments,
        "unchecked" + File.separator + diffRequest.getUserData(Constants.REVIEW).getId() + File.separator
            + diffRequest.getUserData(Constants.FILE_KEY).getId() + "_comments.json");
  }

  @Override
  protected void onDocumentChange(@NotNull DocumentEvent event) {
    super.onDocumentChange(event);
  }

  @Override
  protected void onFileChange(@NotNull VirtualFileEvent event) {
    super.onFileChange(event);
  }

  @Override
  public void addListener(@NotNull DiffViewerListener listener) {
    super.addListener(listener);
  }

  @Override
  protected void onInit() {
    super.onInit();
    Comments comments = null;
    try {
      File file = OfflineUtils.getFile(
          "unchecked" + File.separator + diffRequest.getUserData(Constants.REVIEW).getId() + File.separator
              + diffRequest.getUserData(Constants.FILE_KEY).getId() + "_comments.json");
      if (file.exists()) {
        comments = OfflineUtils.read(file.getPath(), Comments.class);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    if (comments != null) {
      comments.forEach(comment -> {
        addComment(comment.getFirstLine(), comment);
      });
    }
  }
}

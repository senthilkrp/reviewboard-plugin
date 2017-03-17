package com.senthil.ui;

import com.intellij.diff.DiffContext;
import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.DiffTool;
import com.intellij.diff.chains.DiffRequestChain;
import com.intellij.diff.impl.CacheDiffRequestChainProcessor;
import com.intellij.diff.impl.DiffRequestProcessor;
import com.intellij.diff.impl.DiffWindow;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.tools.simple.SimpleDiffTool;
import com.intellij.diff.tools.simple.SimpleDiffViewer;
import com.intellij.diff.util.DiffUserDataKeysEx;
import com.intellij.openapi.project.Project;
import com.senthil.model.Comment;
import java.util.*;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Created by spanneer on 2/5/17.
 */
public class CommentDiffWindow extends DiffWindow {

  private final DiffRequestChain requestChain;
  private List<Comment> _comments;

  public CommentDiffWindow(@Nullable Project project, @NotNull DiffRequestChain requestChain,
      @NotNull DiffDialogHints hints) {
    super(project, requestChain, hints);
    this.requestChain = requestChain;
  }

  @NotNull
  @Override
  protected DiffRequestProcessor createProcessor() {
    return new MyCacheDiffRequestChainProcessor(myProject, requestChain);
  }

  public void setComments(List<Comment> comments) {
    _comments = comments;
  }

  private class MyCacheDiffRequestChainProcessor extends CacheDiffRequestChainProcessor {
    private final DiffRequestChain requestChain;

    public MyCacheDiffRequestChainProcessor(@Nullable Project project, @NotNull DiffRequestChain requestChain) {
      super(project, requestChain);
      this.requestChain = requestChain;
    }

    @NotNull
    @Override
    protected java.util.List<DiffTool> getToolOrderFromSettings(@NotNull java.util.List<DiffTool> availableTools) {
      return Collections.singletonList(new RBTool());
    }

    @Override
    protected void goToNextChange(boolean fromDifferences) {
      requestChain.getIndex();
      super.goToNextChange(fromDifferences);
//      save current comments
      updateRequest(false, fromDifferences ? DiffUserDataKeysEx.ScrollToPolicy.FIRST_CHANGE : null);
    }

  }
}


class RBTool extends SimpleDiffTool {

  @NotNull
  @Override
  public DiffViewer createComponent(@NotNull DiffContext context, @NotNull DiffRequest request) {
    return new ReviewDiffWindow(context, request);
  }

  @NotNull
  @Override
  public String getName() {
    return "Review Board";
  }

  @Override
  public boolean canShow(@NotNull DiffContext context, @NotNull DiffRequest request) {
    return SimpleDiffViewer.canShowRequest(context, request);
  }
}

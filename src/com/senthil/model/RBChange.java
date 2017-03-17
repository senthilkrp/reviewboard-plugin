package com.senthil.model;

import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import org.jetbrains.annotations.Nullable;


public class RBChange extends Change{

  private final ReviewFile _reviewFile;

  public RBChange(@Nullable ContentRevision beforeRevision, @Nullable ContentRevision afterRevision,
       ReviewFile reviewFile) {
    super(beforeRevision, afterRevision);
    this._reviewFile = reviewFile;
  }

  public ReviewFile getFile() {
    return _reviewFile;
  }
}

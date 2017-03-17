package com.senthil.ui;

import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.senthil.model.FileContent;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Created by spanneer on 2/5/17.
 */
public class LazyContentRevision implements ContentRevision {

  private final CompletableFuture<FileContent> myContent;
  private final FilePath myNewFilePath;
  private final String myRevision;

  public LazyContentRevision(CompletableFuture<FileContent> contents, FilePath filePath, String revision) {
    myContent = contents;
    myNewFilePath = filePath;
    myRevision = revision;
  }

  @Nullable
  @Override
  public String getContent() throws VcsException {
    try {
      FileContent content = myContent.get();
      if(content == null) {
        System.out.println("Null content received for " + myNewFilePath);
        return null;
      }
      FileContent file = myContent.get();
      if(file == null) {
        return null;
      }
      return file.getContent();
    } catch (Exception e) {
      return "Error retrieving contents of the file";
    }
  }

  @NotNull
  @Override
  public FilePath getFile() {
    return myNewFilePath;
  }

  @NotNull
  @Override
  public VcsRevisionNumber getRevisionNumber() {
    return new VcsRevisionNumber() {
      public String asString() {
        return myRevision;
      }

      public int compareTo(final VcsRevisionNumber o) {
        return myRevision.compareTo(asString());
      }
    };
  }
}

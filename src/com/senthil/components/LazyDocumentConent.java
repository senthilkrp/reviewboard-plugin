package com.senthil.components;

import com.intellij.diff.contents.DocumentContent;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class LazyDocumentConent implements DocumentContent {
  @NotNull
  @Override
  public Document getDocument() {
    return null;
  }

  @Nullable
  @Override
  public FileType getContentType() {
    return JavaFileType.INSTANCE;
  }

  @Nullable
  @Override
  public <T> T getUserData(@NotNull Key<T> key) {
    return null;
  }

  @Override
  public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {

  }
}

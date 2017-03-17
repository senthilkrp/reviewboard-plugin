package com.senthil.ui;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ui.ColoredListCellRenderer;
import com.senthil.model.ReviewFile;
import javax.swing.*;


/**
 * Created by spanneer on 2/4/17.
 */
public class ReviewFileListRenderer extends ColoredListCellRenderer <ReviewFile>{

  @Override
  protected void customizeCellRenderer(JList<? extends ReviewFile> list, ReviewFile reviewFile, int index,
      boolean selected, boolean hasFocus) {
      append(
          reviewFile.getSourceFile().equals(reviewFile.getDestinationFile()) ? reviewFile.getSourceFile() : reviewFile.getSourceFile() + " -> " + reviewFile
              .getDestinationFile());
      setIcon(JavaFileType.INSTANCE.getIcon());
  }
}

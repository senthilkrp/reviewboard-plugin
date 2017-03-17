package com.senthil.components;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;


/**
 * Created by spanneer on 2/2/17.
 */
public class RBWindow implements ToolWindowFactory, DumbAware {

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

  }

  @Override
  public boolean shouldBeAvailable(@NotNull Project project) {
    return false;
  }
}

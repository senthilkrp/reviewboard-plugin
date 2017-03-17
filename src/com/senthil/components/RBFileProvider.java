package com.senthil.components;

import com.intellij.ide.util.gotoByName.ChooseByNameBase;
import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.util.Processor;
import com.senthil.model.ReviewRequest;
import com.senthil.net.ReviewDataProvider;
import com.senthil.net.ReviewFactory;
import com.senthil.utils.Utils;
import java.util.List;
import org.jetbrains.annotations.NotNull;


class RBFileProvider implements ChooseByNameItemProvider, DumbAware{

  private final Project project;

  public RBFileProvider(Project project) {
    this.project = project;
  }

  @NotNull
  @Override
  public List<String> filterNames(@NotNull ChooseByNameBase base, @NotNull String[] names, @NotNull String pattern) {
    return null;
  }

  @Override
  public boolean filterElements(@NotNull ChooseByNameBase base, @NotNull String pattern, boolean everywhere,
      @NotNull ProgressIndicator cancelled, @NotNull Processor<Object> consumer) {

    if (pattern.length() < 5) {
      return false;
    }
    ReviewDataProvider provider = ReviewFactory.getInstance();
    try {
      List<ReviewRequest> reviews = provider.search(Utils.getRepoId(project), pattern).get();
      reviews.forEach(consumer::process);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return true;
  }
}

package com.senthil.model;

import com.intellij.ide.util.gotoByName.ChooseByNameItem;
import org.jetbrains.annotations.NotNull;


/**
 * Created by spanneer on 2/23/17.
 */
public class ReviewRequestItem implements ChooseByNameItem {

  private final ReviewRequest reviewRequest;

  public ReviewRequestItem(@NotNull ReviewRequest reviewRequest) {
    this.reviewRequest = reviewRequest;
  }

  @Override
  public String getName() {
    return reviewRequest.getDescription();
  }

  @Override
  public String getDescription() {
    return reviewRequest.getStatus();
  }
}

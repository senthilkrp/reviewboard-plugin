package com.senthil.model;

import java.util.List;


/**
 * Created by spanneer on 2/22/17.
 */
public class Reviews extends IterableModel<Review> {

  private List<Review> reviews;

  @Override
  protected List<Review> getCollection() {
    return reviews;
  }
}

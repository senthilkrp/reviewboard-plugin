package com.senthil.net;

/**
 * Created by spanneer on 1/26/17.
 */
public class ReviewFactory {

  private static ReviewDataProvider provider = new CombinedReviewDataProvider();

  public static ReviewDataProvider getInstance(){
    return provider;

  }
}

package com.senthil.model;

import com.senthil.net.Constants;
import java.util.List;
import java.util.regex.Matcher;


/**
 * Created by spanneer on 2/22/17.
 */
public class Diffs extends IterableModel<Diff> implements Writable{

  private List<Diff> diffs;

  private Links links;

  @Override
  protected List<Diff> getCollection() {
    return diffs;
  }

  @Override
  public String getPath() {
    return Constants.DIFF_SAVE_PATH.format(new Object[]{getReviewRequestId()});
  }

  public Integer getReviewRequestId() {
    Matcher matcher = Constants.DIFF_PATTERN.matcher(links.self.getHref());
    if (matcher.find()) {
      return Integer.parseInt(matcher.group(2));
    }
    return null;
  }

  class Links {
    Link self;
  }
}

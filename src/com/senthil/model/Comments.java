package com.senthil.model;

import com.senthil.net.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;


/**
 * Created by spanneer on 2/22/17.
 */
public class Comments extends IterableModel<Comment> implements Writable{

  private Integer fileId;

  public Comments() {

  }

  public Comments(Set<Comment> comments){
    diff_comments = comments;
  }

  private Set<Comment> diff_comments;

  @Override
  protected List<Comment> getCollection() {
    return new ArrayList<>(diff_comments);
  }

  @Override
  public String getPath() {
    return Constants.COMMENTS_SAVE_PATH.format(new Object[]{getReviewRequest(), getRevision(), getFileId()});
  }

  private Links links;

  public void setFileId(Integer  fileId) {
    this.fileId = fileId;
  }

  private class Links {
    Link self;
  }

  public Integer getRevision() {
    Matcher matcher = Constants.COMMENT_PATTERN.matcher(links.self.getHref());
    if (matcher.find()) {
      return Integer.valueOf(matcher.group(4));
    }
    return null;
  }

  public Integer getReviewRequest() {
    Matcher matcher = Constants.COMMENT_PATTERN.matcher(links.self.getHref());
    if (matcher.find()) {
      return Integer.valueOf(matcher.group(2));
    }
    return null;
  }

  public Integer getFileId() {
    if (fileId == null) {
      Matcher matcher = Constants.COMMENT_PATTERN.matcher(links.self.getHref());
      if (matcher.find()) {
        fileId = Integer.valueOf(matcher.group(6));
      }
    }
    return fileId;
  }
}

package com.senthil.model;

import com.google.gson.annotations.SerializedName;
import com.senthil.net.Constants;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;


/**
 * Created by spanneer on 2/22/17.
 */
public class ReviewFiles extends IterableModel<ReviewFile> implements Writable{

  @SerializedName("files")
  private List<ReviewFile> reviewFiles;

  private Links links;

  class Links{
    Link self;
  }

  @Override
  protected List<ReviewFile> getCollection() {
    return reviewFiles;
  }

  @Override
  public String getPath() {
    return Constants.FILES_SAVE_PATH.format(new Object[]{getReviewRequest(), getRevision()});
  }

  public Integer getRevision() {
    Matcher matcher = Constants.FILE_LIST_PATTERN.matcher(links.self.getHref());
    if (matcher.find()) {
      return Integer.valueOf(matcher.group(4));
    }
    return null;
  }

  public Integer getReviewRequest() {
    Matcher matcher = Constants.FILE_LIST_PATTERN.matcher(links.self.getHref());
    if (matcher.find()) {
      return Integer.valueOf(matcher.group(2));
    }
    return null;
  }

  public Map<String,ReviewFile> asMap() {
    return getCollection().stream().collect(Collectors.toMap(ReviewFile::getSourceFile, Function.identity()));
  }

  private static String getRevision(ReviewFile file) {
    return file.getSourceRevision();
  }
}

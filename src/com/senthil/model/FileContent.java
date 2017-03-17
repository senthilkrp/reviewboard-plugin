package com.senthil.model;

import com.senthil.net.Constants;
import java.util.regex.Matcher;


/**
 * Created by spanneer on 2/23/17.
 */
public class FileContent implements Writable {
  private String href;

  private String content;

  public FileContent(String href, String content) {
    this.href = href;
    this.content = content;
  }

  @Override
  public String getPath() {
    Matcher matcher = Constants.CONTENT_PATTERN.matcher(href);
    if(matcher.find()) {
      return Constants.CONTENT_SAVE_PATH.format(new Object[]{Integer.valueOf(matcher.group(2)), Integer.valueOf(matcher.group(4)), Integer.valueOf(matcher.group(6)), matcher.group(7)});
    }
    return null;
  }

  public int length() {
    return content.length();
  }

  public String getContent() {
    return content;
  }
}

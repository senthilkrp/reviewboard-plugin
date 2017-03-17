package com.senthil.model;

/**
 * Created by spanneer on 2/23/17.
 */
public class Link {
  private String href;
  private String method;
  private String title;

  public String getHref() {
    return href;
  }

  public String getMethod() {
    return method;
  }

  public String getTitle() {
    return title;
  }

  @Override
  public String toString() {
    return "Link{" + "href='" + href + '\'' + ", method='" + method + '\'' + ", title='" + title + '\'' + '}';
  }
}

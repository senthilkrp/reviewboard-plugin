package com.senthil.model;

import com.google.gson.annotations.SerializedName;


/**
 * Created by spanneer on 2/22/17.
 */
public class Review extends Model{
  @SerializedName("absolute_url")
  private String url;

  @SerializedName("body_top")
  private String comment;

  @SerializedName("ship_it")
  private boolean shipIt;

  public String getUrl() {
    return url;
  }

  public String getComment() {
    return comment;
  }

  public boolean isShipIt() {
    return shipIt;
  }

  @Override
  public String toString() {
    return "Review{" + "url='" + url + '\'' + ", comment='" + comment + '\'' + ", shipIt=" + shipIt + "} "
        + super.toString();
  }
}

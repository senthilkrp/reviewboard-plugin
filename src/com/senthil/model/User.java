package com.senthil.model;

import com.google.gson.annotations.SerializedName;


/**
 * Created by spanneer on 2/22/17.
 */
public class User {
  @SerializedName("title")
  String name;

  String href;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getHref() {
    return href;
  }
}

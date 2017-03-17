package com.senthil.model;

import com.google.gson.annotations.SerializedName;


/**
 * Created by spanneer on 2/22/17.
 */
public class Links {

  private User user;

  @SerializedName("original_file")
  private FileContent originalFile;

  @SerializedName("patched_file")
  private FileContent patchedFile;

  private User submitter;

  public User getUser() {
    return user;
  }



  public void setUser(User user) {
    this.user = user;
  }

  public User getSubmitter() {
    return submitter;
  }

  public FileContent getOriginalFile() {
    return originalFile;
  }

  public FileContent getPatchedFile() {
    return patchedFile;
  }
}

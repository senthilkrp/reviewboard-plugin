package com.senthil.net;

/**
 * Created by spanneer on 1/29/17.
 */
public abstract class AbstractReviewDataProvider implements ReviewDataProvider {

  protected String userName;
  protected String password;
  protected String url;

  public void setCredentials(String userName, String password, String repoUrl) {
    System.out.println("Setting username " + userName);
    System.out.println("Setting password " + password);
    this.userName = userName;
    this.password = password;
    this.url = repoUrl;
  }
}

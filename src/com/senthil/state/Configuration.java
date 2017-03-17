package com.senthil.state;

import com.senthil.model.Repository;
import java.util.Date;


public class Configuration {
  private String reviewBoardUrl;
  private String username;
  private String password;

  private Date lastAccessTime = new Date(0);
  private Repository repository;

  public Configuration(String username, String password, String reviewBoardUrl, Repository repository) {
    this.username = username;
    this.password = password;
    this.reviewBoardUrl = reviewBoardUrl;
    this.repository = repository;
  }

  public Configuration() {
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Date getLastAccessTime() {
    return lastAccessTime;
  }

  public void setLastAccessTime(Date lastAccessTime) {
    this.lastAccessTime = lastAccessTime;
  }

  public String getReviewBoardUrl() {
    return reviewBoardUrl;
  }

  public void setReviewBoardUrl(String reviewBoardUrl) {
    this.reviewBoardUrl = reviewBoardUrl;
  }

  @Override
  public String toString() {
    return "Configuration{" + "username='" + username + '\'' + ", password='" + password + '\'' + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Configuration that = (Configuration) o;

    if (!reviewBoardUrl.equals(that.reviewBoardUrl)) {
      return false;
    }
    if (!username.equals(that.username)) {
      return false;
    }
    return password.equals(that.password);
  }

  @Override
  public int hashCode() {
    int result = reviewBoardUrl.hashCode();
    result = 31 * result + username.hashCode();
    result = 31 * result + password.hashCode();
    return result;
  }

  public Repository getRepository() {
    return repository;
  }

}

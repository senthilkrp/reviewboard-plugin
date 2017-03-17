package com.senthil.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;


/**
 * Created by spanneer on 2/22/17.
 */
public class Comment extends Model {

  private Integer first_line;

  private Integer interfilediff;

  @SerializedName("num_lines")
  private Integer numberOfLines;

  @SerializedName("public")
  private boolean publicAccess = true;

  private String text;

  @SerializedName("issue_opened")
  private boolean isIssue = true;

  @SerializedName("issue_status")
  private String status;

  @SerializedName("timestamp")
  protected Date lastUpdatedTime;

  private Links links;
  private boolean _new;

  public Comment(int lineNumber, String value) {
    first_line = lineNumber;
    text = value;
  }

  public Comment() {
  }

  public User getUser() {
    return links.user;
  }

  @Override
  public String toString() {
    return "Comment{" + "first_line=" + first_line + ", id=" + id + ", interfilediff=" + interfilediff
        + ", numberOfLines=" + numberOfLines + ", public=" + publicAccess + ", text='" + text + '\''
        + ", isIssue=" + isIssue + ", status='" + status + '\'' + '}';
  }

  public Comment setFirstLine(Integer first_line) {
    this.first_line = first_line;
    return this;
  }

  public Comment setId(Integer id) {
    this.id = id;
    return this;
  }

  public Comment setInterfilediff(Integer interfilediff) {
    this.interfilediff = interfilediff;
    return this;
  }

  public Comment setNumberOfLines(Integer numberOfLines) {
    this.numberOfLines = numberOfLines;
    return this;
  }

  public Comment setPublic(boolean publicAccess) {
    this.publicAccess = publicAccess;
    return this;
  }

  public Comment setText(String text) {
    this.text = text;
    return this;
  }

  public Comment setIssue(boolean issue) {
    isIssue = issue;
    return this;
  }

  public Comment setStatus(String status) {
    this.status = status;
    return this;
  }

  public Integer getFirstLine() {
    return first_line;
  }

  public Integer getInterfilediff() {
    return interfilediff;
  }

  public Integer getNumberOfLines() {
    return numberOfLines;
  }

  public boolean isPublic() {
    return publicAccess;
  }

  public String getText() {
    return text;
  }

  public boolean issue() {
    return isIssue;
  }

  public String getStatus() {
    return status;
  }

  public boolean isNew() {
    return id == null;
  }

  public Date getLastUpdatedTime() {
    return lastUpdatedTime;
  }

  class Links {
    User user;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Comment comment = (Comment) o;

    //if its a new comment and if its on the same line, its the same comment.
    if(id == null && comment.getId() ==null && first_line != null && comment.first_line.equals(first_line))
      return true;

    if (publicAccess != comment.publicAccess) {
      return false;
    }
    if (isIssue != comment.isIssue) {
      return false;
    }
    if (_new != comment.isNew()) {
      return false;
    }
    if (first_line != null ? !first_line.equals(comment.first_line) : comment.first_line != null) {
      return false;
    }
    if (interfilediff != null ? !interfilediff.equals(comment.interfilediff) : comment.interfilediff != null) {
      return false;
    }
    if (numberOfLines != null ? !numberOfLines.equals(comment.numberOfLines) : comment.numberOfLines != null) {
      return false;
    }
    if (text != null ? !text.equals(comment.text) : comment.text != null) {
      return false;
    }
    if (status != null ? !status.equals(comment.status) : comment.status != null) {
      return false;
    }
    return links != null ? links.equals(comment.links) : comment.links == null;
  }

  @Override
  public int hashCode() {
    int result = first_line != null ? first_line.hashCode() : 0;
    result = 31 * result + (interfilediff != null ? interfilediff.hashCode() : 0);
    result = 31 * result + (numberOfLines != null ? numberOfLines.hashCode() : 0);
    result = 31 * result + (publicAccess ? 1 : 0);
    result = 31 * result + (text != null ? text.hashCode() : 0);
    result = 31 * result + (isIssue ? 1 : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    result = 31 * result + (_new ? 1 : 0);
    result = 31 * result + (links != null ? links.hashCode() : 0);
    return result;
  }
}

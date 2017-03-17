package com.senthil.model;

import com.google.gson.annotations.SerializedName;
import com.intellij.ide.util.gotoByName.ChooseByNameItem;
import com.senthil.net.Constants;
import java.util.Date;
import java.util.regex.Matcher;


/**
 * Created by spanneer on 2/22/17.
 */
public class ReviewRequest extends Model implements ChooseByNameItem {

  private Links links;

  private String branch;

  @SerializedName("close_description")
  private String closeDescription;

  private boolean approved;

  @SerializedName("commit_id")
  private String commitId;

  private String description;

  private String summary;

  @SerializedName("last_updated")
  private Date lastUpdatedTime;

  private String status;

  public String getBranch() {
    return branch;
  }

  public String getCloseDescription() {
    return closeDescription;
  }

  public boolean isApproved() {
    return approved;
  }

  public String getCommitId() {
    return commitId;
  }

  @Override
  public String getName() {
    return getSubmitter();
  }

  public String getDescription() {
    return description;
  }

  public Date getLastUpdatedTime() {
    return lastUpdatedTime;
  }

  public String getSummary() {
    return summary;
  }

  public String getStatus() {
    return status;
  }

  @Override
  public String toString() {
    return "ReviewRequest{" + "branch='" + branch + '\'' + ", closeDescription='" + closeDescription + '\''
        + ", approved=" + approved + ", commitId='" + commitId + '\'' + ", description='" + description + '\''
        + ", lastUpdatedTime=" + lastUpdatedTime + ", status='" + status + '\'' + "} " + super.toString();
  }


  public String getSubmitter() {
    Matcher matcher = Constants.SUBMITTER_PATTERN.matcher(links.submitter.getHref());
    if (matcher.find()) {
      return matcher.group(2);
    }
    return null;
  }

  public boolean isPending() {
    return "pending".equals(status);
  }

  class Links {
    Link submitter;
  }
}

package com.senthil.model;

import com.google.gson.annotations.SerializedName;
import com.senthil.net.Constants;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by spanneer on 2/22/17.
 */
public class ReviewRequests extends IterableModel<ReviewRequest> implements Writable{

  @SerializedName("review_requests")
  private List<ReviewRequest> reviewRequests;

  @SerializedName("last_updated")
  protected Date lastUpdatedTime;

  private Integer repositoryId;

  private Links links;

  public ReviewRequests(List<ReviewRequest> reviewRequests) {
    this.reviewRequests = reviewRequests;
  }

  public ReviewRequests() {
    reviewRequests = new ArrayList<>();
  }

  @Override
  protected List<ReviewRequest> getCollection() {
    return reviewRequests;
  }

  @Override
  public String getPath() {
    return Constants.REVIEW_REQUESTS_SAVE_PATH.format(new Object[]{repositoryId});
  }

  public Integer getRepositoryId() {
    return repositoryId;
  }

  public void setRepositoryId(Integer repositoryId) {
    this.repositoryId = repositoryId;
  }

  public Date getLastUpdatedTime() {
    return lastUpdatedTime;
  }
}


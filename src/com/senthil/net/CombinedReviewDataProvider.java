package com.senthil.net;

import com.senthil.model.Comment;
import com.senthil.model.Comments;
import com.senthil.model.Diffs;
import com.senthil.model.FileContent;
import com.senthil.model.ReviewFiles;
import com.senthil.model.Repositories;
import com.senthil.model.ReviewRequest;
import com.senthil.model.ReviewRequests;
import com.senthil.utils.Utils;
import com.senthil.model.Review;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class CombinedReviewDataProvider extends AbstractReviewDataProvider {

  private ReviewDataProvider onlineReviewDataProvider = new OnlineReviewDataProvider();
  private ReviewDataProvider offlineReviewDataProvider = new OfflineReviewDataProvider();

  @Override
  public CompletableFuture<List<ReviewRequest>> search(Integer repositoryId, String query) {
//    return Utils.supplyAsync(onlineReviewDataProvider.search(query), offlineReviewDataProvider.search(query), 10000, TimeUnit.MILLISECONDS, t -> {});
    return offlineReviewDataProvider.search(repositoryId, query);
  }

  @Override
  public void testConnection(String username, String password, String repoUrl) throws IOException, URISyntaxException {
    onlineReviewDataProvider.testConnection(username, password, repoUrl);
  }

  @Override
  public CompletableFuture<ReviewRequests> reviewRequests(Integer repositoryId, String status, Date lastUpdateTime) {
    return Utils.supplyAsync(onlineReviewDataProvider.reviewRequests(repositoryId, status, lastUpdateTime),
        offlineReviewDataProvider.reviewRequests(repositoryId, status, lastUpdateTime), t-> {});
  }

  @Override
  public CompletableFuture<ReviewRequests> reviewRequests(Integer repositoryId) {
    return Utils.supplyAsync(onlineReviewDataProvider.reviewRequests(repositoryId),
        offlineReviewDataProvider.reviewRequests(repositoryId));
  }

  @Override
  public CompletableFuture<Review> createReview(Integer reviewRequestId) {
    return onlineReviewDataProvider.createReview(reviewRequestId);
  }

  @Override
  public CompletableFuture<Repositories> repositories() {
    return onlineReviewDataProvider.repositories();
  }

  @Override
  public CompletableFuture<Integer> noOfRepositories(String reviewBoardUrl, String username, String password) {
    return onlineReviewDataProvider.noOfRepositories(reviewBoardUrl, username, password);
  }

  @Override
  public CompletableFuture<Repositories> repositories(String reviewBoardUrl, String username, String password, int start, int maxResults) {
    return onlineReviewDataProvider.repositories(reviewBoardUrl, username, password, start, maxResults);
  }

  @Override
  public CompletableFuture<ReviewFiles> files(Integer reviewRequestId, Integer revision) {
    return Utils.supplyAsync(onlineReviewDataProvider.files(reviewRequestId, revision),
        offlineReviewDataProvider.files(reviewRequestId, revision));
  }

  @Override
  public CompletableFuture<Comments> comments(Integer reviewRequestId, Integer revision, Integer fileId) {
    return Utils.supplyAsync(onlineReviewDataProvider.comments(reviewRequestId, revision, fileId),
        offlineReviewDataProvider.comments(reviewRequestId, revision, fileId));
  }

  @Override
  public CompletableFuture<Comments> comments(Integer reviewRequestId, Integer startRevision, Integer endRevision,
      Integer fileId) {
    return onlineReviewDataProvider.comments(reviewRequestId, startRevision, endRevision, fileId);
  }

  @Override
  public CompletableFuture<Diffs> diffs(Integer reviewRequestId) {
    return Utils.supplyAsync(onlineReviewDataProvider.diffs(reviewRequestId),
        offlineReviewDataProvider.diffs(reviewRequestId));
  }

  @Override
  public CompletableFuture<FileContent> contents(String href) {
//    return onlineReviewDataProvider.contents(href);
    return Utils.supplyAsync(onlineReviewDataProvider.contents(href), offlineReviewDataProvider.contents(href));
  }

  @Override
  public CompletableFuture<Void> createComment(Comment comment, Integer reviewRequestId, Integer reviewId,
      Integer fileId) {
    return onlineReviewDataProvider.createComment(comment, reviewRequestId, reviewId, fileId);
  }

  @Override
  public CompletableFuture<Integer> submitReview(ReviewRequest reviewRequest, List<Comments> commentsList,
      boolean shipIt, String description) {
    return onlineReviewDataProvider.submitReview(reviewRequest, commentsList, shipIt, description);
  }

  @Override
  public void setCredentials(String userName, String password, String repoUrl) {
    onlineReviewDataProvider.setCredentials(userName, password, repoUrl);
  }
}

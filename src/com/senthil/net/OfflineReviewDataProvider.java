package com.senthil.net;

import com.senthil.model.Comment;
import com.senthil.model.Comments;
import com.senthil.model.Diffs;
import com.senthil.model.FileContent;
import com.senthil.model.Repositories;
import com.senthil.model.ReviewFiles;
import com.senthil.model.ReviewRequest;
import com.senthil.model.ReviewRequests;
import com.senthil.model.Writable;
import com.senthil.utils.OfflineUtils;
import com.senthil.model.Review;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.stream.Collectors;


/**
 * Uses the provided review data provider as primary source.
 * If primary source fails, offline provider will provide offline data.
 */
public class OfflineReviewDataProvider extends AbstractReviewDataProvider {

  private <T extends Writable> CompletableFuture<T> load(Class<T> clazz, String path) {
    CompletableFuture<T> future = new CompletableFuture<T>();
    CompletableFuture.runAsync(() -> {
      if (clazz != null && path != null) {
        try {
          future.complete(OfflineUtils.read(path, clazz));
        } catch (Exception e) {
          future.completeExceptionally(e);
        }
      }
    });
    return future;
  }

  @Override
  //most basic search
  public CompletableFuture<List<ReviewRequest>> search(Integer repositoryId, String query) {
    return reviewRequests(repositoryId).thenApply(reviewRequests -> reviewRequests.stream().filter(r -> {
      if (r == null) {
        return false;
      }
      for (String token : query.toLowerCase().split(" ")) {
        return (r.getDescription() != null && r.getDescription().toLowerCase().contains(token)) || (
            r.getDescription() != null && r.getSummary().contains(token)) || (r.getSubmitter() != null
            && r.getSubmitter().equals(token));
      }
      return false;
    }).collect(Collectors.toList()));
  }

  @Override
  public void testConnection(String username, String password, String repoUrl) {
  }

  @Override
  public CompletableFuture<ReviewRequests> reviewRequests(Integer repositoryId, String status, Date lastUpdateTime) {
    return reviewRequests(repositoryId).thenApply(reviewRequests -> {
      List<ReviewRequest> filteredRequests = reviewRequests.stream()
          .filter(r -> status != null && status.equals(r.getStatus()) && r.getLastUpdatedTime().after(lastUpdateTime))
          .collect(Collectors.toList());
      ReviewRequests retValue = new ReviewRequests(filteredRequests);
      retValue.setRepositoryId(reviewRequests.getRepositoryId());
      return retValue;
    });
  }

  @Override
  public CompletableFuture<ReviewRequests> reviewRequests(Integer repositoryId) {
    return load(ReviewRequests.class, Constants.REVIEW_REQUESTS_SAVE_PATH.format(new Object[]{repositoryId}));
  }

  @Override
  public CompletableFuture<Review> createReview(Integer reviewRequestId) {
    return null;
  }

  @Override
  public CompletableFuture<Repositories> repositories() {
    return load(Repositories.class, "repositories.json");
  }

  @Override
  public CompletableFuture<Integer> noOfRepositories(String reviewBoardUrl, String username, String password) {
    return null;
  }

  @Override
  public CompletableFuture<Repositories> repositories(String reviewBoardUrl, String username, String password,
      int start, int maxResults) {
    return null;
  }

  @Override
  public CompletableFuture<ReviewFiles> files(Integer reviewRequestId, Integer revision) {
    return load(ReviewFiles.class,
        Constants.FILES_SAVE_PATH.format(new Object[]{reviewRequestId, revision}));
  }

  @Override
  public CompletableFuture<Comments> comments(Integer reviewRequestId, Integer revision, Integer fileId) {
    return load(Comments.class, Constants.COMMENTS_SAVE_PATH.format(
        new Object[]{reviewRequestId, revision, fileId}));
  }

  @Override
  public CompletableFuture<Comments> comments(Integer reviewRequestId, Integer startRevision, Integer endRevision,
      Integer fileId) {
    return null;
  }

  @Override
  public CompletableFuture<Diffs> diffs(Integer reviewRequestId) {
    return load(Diffs.class, Constants.DIFF_SAVE_PATH.format(new Object[]{reviewRequestId}));
  }

  @Override
  public CompletableFuture<FileContent> contents(String href) {
    Matcher matcher = Constants.CONTENT_PATTERN.matcher(href);
    if (matcher.find()) {
      return load(FileContent.class, Constants.CONTENT_SAVE_PATH.format(
          new Object[]{Integer.valueOf(matcher.group(2)), Integer.valueOf(matcher.group(4)), Integer.valueOf(matcher.group(6)), String.valueOf(matcher.group(7))}));
    }
    return null;
  }

  @Override
  public CompletableFuture<Void> createComment(Comment comment, Integer reviewRequestId, Integer reviewId,
      Integer fileId) {
    return null;
  }

  @Override
  public CompletableFuture<Integer> submitReview(ReviewRequest draft, List<Comments> comments, boolean shipIt,
      String description) {
    return null;
  }
}

package com.senthil.net;

import com.senthil.model.Comment;
import com.senthil.model.Comments;
import com.senthil.model.Diffs;
import com.senthil.model.FileContent;
import com.senthil.model.ReviewFiles;
import com.senthil.model.Repositories;
import com.senthil.model.Review;
import com.senthil.model.ReviewRequest;
import com.senthil.model.ReviewRequests;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public interface ReviewDataProvider {
  String API = "api";
  String SEARCH = "search";
  String QUERY = "q";
  String REVIEW_REQUESTS = "review-requests";
  String DIFFS = "diffs";
  String FILES = "files";
  String DIFF_COMMENTS = "diff-comments";
  String DRAFT = "draft";
  String REVIEWS = "reviews";
  String AUTHORIZATION = "Authorization";
  String REPOSITORIES = "repositories";
  String GROUPS = "groups";
  String USERS = "users";
  String SHIP_IT ="ship_it";

  CompletableFuture<List<ReviewRequest>> search(Integer repositoryId, String query);

  void testConnection(String username, String password, String repoUrl) throws URISyntaxException, IOException;

  CompletableFuture<ReviewRequests> reviewRequests(Integer repositoryId, String status, Date lastUpdateTime);

  CompletableFuture<ReviewRequests> reviewRequests(Integer RepositoryId);

  CompletableFuture<Review>  createReview(final Integer reviewRequestId);

  CompletableFuture<Repositories> repositories();

  CompletableFuture<Integer> noOfRepositories(String reviewBoardUrl, String username, String password);

  CompletableFuture<Repositories> repositories(String reviewBoardUrl, String username, String password, int start, int maxResults);

  CompletableFuture<ReviewFiles> files(Integer reviewRequestId, Integer revision);

  CompletableFuture<Comments> comments(Integer reviewRequestId, Integer revision, Integer fileId);

  CompletableFuture<Comments> comments(Integer reviewRequestId, Integer startRevision, Integer endRevision, Integer fileId);

  CompletableFuture<Diffs> diffs(Integer reviewRequestId);

  CompletableFuture<FileContent> contents(String href);

  CompletableFuture<Void> createComment(Comment comment, Integer reviewRequestId, Integer reviewId,
      Integer fileId);

  CompletableFuture<Integer> submitReview(ReviewRequest draft, List<Comments> comments, boolean shipIt, String description);

  void setCredentials(String username, String password, String repoUrl);

}

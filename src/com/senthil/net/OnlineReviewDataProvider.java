package com.senthil.net;

import com.google.common.io.CharStreams;
import com.senthil.model.Comment;
import com.senthil.model.Comments;
import com.senthil.model.Diffs;
import com.senthil.model.FileContent;
import com.senthil.model.ReviewFiles;
import com.senthil.model.Model;
import com.senthil.model.Repositories;
import com.senthil.model.Review;
import com.senthil.model.ReviewRequest;
import com.senthil.model.ReviewRequests;
import com.senthil.model.Search;
import com.senthil.util.HttpRequestBuilder;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jetbrains.annotations.NotNull;


/**
 * Created by spanneer on 1/26/17.
 */
public class OnlineReviewDataProvider extends AbstractReviewDataProvider {

  private static final ExecutorService executorService = Executors.newFixedThreadPool(100);

  public OnlineReviewDataProvider() {
  }

  @Override
  public CompletableFuture<List<ReviewRequest>> search(Integer repositoryId, String query) {
    return CompletableFuture.supplyAsync(()->{
      try {
        Search search = HttpRequestBuilder.get(url).route(API).route(SEARCH).slash().queryString("q", query).asModel(Search.class);
        return search.getReviewRequests();
      } catch (IOException | URISyntaxException e) {
        e.printStackTrace();
      }
      return null;
    });
  }

  @Override
  public void testConnection(String username, String password, String repoUrl) throws URISyntaxException, IOException {
    HttpRequestBuilder.get(repoUrl).route(API).slash().header(AUTHORIZATION, getAuthorizationHeader()).asJson(Model.class);
  }

  @Override
  public CompletableFuture<ReviewRequests> reviewRequests(Integer repositoryId, String status, Date lastUpdateTime) {
    CompletableFuture<ReviewRequests> future = new CompletableFuture<>();
    CompletableFuture.runAsync(() -> {
      try {
        HttpRequestBuilder requestBuilder = HttpRequestBuilder.get(url).route(API).route(REVIEW_REQUESTS).slash();
        if (repositoryId != null) {
          requestBuilder.queryString("repository", repositoryId);
        }
        ReviewRequests reviewRequests = requestBuilder.queryString("max-results", String.valueOf(100))
            .queryString("status", status == null ? "all" : status)
            .queryString("last-updated-from", lastUpdateTime)
            .asModel(ReviewRequests.class);
        reviewRequests.setRepositoryId(repositoryId);
        future.complete(reviewRequests);
      } catch (IOException | URISyntaxException e) {
        future.completeExceptionally(e);
      }
    }, executorService);
    return withAuthentication(future);
  }

  @Override
  public CompletableFuture<ReviewRequests> reviewRequests(Integer repositoryId) {
    return reviewRequests(repositoryId, null, null);
  }

  @Override
  public CompletableFuture<Review> createReview(Integer reviewRequestId) {
    CompletableFuture<Review> f = CompletableFuture.supplyAsync(() -> {
      try {
        HttpRequestBuilder requestBuilder = null;
        requestBuilder = HttpRequestBuilder.post(url)
            .route(API)
            .route(REVIEW_REQUESTS)
            .route(reviewRequestId)
            .route(REVIEWS)
            .header(AUTHORIZATION, getAuthorizationHeader())
            .slash();

        Review result = requestBuilder.asModel(ReviewWrapper.class).review;
        return result;
      } catch (IOException | URISyntaxException e) {
        return null;
      }
    }, executorService);

    return withAuthentication(f);
  }

  private CompletableFuture<Integer> updateReviewApi(Integer reviewRequestId, Integer reviewId, boolean isPublic,
      String body_top, String body_bottom, boolean shipIt) {
    CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> {
      try {
        HttpRequestBuilder put = HttpRequestBuilder.put(url);
        put.route(API)
            .route(REVIEW_REQUESTS)
            .route(reviewRequestId)
            .route(REVIEWS)
            .route(reviewId)
            .slash()
            .header(AUTHORIZATION, getAuthorizationHeader())
            .field("public", isPublic)
            .field(SHIP_IT, shipIt);
        if (!StringUtils.isEmpty(body_bottom)) {
          put.field("body_bottom", body_bottom);
        }
        if (!StringUtils.isEmpty(body_top)) {
          put.field("body_top", body_top);
        }
        Model result = put.asModel(Model.class);
      } catch (IOException | URISyntaxException e) {
        e.printStackTrace();
      }
      return reviewId;
    }, executorService);
    return withAuthentication(f);
  }

  @Override
  public CompletableFuture<Repositories> repositories() {
    return repositories(url, userName, password, 0, 100 );
  }

  @Override
  public CompletableFuture<Integer> noOfRepositories(String reviewBoardUrl, String username, String password) {
    CompletableFuture<Integer> future = new CompletableFuture<>();
    CompletableFuture.runAsync(() -> {
      try {
        HttpRequestBuilder builder =
            HttpRequestBuilder.get(reviewBoardUrl).route(API).route(REPOSITORIES).slash().queryString("counts-only", "true");
        Model model = builder.asModel(Model.class);
        future.complete(model.getCount());
      } catch (IOException | URISyntaxException e) {
        future.completeExceptionally(e);
      }
    }, executorService);
    return future;
  }

  public CompletableFuture<Repositories> repositories(String reviewBoardUrl, String username, String password, int start, int maxResults){
    CompletableFuture<Repositories> future = new CompletableFuture<>();
    CompletableFuture.runAsync(() -> {
      try {
        HttpRequestBuilder builder =
            HttpRequestBuilder.get(reviewBoardUrl).route(API).route(REPOSITORIES).slash().queryString("start", start).queryString("max-results", maxResults);
        future.complete(builder.asModel(Repositories.class));
      } catch (IOException | URISyntaxException e) {
        future.completeExceptionally(e);
      }
    }, executorService);
    return future;
  }

  @Override
  public CompletableFuture<ReviewFiles> files(Integer reviewRequestId, Integer revision) {
    CompletableFuture<ReviewFiles> future = new CompletableFuture<>();
    CompletableFuture.runAsync(() -> {
      ReviewFiles result;
      try {
        result = HttpRequestBuilder.get(url)
            .route(API)
            .route(REVIEW_REQUESTS)
            .route(reviewRequestId)
            .route(DIFFS)
            .route(revision)
            .route(FILES)
            .slash()
            .asModel(ReviewFiles.class);
        future.complete(result);
      } catch (IOException | URISyntaxException e) {
        future.completeExceptionally(e);
      }
    }, executorService);
    return future;
  }

  public CompletableFuture<FileContent> contents(String href) {
    CompletableFuture<FileContent> future = new CompletableFuture<>();
    CompletableFuture.runAsync(() -> {
      try {
        System.out.println("Getting contents for " + href);
        HttpRequestBase request = HttpRequestBuilder.get(href).request();
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
          CloseableHttpResponse response = client.execute(request);

          if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            future.complete(new FileContent(href, null));
          } else {
            future.complete(new FileContent(href,
                CharStreams.toString(new InputStreamReader(response.getEntity().getContent()))));
          }
        }
      } catch (Exception e) {
        future.completeExceptionally(e);
      }
    }, executorService);
    return future;
  }

  @Override
  public CompletableFuture<Comments> comments(Integer reviewRequestId, Integer revision, Integer fileId) {
    CompletableFuture<Comments> future = new CompletableFuture<>();
    CompletableFuture.runAsync(() -> {
      Comments result;
      try {
        result = HttpRequestBuilder.get(url)
            .route(API)
            .route(REVIEW_REQUESTS)
            .route(reviewRequestId)
            .route(DIFFS)
            .route(revision)
            .route(FILES)
            .route(fileId)
            .route(DIFF_COMMENTS)
            .slash()
            .asModel(Comments.class);
        future.complete(result);
      } catch (IOException | URISyntaxException e) {
        future.completeExceptionally(e);
      }
    }, executorService);
    return future;
  }

  @Override
  public CompletableFuture<Comments> comments(@NotNull Integer reviewRequestId, @NotNull Integer startRevision,
      @NotNull Integer endRevision, Integer fileId) {
    CompletableFuture<Comments> future = new CompletableFuture<>();
    CompletableFuture.runAsync(() -> {
      Comments result;
      try {
        result = HttpRequestBuilder.get(url)
            .route(API)
            .route(REVIEW_REQUESTS)
            .route(reviewRequestId)
            .route(DIFFS)
            .route(startRevision)
            .route(FILES)
            .route(fileId)
            .route(DIFF_COMMENTS)
            .queryString("interdiff-revision", endRevision)
            .slash()
            .asModel(Comments.class);
        future.complete(result);
      } catch (IOException | URISyntaxException e) {
        future.completeExceptionally(e);
      }
    }, executorService);
    return future;
  }

  @Override
  public CompletableFuture<Diffs> diffs(Integer reviewRequestId) {
    CompletableFuture<Diffs> future = new CompletableFuture<>();
    CompletableFuture.runAsync(() -> {
      Diffs result;
      try {
        result = HttpRequestBuilder.get(url)
            .route(API)
            .route(REVIEW_REQUESTS)
            .route(reviewRequestId)
            .route(DIFFS)
            .slash()
            .asModel(Diffs.class);
        future.complete(result);
      } catch (IOException | URISyntaxException e) {
        future.completeExceptionally(e);
      }
    }, executorService);
    return future;
  }

  @Override
  public CompletableFuture<Void> createComment(Comment comment, Integer reviewRequestId, Integer reviewId,
      Integer fileId) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    CompletableFuture.runAsync(() -> {
      try {
        HttpRequestBuilder.post(url)
            .route(API)
            .route(REVIEW_REQUESTS)
            .route(reviewRequestId)
            .route(REVIEWS)
            .route(reviewId)
            .route(DIFF_COMMENTS)
            .slash()
            .field("text", comment.getText())
            .field("filediff_id", fileId)
            .field("num_lines", 1)
            .field("first_line", comment.getFirstLine())
            .header(AUTHORIZATION, getAuthorizationHeader())
            .asString();
        future.complete(null);
      } catch (IOException | URISyntaxException e) {
        future.completeExceptionally(e);
      }
    }, executorService);
    return future;
  }

  @Override
  public CompletableFuture<Integer> submitReview(ReviewRequest reviewRequest, List<Comments> commentsList,
      boolean shipIt, String description) {
    return createReview(reviewRequest.getId()).thenCompose(review -> {
      if (review == null) {
        return CompletableFuture.completedFuture(null);
      }
      List<CompletableFuture> futures = new ArrayList<>();
      for (Comments comments : commentsList) {
        if (comments == null) {
          return null;
        }
        futures.addAll(comments.stream()
            .map(comment -> createComment(comment, reviewRequest.getId(), review.getId(), comments.getFileId()))
            .collect(Collectors.toList()));
      }

      return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).thenApply(c -> review);
    }).thenCompose(review -> updateReviewApi(reviewRequest.getId(), review.getId(), true, description, null, shipIt));
  }

  private static <T> CompletableFuture<T> withAuthentication(CompletableFuture<T> future/*,
//      Function<Throwable, ? extends CompletableFuture<T>> fallback*/) {
    return future;
//    return future.handle((response, error) -> error)
//        .thenCompose(error -> {
//          if (error == null){
//            return future;
//          }else {
//            AuthenticationDialog dialog = new AuthenticationDialog(PopupUtil.getActiveComponent(), "Authneticate", "Provide your username and password", "username", "password", true);
//            if(dialog.showAndGet()) {
//              return null;
//            }else {
//              return null;
//            }
//          }
//
//        });
  }

  private String getAuthorizationHeader() {
    System.out.println("Using username " + userName +" and password " +password);
    return getAuthorizationHeader(userName, password);
  }

  private String getAuthorizationHeader(String userName, String password) {
    return "Basic " + Base64.encodeBase64String((userName + ":" + password).getBytes());
  }

}

class ReviewWrapper extends Model {
  Review review;
}
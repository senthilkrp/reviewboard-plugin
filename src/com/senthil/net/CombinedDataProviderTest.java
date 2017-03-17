package com.senthil.net;

import com.senthil.model.Comments;
import com.senthil.model.Diffs;
import com.senthil.model.ReviewFile;
import com.senthil.model.ReviewFiles;
import com.senthil.model.Repositories;
import com.senthil.model.ReviewRequests;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


/**
 * Created by spanneer on 1/26/17.
 */
public class CombinedDataProviderTest{

  protected ReviewDataProvider offlineProvider;
  protected ReviewDataProvider onlineProvider;
  protected ReviewDataProvider provider;

  @BeforeMethod
  public void setup() {
    provider = new CombinedReviewDataProvider();
    offlineProvider = new OfflineReviewDataProvider();
    onlineProvider = new OnlineReviewDataProvider();
  }

  @Test
  public void testRepositoriesOffline() throws ExecutionException, InterruptedException {
    Repositories repos = provider.repositories().get();
    Assert.assertNotNull(repos);
    Assert.assertFalse(repos.isStale());
    //sleep for sometime so that the offline save completes
    Thread.sleep(10000);

    //check if the write is successful.
    Repositories offlineRepos = offlineProvider.repositories().get();
    Assert.assertTrue(offlineRepos.isStale());
    Assert.assertEquals(repos.size(), offlineRepos.size());

  }

  @Test
  public void testListReviews() throws ExecutionException, InterruptedException {
    ReviewRequests reviews = provider.reviewRequests(5503).get();
    //sleep for sometime so that the offline save completes
    Thread.sleep(10000);
    Assert.assertNotNull(reviews);
    ReviewRequests staleReviews = offlineProvider.reviewRequests(5503).get();
    Assert.assertNotNull(staleReviews);
    Assert.assertEquals(reviews.size(), staleReviews.size());
  }

//  @Test
//  public void testSearch() throws ExecutionException, InterruptedException, TimeoutException {
//    SearchResultList results = provider.search("articles").get();
//    Assert.assertNotNull(results);
//    Assert.assertFalse(results.isStale());
//    results.stream().forEach(Assert::assertNotNull);
//    Thread.sleep(5000);
//    SearchResultList offlineResults = offlineProvider.search("articles").get();
//    Assert.assertNotNull(offlineResults);
//    offlineResults.stream().forEach(Assert::assertNotNull);
//    Assert.assertTrue(offlineResults.isStale());
//
//  }

  @Test
  public void testDiffs() throws ExecutionException, InterruptedException {

    Diffs files = provider.diffs(918261).get();
    Assert.assertNotNull(files);
    Assert.assertFalse(files.isStale());

    Thread.sleep(10000);

    Diffs offlineFiles = offlineProvider.diffs(918261).get();
    Assert.assertNotNull(offlineFiles);
    Assert.assertTrue(offlineFiles.isStale());

  }

  @Test
  public void testFiles() throws ExecutionException, InterruptedException {

    ReviewFiles files = provider.files(918261, 1).get();
    Assert.assertNotNull(files);
    Assert.assertFalse(files.isStale());

    Thread.sleep(10000);

    ReviewFiles offlineFiles = offlineProvider.files(918261, 1).get();
    Assert.assertNotNull(offlineFiles);
    Assert.assertTrue(offlineFiles.isStale());
  }

  @Test
  public void testComments() throws ExecutionException, InterruptedException {
    Comments comments = provider.comments(918261, 1, 20983124).get();
    org.testng.Assert.assertNotNull(comments);
    org.testng.Assert.assertEquals(comments.size(), 10);

    Thread.sleep(10000);

    Comments offlineComments = offlineProvider.comments(918261, 1, 20983124).get();
    Assert.assertNotNull(offlineComments);
    Assert.assertTrue(offlineComments.isStale());
  }

  @Test
  public void testFileDiffs() throws ExecutionException, InterruptedException {
    CompletableFuture<ReviewFiles> minFuture = onlineProvider.files(921040, 2);
    CompletableFuture<ReviewFiles> maxFuture = onlineProvider.files(921040, 3);
    minFuture.thenAcceptBoth(maxFuture, (minFiles, maxFiles)->{
      List<? extends ReviewFile> originalFiles = minFiles.asList();
      List<? extends ReviewFile> patchFiles = maxFiles.asList();

      originalFiles.removeAll(patchFiles);
      originalFiles.forEach(file -> {
        System.out.println(file.getSourceFile());
      });

    }).get();

  }
}

package com.senthil.net;

import com.senthil.model.Comments;
import com.senthil.model.Diffs;
import com.senthil.model.Repositories;
import com.senthil.model.ReviewFiles;
import com.senthil.model.ReviewRequests;
import java.util.concurrent.ExecutionException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created by spanneer on 1/26/17.
 */
public class OfflineReviewDataProviderTest {
  OfflineReviewDataProvider provider = new OfflineReviewDataProvider();
  @BeforeMethod
  public void setup() {
    provider = new OfflineReviewDataProvider();
  }

  @Test
  public void testRepositoriesOffline() throws ExecutionException, InterruptedException {
    Repositories offlineRepos = provider.repositories().get();
    Assert.assertNotNull(offlineRepos);
    System.out.println(offlineRepos);
  }

  @Test
  public void testReviews() throws ExecutionException, InterruptedException {
    ReviewRequests onlineReviews = provider.reviewRequests(8062).get();
    Assert.assertNotNull(onlineReviews);
    org.testng.Assert.assertTrue(onlineReviews.size() >= 1);
  }
//
//  @Test
//  public void testSearch() throws ExecutionException, InterruptedException {
//    SearchResultList results = provider.search("plugin").get();
//    Assert.assertNotNull(results);
//  }

  @Test
  public void testDiffs() throws ExecutionException, InterruptedException {
    Diffs results = provider.diffs(830260).get();
    Assert.assertNotNull(results);
  }

  @Test
  public void testFiles() throws ExecutionException, InterruptedException {
    ReviewFiles results = provider.files(828920, 1).get();
    Assert.assertNotNull(results);
    Assert.assertEquals(results.size(), 25);
  }

  @Test
  public void testComments() throws ExecutionException, InterruptedException {
    Comments comments = provider.comments(918261, 1, 20983124).get();
    Assert.assertNotNull(comments);
  }

}

package com.senthil.net;

import com.senthil.model.Comments;
import com.senthil.model.Review;
import com.senthil.model.ReviewFile;
import com.senthil.model.ReviewFiles;
import com.senthil.model.Repositories;
import com.senthil.model.ReviewRequest;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class OnlineReviewDataProviderTest {

  protected ReviewDataProvider provider;

  @BeforeMethod
  public void setUp() {
    provider = new OnlineReviewDataProvider();
  }

//  @Test
//  public void testUsers() throws ExecutionException, InterruptedException {
//    RBUserList users = provider.users("spanneer").get();
//    Assert.assertNotNull(users);
//    Assert.assertEquals(1, users.size());
//    Assert.assertEquals("Senthilkumar", users.get(0).first_name);
//    Assert.assertNotNull(users.getResponse());
//  }

  @Test
  public void testRepositories() throws ExecutionException, InterruptedException {
    Repositories repos = provider.repositories().get();
    Assert.assertNotNull(repos);
    Assert.assertTrue(repos.size() > 100);
  }

  @Test
  public void testSearch() throws ExecutionException, InterruptedException {
    List<ReviewRequest> results = provider.search(5503, "articles").get();
    Assert.assertNotNull(results);
  }

  @Test
  public void testReviews() throws ExecutionException, InterruptedException {
    Assert.assertNotNull(provider.reviewRequests(5503).get());
    Assert.assertNotNull(provider.reviewRequests(5503, "pending", new Date()).get());
  }

  @Test
  public void testFiles() throws ExecutionException, InterruptedException {
    ReviewFiles files = provider.files(918261, 1).get();
    Assert.assertNotNull(files);
    Assert.assertEquals(files.size() , 25);
    ReviewFile file = files.get(0);
    Assert.assertEquals(file.getPatchedFile(), "https://rb.corp.linkedin.com/api/review-requests/918261/diffs/1/files/20983124/patched-file/");
    Assert.assertEquals(file.getOriginalFile(), "https://rb.corp.linkedin.com/api/review-requests/918261/diffs/1/files/20983124/original-file/");
  }

  @Test
  void testContents() throws ExecutionException, InterruptedException {
//    Assert.assertEquals(provider.contents("https://rb.corp.linkedin.com/api/review-requests/918261/diffs/1/files/20983124/patched-file/").get().length(), 2915);
    Assert.assertEquals(provider.contents("https://rb.corp.linkedin.com/api/review-requests/893649/diffs/1/files/20165309/original-file/").get().length(), 2915);


  }

  @Test
  public void testCreateReview() throws ExecutionException, InterruptedException {
    Review review = provider.createReview(918261).get();
    Assert.assertNotNull(review);
  }

  @Test
  public void testComments() throws ExecutionException, InterruptedException {
    Comments comments = provider.comments(918261, 1, 20983124).get();
    Assert.assertNotNull(comments);
    Assert.assertEquals(comments.size(), 10);
  }

}

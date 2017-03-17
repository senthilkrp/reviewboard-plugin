package com.senthil.net;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.util.Key;
import com.senthil.model.Comments;
import com.senthil.model.ReviewFile;
import com.senthil.model.ReviewRequest;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;


/**
 * Created by spanneer on 1/26/17.
 */
public class Constants {
  public static final NotificationGroup REVIEW_NOTIFICATION_GROUP =
      new NotificationGroup("offlinereview.notification", NotificationDisplayType.BALLOON, false);
  public static final String REPOSITORY_SAVE_PATH = "repositories.json";
  public static final MessageFormat REVIEW_REQUESTS_SAVE_PATH = new MessageFormat("review_requests/{0,number,####}.json");
  public static final MessageFormat FILES_SAVE_PATH = new MessageFormat("files/{0,number,####}/{1,number,####}.json");
  public static final MessageFormat DIFF_SAVE_PATH = new MessageFormat("diff/{0,number,####}.json");
  public static final MessageFormat COMMENTS_SAVE_PATH = new MessageFormat("comments/{0,number,####}/{1,number,####}/{2,number,####}.json");;
  public static final MessageFormat CONTENT_SAVE_PATH = new MessageFormat("contents/{0,number,####}/{1,number,####}/{2,number,####}_{3}.json");;
  public static final Key<ReviewRequest> REVIEW = new Key<>("reviewRequest");
  public static final Pattern DIFF_PATTERN = Pattern.compile("(://.*/api/review-requests/)([0-9]*)(/diffs/)$");;
  public static final Pattern FILE_LIST_PATTERN = Pattern.compile("(://.*/api/review-requests/)([0-9]*)(/diffs/)([0-9]*)(/files/)$");
  public static final Pattern FILE_PATTERN = Pattern.compile("(://.*/api/review-requests/)([0-9]*)(/diffs/)([0-9]*)(/files/)([0-9]*)/$");;
  public static final Pattern COMMENT_PATTERN = Pattern.compile("(://.*/api/review-requests/)([0-9]*)(/diffs/)([0-9]*)(/files/)([0-9]*)(/diff-comments/)$");
  public static final Pattern CONTENT_PATTERN = Pattern.compile("(://.*/api/review-requests/)([0-9]*)(/diffs/)([0-9]*)(/files/)([0-9]*)/([a-zA-Z0-9\\-]*)-file/$");
  public static final Pattern SUBMITTER_PATTERN = Pattern.compile("(://.*/api/users/)(.*)/$");
  public static Key<CompletableFuture<Comments>> COMMENTS_KEY = new Key<>("comments");
  public static Key<ReviewFile> FILE_KEY = new Key<>("file");
  //6000 ms
  public static final Integer REFERESH_TIME = 18000; //in ms


}

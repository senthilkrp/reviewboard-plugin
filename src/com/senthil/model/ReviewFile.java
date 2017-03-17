package com.senthil.model;

import com.google.gson.annotations.SerializedName;
import com.senthil.net.Constants;
import java.io.File;
import java.util.List;
import java.util.regex.Matcher;


/**
 * Created by spanneer on 2/22/17.
 */
public class ReviewFile extends Model implements Writable{
  @SerializedName("dest_file")
  private String destinationFile;

  @SerializedName("source_file")
  private String sourceFile;

  @SerializedName("source_revision")
  private String sourceRevision;

  @SerializedName("extra_data")
  private ExtraData extraData;

  private ReviewRequest reviewRequest;

  private Links links;

  private String revision;

  private List<Comment> comments;

  public String getDestinationFile() {
    return destinationFile;
  }

  public String getSourceFile() {
    return sourceFile;
  }

  public String getSourceRevision() {
    return sourceRevision;
  }

  public List<Comment> getComments() {
    return comments;
  }

  @Override
  public String toString() {
    return "ReviewFile{" + "destinationFile='" + destinationFile + '\'' + ", sourceFile='" + sourceFile + '\''
        + ", extraData=" + extraData + '}';
  }

  public String getDiffRevision() {
    Matcher matcher = Constants.FILE_PATTERN.matcher(links.self.getHref());
    if(matcher.find()) {
      return matcher.group(4);
    }
    return null;
  }

  public String getOriginalFile() {
    return links.original_file.getHref();
  }

  public String getPatchedFile() {
    return links.patched_file.getHref();
  }

  public String getOriginalSha() {
    return extraData.orig_sha1;
  }

  public String getPatchedSha() {
    return extraData.patched_sha1;
  }

  @Override
  public String getPath() {
    return "unchecked" + File.separator + reviewRequest.getId() + File.separator + id + File.separator + "file.json";
  }

  class Links{
    Link original_file;
    Link patched_file;
    Link self;
    @Override
    public String toString() {
      return "Links{" + "original_file=" + original_file + ", patched_file=" + patched_file + '}';
    }
  }


  class ExtraData {
    String orig_sha1;
    String patched_sha1;
    int delete_count;

    @Override
    public String toString() {
      return "ExtraData{" + "orig_sha1='" + orig_sha1 + '\'' + ", patched_sha1='" + patched_sha1 + '\''
          + ", delete_count=" + delete_count + '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      ExtraData extraData = (ExtraData) o;

      if (orig_sha1 != null ? !orig_sha1.equals(extraData.orig_sha1) : extraData.orig_sha1 != null) {
        return false;
      }
      return patched_sha1 != null ? patched_sha1.equals(extraData.patched_sha1) : extraData.patched_sha1 == null;
    }

    @Override
    public int hashCode() {
      int result = orig_sha1 != null ? orig_sha1.hashCode() : 0;
      result = 31 * result + (patched_sha1 != null ? patched_sha1.hashCode() : 0);
      return result;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ReviewFile that = (ReviewFile) o;

    return extraData != null ? extraData.equals(that.extraData) : that.extraData == null;
  }

  @Override
  public int hashCode() {
    return extraData != null ? extraData.hashCode() : 0;
  }
}

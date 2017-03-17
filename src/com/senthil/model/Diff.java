package com.senthil.model;

/**
 * Created by spanneer on 2/22/17.
 */
public class Diff extends Model {

  private Integer revision;
  private String name;

  public Integer getRevision() {
    return revision;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "Diff{" + "revision=" + revision + ", name='" + name + '\'' + "} " + super.toString();
  }
}
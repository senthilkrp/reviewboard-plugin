package com.senthil.model;

import java.util.List;


/**
 * Created by spanneer on 2/22/17.
 */
public class Repositories extends IterableModel<Repository> implements Writable{

  private List<Repository> repositories;

  @Override
  protected List<Repository> getCollection() {
    return repositories;
  }

  @Override
  public String toString() {
    return repositories.toString();
  }

  @Override
  public String getPath() {
    return "repositories.json";
  }

  public void addAll(Repositories repos) {
    repositories.addAll(repos.asList());
  }
}

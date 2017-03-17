package com.senthil.model;

import org.jetbrains.annotations.NotNull;


/**
 * Created by spanneer on 2/22/17.
 */
public class Repository extends Model implements Comparable<Repository>{

  private String name;

  private String tool;

  private String path;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTool() {
    return tool;
  }

  public void setTool(String tool) {
    this.tool = tool;
  }

  public String getRepoPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public String toString() {
    return "Repository{" + "id=" + id + ", name='" + name + '\'' + ", tool='" + tool + '\'' + ", path='" + path + '\''
        + '}';
  }

  @Override
  public int compareTo(@NotNull Repository o) {
    return getName().compareTo(o.getName());
  }
}

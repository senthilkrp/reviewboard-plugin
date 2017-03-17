package com.senthil.model;

import com.senthil.utils.OfflineUtils;


/**
 * Created by spanneer on 1/26/17.
 */
public interface Writable {

  class Extensions {
    private static boolean stale = false;
  }

  default boolean isStale() {
    return Extensions.stale;
  }

  default void write() {
    writeTo(getPath());
  }

  String getPath();

  default void setStale(boolean isStale) {
    Extensions.stale = isStale;
  }

  default void writeTo(String relativePath) {
    OfflineUtils.write(this, relativePath);
  }

  default boolean delete() {
    return OfflineUtils.delete(getPath());
  }

}


package com.senthil.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.Date;


/**
 * Created by spanneer on 2/22/17.
 */
public class Model{

  private boolean stale;

  protected Integer id;

  private Integer count;

  private String stat;

  private Error err;

  private String response;

  public Integer getId() {
    return id;
  }

  public String getStat() {
    return stat;
  }

  public Error getErr() {
    return err;
  }

  public void setResponse(String response) {
    this.response = response;
  }

  public Integer getCount() {
    return count;
  }

  public class Error {
    private String msg;
    private int code;

    public String getMsg() {
      return msg;
    }

    public int getCode() {
      return code;
    }
  }
}





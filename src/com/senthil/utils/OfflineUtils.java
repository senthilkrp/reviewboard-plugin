package com.senthil.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.senthil.model.Writable;
import com.senthil.net.Constants;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;


/**
 * Created by spanneer on 1/26/17.
 */
public class OfflineUtils {

  private static final Gson GSON = new GsonBuilder().create();
  private static final JsonParser jsonParser = new JsonParser();

  private static final File TEMP_DIR = new File(System.getProperty("user.home"), "OfflineReview");

  public static <T extends Writable> T read(String path, Class<T> clazz) throws FileNotFoundException {
    File file = getFile(path);
    System.out.println("Reading from file " + file.getAbsolutePath());
    try {
      T retValue = GSON.fromJson(new JsonReader(new FileReader(file)), clazz);
      long lastModifiedTime = file.lastModified();
      if (System.currentTimeMillis() - lastModifiedTime > Constants.REFERESH_TIME) {
        retValue.setStale(true);
      }
      return retValue;
    } catch (FileNotFoundException e) {
      return null;
    }
  }

  public static File getFile(File file) {

    if (file.isAbsolute()) {
      return file;
    }
    if (file.getParent() != null) {
      new File(TEMP_DIR, file.getParent()).mkdirs();
    }
    return new File(TEMP_DIR, file.getPath());
  }

  public static void write(String response, String path) throws IOException {
    write(response, new File(path));
  }

  public static void write(String response, File file) throws IOException {
    File existingFile = getFile(file);
    System.out.println("Writing to file " + file.getAbsolutePath());
    if (existingFile.exists()) {
      if (!existingFile.delete()) {
        throw new RuntimeException("Cannot delete file " + existingFile.getAbsolutePath());
      }
    }

    JsonWriter writer = new JsonWriter(new FileWriter(existingFile));
    try {
      JsonElement obj = jsonParser.parse(response);
      GSON.toJson(obj, writer);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      writer.flush();
      writer.close();
    }
  }

  public static <T extends Writable> void write(T object, String path) {
    File existingFile = getFile(path);
    System.out.println("Writing to file " + existingFile.getAbsolutePath());
    if (existingFile.exists()) {
      if (!existingFile.delete()) {
        throw new RuntimeException("Cannot delete file " + existingFile.getAbsolutePath());
      }
    }

    try (BufferedWriter br = new BufferedWriter(new FileWriter(existingFile))) {
      GSON.toJson(object, br);
      br.flush();
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static boolean delete(File path) {
    File file = getFile(path);
    if (file.isDirectory()) {
      return deleteDirectory(file);
    }
    return file.delete();
  }

  private static boolean deleteDirectory(File folder) {
    File[] files = folder.listFiles();
    if (files != null) { //some JVMs return null for empty dirs
      for (File f : files) {
        if (f.isDirectory()) {
          return deleteDirectory(f);
        } else {
          return f.delete();
        }
      }
    }
    return folder.delete();
  }

  public static boolean delete(String path) {
    return getFile(path).delete();
  }

  public static File getFile(String path) {
    return getFile(new File(path));
  }

  public static File[] listFiles(File drafts, FilenameFilter filenameFilter) {
    return getFile(drafts).listFiles(filenameFilter);
  }
}

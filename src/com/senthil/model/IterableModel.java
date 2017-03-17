package com.senthil.model;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;


/**
 * Created by spanneer on 2/22/17.
 */
public abstract class IterableModel<T> extends Model{

  protected abstract List<T> getCollection();

  public Stream<T> stream() {
    return getCollection().stream();
  }

  public void forEach(Consumer<? super T> action) {
     getCollection().forEach(action);
  }


  public int size() {
    return getCollection().size();
  }


  public boolean isEmpty() {
    return getCollection().isEmpty();
  }


  public boolean contains(Object o) {
    return getCollection().isEmpty();
  }

  @NotNull

  public Iterator<T> iterator() {
    return getCollection().iterator();
  }

  @NotNull

  public <T1> T1[] toArray(@NotNull T1[] a) {
    return getCollection().toArray(a);
  }


  public boolean add(@Flow(targetIsContainer = true) T t) {
    return getCollection().add(t);
  }


  public boolean remove(Object o) {
    return getCollection().remove(o);
  }


  public T get(int index) {
    return getCollection().get(index);
  }

  public String toString() {
    return getCollection().toString();
  }

  public List<T> asList() {
    return getCollection();
  }
}

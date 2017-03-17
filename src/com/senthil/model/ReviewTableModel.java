package com.senthil.model;

import javax.swing.table.AbstractTableModel;


public class ReviewTableModel extends AbstractTableModel {


  public enum Columns {
    SUMMARY(0, "Summary"),
    SUBMITTER(1, "Submitter"),
    STATUS(2, "Status"),
    LAST_MODIFIED(3, "Last Modified");

    private int index;
    private String name;

    Columns(int index, String name) {
      this.index = index;
      this.name = name;
    }

    public int getIndex() {
      return index;
    }

    public String getName() {
      return name;
    }
  }

  private final ReviewRequests reviews;
  private final String[] columnNames =
      {Columns.SUMMARY.getName(), Columns.SUBMITTER.getName(), Columns.STATUS.getName(), Columns.LAST_MODIFIED.getName()};

  @Override
  public String getColumnName(int column) {
    return columnNames[column];
  }

  public ReviewTableModel(ReviewRequests reviews) {
    this.reviews = reviews;
  }

  @Override
  public int getRowCount() {
    return reviews.size();
  }

  @Override
  public int getColumnCount() {
    return columnNames.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    switch (columnIndex) {
      case 0:
        return reviews.get(rowIndex).getSummary();
      case 1:
        return reviews.get(rowIndex).getName();
      case 2:
        return reviews.get(rowIndex).getStatus();
      case 3:
        return reviews.get(rowIndex).getLastUpdatedTime();
    }
    return null;
  }

  public ReviewRequest getReview(int index) {
    return reviews == null ? null : reviews.get(index);
  }
}

package com.senthil.ui;

import com.intellij.openapi.project.Project;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.senthil.model.ReviewRequests;
import com.senthil.model.ReviewTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/**
 * Created by spanneer on 2/2/17.
 */
public class ReviewPanel extends JBSplitter {

  private final Project project;
  private JBTable reviewsTable;
  private SummaryPanel summaryPanel;

  public ReviewPanel(Project project, ReviewRequests reviews) {
    this.project = project;
    setLayout(new BorderLayout());
    summaryPanel = new SummaryPanel(project);
    reviewsTable = new JBTable(new ReviewTableModel(reviews == null ? new ReviewRequests() : reviews));
    addListeners();
    setFirstComponent(new JBScrollPane(reviewsTable));
    setSecondComponent(summaryPanel);
  }

  public ReviewPanel(Project project) {
    this.project = project;
  }

  private void addListeners() {
    reviewsTable.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        reviewsTable.setEnabled(false);
        int selectedRow = reviewsTable.getSelectedRow();
        ReviewTableModel model = (ReviewTableModel) reviewsTable.getModel();
        summaryPanel.setReview(model.getReview(selectedRow)).thenAccept(v->{
          reviewsTable.setEnabled(true);
        });
      }
    });
  }
}
/*
 * Copyright 2015 Ritesh Kapoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.senthil.ui.panels;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.ComboboxSpeedSearch;
import com.intellij.ui.speedSearch.SpeedSearch;
import com.senthil.model.Repositories;
import com.senthil.model.Repository;
import com.senthil.net.ReviewFactory;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import java.awt.event.ActionListener;
import org.jetbrains.annotations.NotNull;


/**
 * @author Ritesh
 */
public class LoginPanel {
    private JPanel panel;
    private JPasswordField password;
    private JTextField username;
    private JButton testConnection;
    private JTextField reviewBoardUrl;
    private JComboBox<Repository> repositoryCombo;
    private JButton getRepositoriesBtn;
    private Repository repository;

    public LoginPanel() {
        getRepositoriesBtn.setIcon(AllIcons.Actions.Refresh);
        getRepositoriesBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                refresh();
            }
        });
        repositoryCombo.setLightWeightPopupEnabled(true);
        new ComboboxSpeedSearch(repositoryCombo);
    }

    public void addActionListener(ActionListener l) {
        testConnection.addActionListener(l);
    }

    public JComponent getPanel() {
        return panel;
    }

    public String getUsername() {
        return username.getText().trim();
    }

    public String getPassword() {
        return String.valueOf(password.getPassword());
    }

    public void setUsername(String username) {
        this.username.setText(username);
    }

    public void setPassword(String password) {
        this.password.setText(password);
    }

    public String getReviewBoardUrl() {
        return reviewBoardUrl.getText();
    }

    public void setReviewBoardUrl(String reviewBoardUrl) {
        this.reviewBoardUrl.setText(reviewBoardUrl);
    }

    public Repository getRepository() {
        return (Repository) repositoryCombo.getSelectedItem();
    }

    public void refresh() {
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                Integer total = ReviewFactory.getInstance().noOfRepositories(getReviewBoardUrl(), getUsername(), getPassword()).get();
                CompletableFuture<Repositories> completableFuture = null;
                for (int i = 0; i < total; i = i + 100) {
                    CompletableFuture<Repositories> newFuture = ReviewFactory.getInstance()
                        .repositories(getReviewBoardUrl(), getUsername(), getPassword(), i, 100);
                    if(completableFuture == null) {
                        completableFuture = newFuture;
                    }else {
                        completableFuture = completableFuture.thenCombine(newFuture, (val1, val2) -> {
                            val2.addAll(val1);
                            return val2;
                        });
                    }
                }

                Repositories repos = completableFuture.get();
                Repository[] reposArray = new Repository[repos.size()];
                reposArray = repos.toArray(reposArray);
                Arrays.sort(reposArray);
                repositoryCombo.setModel(new DefaultComboBoxModel<>(reposArray));
                if (repository != null) {
                    repositoryCombo.setSelectedItem(repository);
                }
                repositoryCombo.setRenderer(new ColoredListCellRenderer<Repository>() {
                    @Override
                    protected void customizeCellRenderer(@NotNull JList<? extends Repository> list,
                        Repository value, int index, boolean selected, boolean hasFocus) {
                        append(value.getName());
                    }
                });
            } catch (InterruptedException | ExecutionException e1) {
                e1.printStackTrace();
            }
        });

    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }
}

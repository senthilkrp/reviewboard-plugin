package com.senthil.state;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

@State(
        name = "Offline Review",
        reloadable = true,
        storages = {
                @Storage(id = "default", file = "$PROJECT_FILE$"),
                @Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/offline_review.xml",
                        scheme = StorageScheme.DIRECTORY_BASED)
        }
)
public class ConfigurationPersistence implements PersistentStateComponent<Configuration> {

    private ConfigurationPersistence() {
    }

    @Nullable
    public static ConfigurationPersistence getInstance(Project project) {
        return ServiceManager.getService(project, ConfigurationPersistence.class);
    }

    private Configuration state;

    @Nullable
    @Override
    public Configuration getState() {
        return state;
    }

    @Override
    public void loadState(Configuration state) {
        this.state = state;
    }
}

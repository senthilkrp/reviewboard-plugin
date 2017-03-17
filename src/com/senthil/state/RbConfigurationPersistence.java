package com.senthil.state;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;


@State(
    name = "rbConfiguration",
    reloadable = true,
    storages = {
        @Storage(id = "default", file = "$PROJECT_FILE$"),
        @Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/review_board_plugin_settings_v0_1.xml",
            scheme = StorageScheme.DIRECTORY_BASED)
    }
)
public class RbConfigurationPersistence implements PersistentStateComponent<RbConfiguration> {

  private RbConfiguration state;

  @Nullable
  public static RbConfigurationPersistence getInstance(Project project) {
    return ServiceManager.getService(project, RbConfigurationPersistence.class);
  }

  @Nullable
  @Override
  public RbConfiguration getState() {
    return state;
  }

  @Override
  public void loadState(RbConfiguration state) {
    this.state = state;
  }
}

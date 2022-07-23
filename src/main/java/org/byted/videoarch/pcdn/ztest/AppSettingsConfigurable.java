package org.byted.videoarch.pcdn.ztest;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

public class AppSettingsConfigurable implements Configurable {

    private AppSettingsComponent mySettingsComponent;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Z-Test Generator";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new AppSettingsComponent();

        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        return !mySettingsComponent.getZTestPathText().equals(AppSettingsState.getInstance().zTestPath);
    }

    @Override
    public void apply() {
        AppSettingsState.getInstance().zTestPath = mySettingsComponent.getZTestPathText();
    }

    @Override
    public void reset() {
        mySettingsComponent.setZTestPathText(AppSettingsState.getInstance().zTestPath);
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

}

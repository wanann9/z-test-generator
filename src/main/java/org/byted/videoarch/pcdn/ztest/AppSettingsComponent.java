package org.byted.videoarch.pcdn.ztest;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;

public class AppSettingsComponent {

    private final JPanel myMainPanel;
    private final JBTextField myZTestPathText = new JBTextField();

    public AppSettingsComponent() {
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Path"), myZTestPathText, 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    @NotNull
    public String getZTestPathText() {
        return myZTestPathText.getText();
    }

    public void setZTestPathText(@NotNull String newText) {
        myZTestPathText.setText(newText);
    }

}

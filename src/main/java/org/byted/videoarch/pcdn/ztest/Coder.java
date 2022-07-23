package org.byted.videoarch.pcdn.ztest;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Coder extends AnAction {

    private String dstFilePath;
    private int dstFuncOffset;

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            notify("no project", NotificationType.ERROR);

            return;
        }

        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        if (fileEditorManager == null) {
            notify("no file editor manager", NotificationType.ERROR);

            return;
        }

        Editor editor = fileEditorManager.getSelectedTextEditor();
        if (editor == null) {
            notify("no selected text editor", NotificationType.ERROR);

            return;
        }

        String funcName = getFuncName(editor.getSelectionModel());

        FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
        fileDocumentManager.saveAllDocuments();

        VirtualFile srcFile = fileDocumentManager.getFile(editor.getDocument());
        if (srcFile == null) {
            notify("no source file", NotificationType.ERROR);

            return;
        }

        String srcFilePath = srcFile.getPath();
        if (srcFilePath.isEmpty()) {
            notify("empty file path", NotificationType.ERROR);

            return;
        }

        Process process;
        try {
            process = Runtime.getRuntime().exec(getCommands(srcFilePath, funcName));
            process.waitFor();
        } catch (IOException | InterruptedException exception) {
            notify(exception.toString(), NotificationType.ERROR);

            return;
        }

        String input, error;
        try {
            input = getContent(process.getInputStream());
            error = getContent(process.getErrorStream());
        } catch (IOException exception) {
            notify(exception.toString(), NotificationType.ERROR);

            return;
        }

        if (input != null && !input.isEmpty()) {
            notify(input, NotificationType.INFORMATION);

            parseInput(input);
            if (dstFilePath != null && !dstFilePath.isEmpty()) {
                VirtualFile dstFile = srcFile.getFileSystem().refreshAndFindFileByPath(dstFilePath);
                if (dstFile != null) {
                    dstFile.refresh(false, false);
                    fileEditorManager.openTextEditor(new OpenFileDescriptor(project, dstFile, dstFuncOffset), true);
                }
            }
        }

        if (error != null && !error.isEmpty()) {
            notify(error, NotificationType.ERROR);
        }
    }

    private void notify(String content, NotificationType type) {
        Notifications.Bus.notify(new Notification("ng", "ZTest", content, type));
    }

    private String getFuncName(SelectionModel selectionModel) {
        String selectedText = selectionModel.getSelectedText();
        if (selectedText == null || selectedText.isEmpty()) {
            return "";
        }

        return selectedText.replaceFirst("\\s*\\)\\s*", ":");
    }

    private String[] getCommands(String filePath, String funcName) {
        String zTestPath = AppSettingsState.getInstance().zTestPath;

        return new String[]{"/bin/sh", "-c", zTestPath + " -p \"" + filePath + "\" -f \"" + funcName + "\""};
    }

    private String getContent(InputStream inputStream) throws IOException {
        String content = "";
        if (inputStream == null) {
            return content;
        }

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        for (String line; (line = bufferedReader.readLine()) != null; ) {
            content = content.concat(line + "\n");
        }

        return content;
    }

    private void parseInput(String input) {
        dstFilePath = "";
        dstFuncOffset = 0;

        Pattern pattern = Pattern.compile("generate test functions in file `(.+)` \\(offset (\\d+)\\)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            dstFilePath = matcher.group(1);
            dstFuncOffset = Integer.parseInt(matcher.group(2));
        }
    }

}

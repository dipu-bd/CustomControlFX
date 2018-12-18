/*
 * Copyright 2015 dipu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sandsoft.components.htmleditor;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

/**
 *
 * @author dipu
 */
public class CustomHTMLEditor extends HTMLEditor {

    public static final String TOP_TOOLBAR = ".top-toolbar";
    public static final String BOTTOM_TOOLBAR = ".bottom-toolbar";
    public static final String WEB_VIEW = ".web-view";
    private static final String IMPORT_BUTTON_GENERAL = "embed.png";

    private final WebView mWebView;
    private final ToolBar mTopToolBar;
    private final ToolBar mBottomToolBar;
    private Button mImportFileButton;

    public CustomHTMLEditor() {
        mWebView = (WebView) this.lookup(WEB_VIEW);
        mTopToolBar = (ToolBar) this.lookup(TOP_TOOLBAR);
        mBottomToolBar = (ToolBar) this.lookup(BOTTOM_TOOLBAR);
        GridPane.setVgrow(mWebView, Priority.ALWAYS);

        createCustomButtons();
        this.setHtmlText("<html />");
    }

    /**
     * Inserts HTML data after the current cursor. If anything is selected, they
     * get replaced with new HTML data.
     *
     * @param html HTML data to insert.
     */
    public void insertHtmlAfterCursor(String html) {
        //replace invalid chars
        html = html.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
        //get script
        String script = String.format(
                "(function(html) {"
                + "  var sel, range;"
                + "  if (window.getSelection) {"
                + "    sel = window.getSelection();"
                + "    if (sel.getRangeAt && sel.rangeCount) {"
                + "      range = sel.getRangeAt(0);"
                + "      range.deleteContents();"
                + "      var el = document.createElement(\"div\");"
                + "      el.innerHTML = html;"
                + "      var frag = document.createDocumentFragment(),"
                + "        node, lastNode;"
                + "      while ((node = el.firstChild)) {"
                + "        lastNode = frag.appendChild(node);"
                + "      }"
                + "      range.insertNode(frag);"
                + "      if (lastNode) {"
                + "        range = range.cloneRange();"
                + "        range.setStartAfter(lastNode);"
                + "        range.collapse(true);"
                + "        sel.removeAllRanges();"
                + "        sel.addRange(range);"
                + "      }"
                + "    }"
                + "  }"
                + "  else if (document.selection && "
                + "           document.selection.type != \"Control\") {"
                + "    document.selection.createRange().pasteHTML(html);"
                + "  }"
                + "})(\"%s\");", html);
        //execute script
        mWebView.getEngine().executeScript(script);
    }

    /**
     * Creates Custom ToolBar buttons and other controls
     */
    private void createCustomButtons() {
        //add embed file button  
        ImageView graphic = new ImageView(new Image(
                getClass().getResourceAsStream(IMPORT_BUTTON_GENERAL)));
        mImportFileButton = new Button("Import File", graphic);
        mImportFileButton.setTooltip(new Tooltip("Import File"));
        mImportFileButton.setOnAction((event) -> onImportFileButtonAction());

        //add to top toolbar         
        mTopToolBar.getItems().add(mImportFileButton);
        mTopToolBar.getItems().add(new Separator(Orientation.VERTICAL));
    }

    /**
     * Action to do on Import Image button click
     */
    private void onImportFileButtonAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a file to import");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(this.getScene().getWindow());
        if (selectedFile != null) {
            importDataFile(selectedFile);
        }
    }

    /**
     * Imports an image file.
     *
     * @param file Image file.
     */
    private void importDataFile(File file) {
        try {
            //check if file is too big
            if (file.length() > 1024 * 1024) {
                throw new VerifyError("File is too big.");
            }
            //get mime type of the file
            String type = java.nio.file.Files.probeContentType(file.toPath());
            //get html content
            byte[] data = org.apache.commons.io.FileUtils.readFileToByteArray(file);
            String base64data = java.util.Base64.getEncoder().encodeToString(data);
            String htmlData = String.format(
                    "<embed src='data:%s;base64,%s' type='%s' />",
                    type, base64data, type);
            //insert html
            insertHtmlAfterCursor(htmlData);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }
}

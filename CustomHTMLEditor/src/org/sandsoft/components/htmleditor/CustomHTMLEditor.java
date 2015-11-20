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
    private static final String IMPORT_BUTTON_IMAGE = "image.png";

    private WebView mWebView;
    private ToolBar mTopToolBar;
    private ToolBar mBottomToolBar;
    private Button mImportImageButton;

    public CustomHTMLEditor() {
        createCustomButtons();
        this.setHtmlText("<html />");
    }

    private void createCustomButtons() {
        //identify controls
        mWebView = (WebView) this.lookup(WEB_VIEW);
        mTopToolBar = (ToolBar) this.lookup(TOP_TOOLBAR);
        mBottomToolBar = (ToolBar) this.lookup(BOTTOM_TOOLBAR);

        // add import image button
        ImageView graphic = null;
        try {
            graphic = new ImageView(new Image(
                    getClass().getResourceAsStream(IMPORT_BUTTON_IMAGE)));
        } catch (Exception ex) {
        }
        mImportImageButton = new Button("Import Image", graphic);
        mImportImageButton.setTooltip(new Tooltip("Import Image"));
        mImportImageButton.setOnAction((event) -> {
            onImportButtonAction();
        });

        //add to top toolbar        
        mTopToolBar.getItems().add(mImportImageButton);
        mTopToolBar.getItems().add(new Separator(Orientation.VERTICAL));
    }

    private void onImportButtonAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select an image file");
        FileChooser.ExtensionFilter imageFileFilter = new FileChooser.ExtensionFilter(
                "Image Files", "*.png", "*.jpg", "*.bmp", "*.gif", "*.tif", "*.tiff");
        FileChooser.ExtensionFilter allFileFilter = new FileChooser.ExtensionFilter(
                "All Files", "*.*");
        fileChooser.getExtensionFilters().add(imageFileFilter);
        fileChooser.getExtensionFilters().add(allFileFilter);
        fileChooser.setSelectedExtensionFilter(imageFileFilter);
        File selectedFile = fileChooser.showOpenDialog(this.getScene().getWindow());
        if (selectedFile != null) {
            importImageFile(selectedFile);
        }
    }

    private void importImageFile(File file) {
        try {
            //get type and alt-text
            String fileName = file.getName();
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));

            //get html content
            byte[] data = org.apache.commons.io.FileUtils.readFileToByteArray(file);
            String base64data = java.util.Base64.getEncoder().encodeToString(data);
            String htmlData = String.format(
                    "<img src='data:image/%s;base64,%s' alt='%s'>",
                    extension, base64data, fileNameWithoutExtension);

            //insert beside cursor
            String script = String.format("(function(html) {\n"
                    + "    var sel, range;\n"
                    + "    if (window.getSelection) {\n"
                    + "        // IE9 and non-IE\n"
                    + "        sel = window.getSelection();\n"
                    + "        if (sel.getRangeAt && sel.rangeCount) {\n"
                    + "            range = sel.getRangeAt(0);\n"
                    + "            range.deleteContents();\n"
                    + "            // Range.createContextualFragment() would be useful here but is\n"
                    + "            // only relatively recently standardized and is not supported in\n"
                    + "            // some browsers (IE9, for one)\n"
                    + "            var el = document.createElement(\"div\");\n"
                    + "            el.innerHTML = html;\n"
                    + "            var frag = document.createDocumentFragment(), node, lastNode;\n"
                    + "            while ( (node = el.firstChild) ) {\n"
                    + "                lastNode = frag.appendChild(node);\n"
                    + "            }\n"
                    + "            range.insertNode(frag);\n"
                    + "            // Preserve the selection\n"
                    + "            if (lastNode) {\n"
                    + "                range = range.cloneRange();\n"
                    + "                range.setStartAfter(lastNode);\n"
                    + "                range.collapse(true);\n"
                    + "                sel.removeAllRanges();\n"
                    + "                sel.addRange(range);\n"
                    + "            }\n"
                    + "        }\n"
                    + "    } else if (document.selection && document.selection.type != \"Control\") {\n"
                    + "        // IE < 9\n"
                    + "        document.selection.createRange().pasteHTML(html);\n"
                    + "    }\n"
                    + "})(\"%s\");", htmlData);

            mWebView.getEngine().executeScript(script);
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }
}

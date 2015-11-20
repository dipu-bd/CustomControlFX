/*
 * Copyright 2015 Sudipto Chandra.
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
package org.sandsoft.components.test;

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;
import org.sandsoft.components.htmleditor.CustomHTMLEditor;

/**
 * @author Sudipto Chandra.
 */
public class CustomHTMLEditorTest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        CustomHTMLEditor htmlEditor = new CustomHTMLEditor();
        htmlEditor.setMaxHeight(Double.MAX_VALUE);
        htmlEditor.setMaxWidth(Double.MAX_VALUE);
        htmlEditor.setMinWidth(0);
        htmlEditor.setMinHeight(0);
        HBox.setHgrow(htmlEditor, Priority.ALWAYS);
        VBox.setVgrow(htmlEditor, Priority.ALWAYS);

        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Consolas", 14f));

        TabPane root = new TabPane();
        root.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        root.getTabs().add(new Tab("   Visual   ", htmlEditor));
        root.getTabs().add(new Tab("   HTML   ", textArea));

        root.getSelectionModel().selectedIndexProperty().addListener((event) -> {
            textArea.setText(
                    htmlEditor.getHtmlText()
                    .replace("<", "\n<")
                    .replace(">", ">\n")
                    .replace("\n\n", "\n")
            );
        });

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("HTML Editor Test!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
} 
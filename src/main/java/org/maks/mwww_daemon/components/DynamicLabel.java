package org.maks.mwww_daemon.components;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.util.function.Consumer;

public class DynamicLabel extends StackPane {

    private final Label label = new Label();
    private final TextField textField = new TextField();

    private Consumer<String> onSearchSong = (_) -> {};

    public DynamicLabel() {
        getChildren().add(label);
        textField.setOnKeyPressed(keyEvent -> {
            switch (keyEvent.getCode()) {
                case ESCAPE -> switchToLabel();
                case ENTER -> switchSong();
            }
        });
    }

    public void switchToTextField() {
        getChildren().remove(label);
        getChildren().add(textField);
    }

    public void setText(String text) {
        label.setText(text);
    }

    private void switchSong() {
        String songName = textField.getText();

        if (songName.trim().isEmpty()) {
            switchToLabel();
        }

        onSearchSong.accept(songName);
        switchToLabel();
    }

    private void switchToLabel() {
        getChildren().remove(textField);
        getChildren().add(label);
    }

    public void setOnSearchSong(Consumer<String> onSearchSong) {
        this.onSearchSong = onSearchSong;
    }
}

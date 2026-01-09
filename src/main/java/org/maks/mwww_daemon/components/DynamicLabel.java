package org.maks.mwww_daemon.components;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DynamicLabel extends StackPane {

    private final Label label = new Label();
    private final TextField textField = new TextField();

    private Consumer<String> onSubmit = (_) -> {};

    public DynamicLabel() {
        getChildren().add(label);
        textField.minWidthProperty().bind(widthProperty());
        setDefaultKeybindings();
    }

    public CompletableFuture<String> acceptNext(String prompt) {
        switchToTextField();
        textField.setText(prompt);

        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        Runnable onSubmit = () -> {
            String input = textField.getText();
            completableFuture.complete(input);

            this.setDefaultKeybindings();
            this.switchToLabel();
            textField.setPromptText("");
        };
        setKeybindings(onSubmit);

        return completableFuture;
    }

    public void switchToTextField() {
        getChildren().remove(label);
        getChildren().add(textField);
    }

    public void setText(String text) {
        label.setText(text);
    }

    public void setOnSubmit(Consumer<String> onSubmit) {
        this.onSubmit = onSubmit;
    }

    private void onSubmit() {
        String input = textField.getText();

        if (input.trim().isEmpty()) {
            switchToLabel();
            return;
        }

        onSubmit.accept(input);
        switchToLabel();
    }

    private void switchToLabel() {
        getChildren().remove(textField);
        getChildren().add(label);
    }

    private void setDefaultKeybindings() {
        setKeybindings(this::onSubmit);
    }

    private void setKeybindings(Runnable onSubmit) {
        textField.setOnKeyPressed(keyEvent -> {
            switch (keyEvent.getCode()) {
                case ESCAPE -> switchToLabel();
                case ENTER -> onSubmit.run();
            }
        });
    }
}

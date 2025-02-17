package org.maks.musicplayer.components;

import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.maks.musicplayer.model.SongPlayer;

import java.util.concurrent.atomic.AtomicReference;

public class DurationSlider extends StackPane {

    private final Slider slider = new Slider();
    private boolean sliderIsDragged = false;

    public DurationSlider() {
        slider.setId("duration-slider");

        Rectangle progressRec = new Rectangle();
        progressRec.heightProperty().bind(slider.heightProperty().subtract(7));
        progressRec.widthProperty().bind(slider.widthProperty());

        progressRec.setFill(Color.rgb(100, 100, 100, 1));

        progressRec.setArcHeight(9);
        progressRec.setArcWidth(9);

        slider.valueProperty().addListener((_, _, new_val) -> {
            String style = String.format("-fx-fill: linear-gradient(to right, rgb(235, 235, 235) %f%%, rgb(100, 100, 100) %f%%);",
                    new_val.doubleValue(), new_val.doubleValue());
            progressRec.setStyle(style);
        });

        getChildren().addAll(progressRec, slider);
    }

    public void bindSliderValue(AtomicReference<SongPlayer> currentMediaContainer) {
        slider.setOnMousePressed(_ -> {
            if (currentMediaContainer.get() == null) {
                return;
            }

            sliderIsDragged = true;
        });

        slider.setOnMouseReleased(_ -> {
            if (currentMediaContainer.get() == null) {
                return;
            }

            double percentage = slider.getValue();
            SongPlayer songPlayer = currentMediaContainer.get();

            MediaPlayer mediaPlayer = songPlayer.mediaPlayer();
            Duration duration = mediaPlayer.getCycleDuration();

            Duration percentageDuration = duration.multiply((percentage / 100));
            mediaPlayer.seek(percentageDuration);

            sliderIsDragged = false;
        });
    }

    public void setValue(double value) {
        slider.setValue(value);
    }

    public boolean sliderIsDragged() {
        return sliderIsDragged;
    }

}

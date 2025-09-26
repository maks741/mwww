package org.maks.musicplayer.components;

import javafx.beans.NamedArg;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import org.maks.musicplayer.utils.SongAvatarRadius;

public class RoundedImageView extends ImageView {

    private final SongAvatarRadius radius;

    public RoundedImageView(@NamedArg("radius") SongAvatarRadius radius) {
        this.radius = radius;
        addClip();
    }

    private void addClip() {
        Rectangle clip = new Rectangle(75, 75);

        fitWidthProperty().addListener((observableValue, oldWidth, width) -> {
            clip.setWidth(width.doubleValue());
        });

        fitHeightProperty().addListener((observableValue, oldHeight, height) -> {
            clip.setHeight(height.doubleValue());
        });

        clip.setArcHeight(radius.value());
        clip.setArcWidth(radius.value());
        setClip(clip);
    }
}

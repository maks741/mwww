package org.maks.musicplayer.components;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

public class RoundedImageView extends ImageView {

    public RoundedImageView(Image image) {
        super(image);
        addClip();
    }

    public RoundedImageView() {
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

        double arc = 20;

        clip.setArcHeight(arc);
        clip.setArcWidth(arc);
        setClip(clip);
    }

}

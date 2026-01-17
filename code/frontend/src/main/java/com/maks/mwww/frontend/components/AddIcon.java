package com.maks.mwww.frontend.components;

import com.maks.mwww.domain.enumeration.Icon;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import com.maks.mwww.frontend.utils.IconUtils;

public class AddIcon extends ImageView {

    private final Image addImage = IconUtils.image(Icon.ADD);

    public AddIcon() {
        setImage(addImage);
    }

    public void loading() {
        Image loadingGif = IconUtils.image(Icon.LOADING);
        setImage(loadingGif);
    }

    public void fail() {
        showThenReset(Icon.X);
    }

    public void like() {
        showThenReset(Icon.HEART);
    }

    public void success() {
        showThenReset(Icon.CHECKMARK);
    }

    public void reset() {
        setImage(addImage);
    }

    private void showThenReset(Icon icon) {
        Image image = IconUtils.image(icon);
        setImage(image);

        var keyFrame = new KeyFrame(Duration.seconds(1), _ ->
                reset()
        );
        new Timeline(keyFrame).play();
    }
}

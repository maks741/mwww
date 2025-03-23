package org.maks.musicplayer.components;

import javafx.beans.property.BooleanProperty;
import javafx.scene.image.ImageView;
import org.maks.musicplayer.enumeration.Icon;
import org.maks.musicplayer.utils.IconUtils;

public class PauseToggle extends ImageView {

    public void bind(BooleanProperty songPlayingProperty) {
        songPlayingProperty.addListener((observableValue, prev, playing) -> {
            if (playing) {
                onMusicPlayed();
            } else {
                onMusicPaused();
            }
        });
    }

    private void onMusicPlayed() {
        setImage(IconUtils.image(Icon.PAUSE));
    }

    private void onMusicPaused() {
        setImage(IconUtils.image(Icon.PLAY));
    }

}

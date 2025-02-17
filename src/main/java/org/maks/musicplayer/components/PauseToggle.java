package org.maks.musicplayer.components;

import javafx.beans.property.BooleanProperty;
import javafx.scene.image.ImageView;
import org.maks.musicplayer.enumeration.Icon;
import org.maks.musicplayer.utils.IconUtils;

public class PauseToggle extends ImageView {

    public void bind(BooleanProperty songPlayingProperty) {
        songPlayingProperty.addListener((_, _, playing) -> {
            if (playing) {
                onMusicPlayed();
            } else {
                onMusicPaused();
            }
        });
    }

    public void onMusicPlayed() {
        setImage(IconUtils.image(Icon.PAUSE));
    }

    public void onMusicPaused() {
        setImage(IconUtils.image(Icon.PLAY));
    }

}

package org.maks.musicplayer.components;

import javafx.scene.image.ImageView;
import org.maks.musicplayer.enumeration.Icon;
import org.maks.musicplayer.utils.IconUtils;

public class RepeatSongToggle extends ImageView {

    private boolean onRepeat = false;

    public void toggleOnRepeat() {
        if (onRepeat) {
            onRepeat = false;
            setImage(IconUtils.image(Icon.REPEAT));
        } else {
            onRepeat = true;
            setImage(IconUtils.image(Icon.REPEAT_SINGLE));
        }
    }

    public boolean onRepeat() {
        return onRepeat;
    }
}

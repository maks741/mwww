package org.maks.mwww_daemon.components;

import javafx.scene.image.ImageView;
import org.maks.mwww_daemon.enumeration.Icon;
import org.maks.mwww_daemon.utils.IconUtils;

public class RepeatSongToggle extends ImageView {

    private final IconUtils iconUtils = new IconUtils();
    private boolean onRepeat = false;

    public void toggleOnRepeat() {
        if (onRepeat) {
            onRepeat = false;
            setImage(iconUtils.image(Icon.REPEAT));
        } else {
            onRepeat = true;
            setImage(iconUtils.image(Icon.REPEAT_SINGLE));
        }
    }

    public boolean onRepeat() {
        return onRepeat;
    }
}

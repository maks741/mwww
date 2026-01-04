package org.maks.mwww_daemon.components;

import org.maks.mwww_daemon.enumeration.Icon;
import org.maks.mwww_daemon.utils.IconUtils;

public class ShuffleToggle extends Toggle {

    @Override
    protected void toggleOn() {
        setImage(IconUtils.image(Icon.SHUFFLE_ON));
    }

    @Override
    protected void toggleOff() {
        setImage(IconUtils.image(Icon.SHUFFLE_OFF));
    }
}
